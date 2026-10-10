package com.datn.backend.service.impl;

import com.datn.backend.dto.response.*;
import com.datn.backend.entity.*;
import com.datn.backend.entity.enums.*;
import com.datn.backend.repository.*;
import com.datn.backend.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final UserBehaviorLogRepository behaviorRepository;
    private final ProductRelationRepository relationRepository;
    private final ProductRecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public RecommendationResponse recommend(Long userId, String sessionId, RecommendationPlacement placement, Long anchorProductId, int limit) {
        limit = Math.max(1, Math.min(limit, 20));
        List<Product> active = productRepository.findByStatus(ProductStatus.ACTIVE);
        if (active.isEmpty()) return RecommendationResponse.builder().placement(placement).strategy("EMPTY_CATALOG").products(List.of()).build();

        List<UserBehaviorLog> logs = userId != null
                ? behaviorRepository.findTop200ByUser_UserIdOrderByCreatedAtDesc(userId)
                : (sessionId == null || sessionId.isBlank() ? List.of() : behaviorRepository.findTop200BySessionIdOrderByCreatedAtDesc(sessionId));

        Map<Long, Double> scores = new HashMap<>();
        String strategy;
        if (anchorProductId != null && (placement == RecommendationPlacement.PRODUCT_DETAIL || placement == RecommendationPlacement.CART)) {
            Product anchor = productRepository.findById(anchorProductId).orElse(null);
            if (anchor != null) addAnchorScores(scores, active, anchor, placement);
        }
        if (!logs.isEmpty()) {
            strategy = "PERSONALIZED_BEHAVIOR";
            addBehaviorScores(scores, active, logs);
        } else {
            strategy = "COLD_START_POPULARITY";
            addColdStartScores(scores, active);
        }
        addPopularityBase(scores, active);
        if (anchorProductId != null) scores.remove(anchorProductId);

        List<Product> ranked = active.stream()
                .filter(p -> scores.containsKey(p.getProductId()))
                .sorted(Comparator.comparingDouble((Product p) -> scores.getOrDefault(p.getProductId(), 0d)).reversed())
                .limit(limit).toList();

        if (userId != null) cache(userId, placement, ranked, scores);
        return RecommendationResponse.builder().placement(placement).strategy(strategy)
                .products(ranked.stream().map(this::toProductResponse).toList()).build();
    }

    private void addBehaviorScores(Map<Long,Double> scores, List<Product> products, List<UserBehaviorLog> logs) {
        Map<Integer,Double> categoryWeights = new HashMap<>();
        Map<Integer,Double> brandWeights = new HashMap<>();
        Set<Long> purchased = new HashSet<>();
        for (UserBehaviorLog log : logs) {
            Product p = log.getProduct(); if (p == null) continue;
            double w = actionWeight(log.getActionType());
            if (p.getCategory() != null) categoryWeights.merge(p.getCategory().getCategoryId(), w, Double::sum);
            if (p.getBrand() != null) brandWeights.merge(p.getBrand().getBrandId(), w * .55, Double::sum);
            if (log.getActionType() == BehaviorActionType.PURCHASE) purchased.add(p.getProductId());
        }
        for (Product p : products) {
            double s=0;
            if (p.getCategory()!=null) s += categoryWeights.getOrDefault(p.getCategory().getCategoryId(),0d);
            if (p.getBrand()!=null) s += brandWeights.getOrDefault(p.getBrand().getBrandId(),0d);
            if (purchased.contains(p.getProductId())) s *= .15;
            if (s>0) scores.merge(p.getProductId(), s, Double::sum);
        }
    }

    private void addAnchorScores(Map<Long,Double> scores, List<Product> products, Product anchor, RecommendationPlacement placement) {
        RelationType type = placement == RecommendationPlacement.CART ? RelationType.COMPLEMENTARY : RelationType.SIMILAR;
        for (ProductRelation r : relationRepository.findTop20ByProduct_ProductIdAndRelationTypeOrderByScoreDesc(anchor.getProductId(), type)) {
            scores.merge(r.getRelatedProduct().getProductId(), r.getScore().doubleValue()*20, Double::sum);
        }
        for (Product p : products) {
            if (p.getProductId().equals(anchor.getProductId())) continue;
            double s=0;
            if (p.getCategory()!=null && anchor.getCategory()!=null && Objects.equals(p.getCategory().getCategoryId(), anchor.getCategory().getCategoryId())) s += 5;
            if (p.getBrand()!=null && anchor.getBrand()!=null && Objects.equals(p.getBrand().getBrandId(), anchor.getBrand().getBrandId())) s += 2;
            if (s>0) scores.merge(p.getProductId(), s, Double::sum);
        }
    }

    private void addColdStartScores(Map<Long,Double> scores, List<Product> products) {
        for (Product p: products) scores.merge(p.getProductId(), popularity(p), Double::sum);
    }
    private void addPopularityBase(Map<Long,Double> scores, List<Product> products) {
        for (Product p: products) scores.merge(p.getProductId(), popularity(p)*.15, Double::sum);
    }
    private double popularity(Product p) {
        double sold = Optional.ofNullable(p.getSoldCount()).orElse(0);
        double views = Optional.ofNullable(p.getViewCount()).orElse(0);
        double rating = Optional.ofNullable(p.getAvgRating()).orElse(BigDecimal.ZERO).doubleValue();
        double reviews = Optional.ofNullable(p.getReviewCount()).orElse(0);
        double fresh = p.getCreatedAt()!=null ? Math.max(0, 3 - java.time.temporal.ChronoUnit.DAYS.between(p.getCreatedAt(), LocalDateTime.now())/30.0) : 0;
        return sold*2.5 + Math.log1p(views)*1.2 + rating*2 + Math.log1p(reviews)*1.5 + fresh;
    }
    private double actionWeight(BehaviorActionType t) { return switch(t){case VIEW->1;case SEARCH->.5;case ADD_TO_CART->3;case FAVORITE->3.5;case PURCHASE->7;case REVIEW->5;}; }

    private void cache(Long userId, RecommendationPlacement placement, List<Product> ranked, Map<Long,Double> scores) {
        recommendationRepository.deleteByUser_UserIdAndPlacement(userId, placement);
        User user = userRepository.findById(userId).orElse(null); if (user==null) return;
        LocalDateTime exp=LocalDateTime.now().plusHours(1);
        for(Product p: ranked) recommendationRepository.save(ProductRecommendation.builder().user(user).product(p).placement(placement)
                .score(BigDecimal.valueOf(scores.getOrDefault(p.getProductId(),0d)).setScale(4,RoundingMode.HALF_UP)).expiresAt(exp).build());
    }

    @Override
    @Transactional
    public int rebuildProductRelations() {
        List<Product> products = productRepository.findByStatus(ProductStatus.ACTIVE);
        relationRepository.deleteByRelationType(RelationType.SIMILAR);
        relationRepository.deleteByRelationType(RelationType.COMPLEMENTARY);
        int count=0;
        for (Product a: products) {
            List<Product> similar=products.stream().filter(b->!a.getProductId().equals(b.getProductId()))
                    .sorted(Comparator.comparingDouble((Product b)->similarity(a,b)).reversed()).limit(8).toList();
            for(Product b:similar){double s=similarity(a,b);if(s<=0)continue; saveRelation(a,b,RelationType.SIMILAR,s);count++;}
            for(Object[] row:orderItemRepository.findCoPurchasedProductIds(a.getProductId())){
                Long id=((Number)row[0]).longValue(); Product b=productRepository.findById(id).orElse(null);if(b==null)continue;
                double s=Math.min(1d, ((Number)row[1]).doubleValue()/10d);saveRelation(a,b,RelationType.COMPLEMENTARY,s);count++;
            }
        }
        return count;
    }
    private double similarity(Product a, Product b){double s=0;if(a.getCategory()!=null&&b.getCategory()!=null&&Objects.equals(a.getCategory().getCategoryId(),b.getCategory().getCategoryId()))s+=.6;if(a.getBrand()!=null&&b.getBrand()!=null&&Objects.equals(a.getBrand().getBrandId(),b.getBrand().getBrandId()))s+=.2;Set<String>x=tokens(a.getName()),y=tokens(b.getName());if(!x.isEmpty()){Set<String>i=new HashSet<>(x);i.retainAll(y);s+=.2*((double)i.size()/x.size());}return Math.min(1,s);}
    private Set<String> tokens(String v){if(v==null)return Set.of();return Arrays.stream(v.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+" )).filter(x->x.length()>2).collect(Collectors.toSet());}
    private void saveRelation(Product a, Product b, RelationType type, double score){relationRepository.save(ProductRelation.builder().product(a).relatedProduct(b).relationType(type).score(BigDecimal.valueOf(score).setScale(4,RoundingMode.HALF_UP)).build());}

    private ProductResponse toProductResponse(Product p) {
        Inventory inv=inventoryRepository.findByProduct_ProductId(p.getProductId()).orElse(null);
        int stock=inv==null?0:Math.max(0,inv.getQuantityOnHand()-inv.getQuantityReserved());
        CategoryResponse cat=p.getCategory()==null?null:CategoryResponse.builder().id(p.getCategory().getCategoryId().longValue()).name(p.getCategory().getName()).slug(p.getCategory().getSlug()).description(p.getCategory().getDescription()).build();
        BrandResponse brand=p.getBrand()==null?null:BrandResponse.builder().id(p.getBrand().getBrandId().longValue()).name(p.getBrand().getName()).slug(null).description(p.getBrand().getDescription()).logoUrl(p.getBrand().getLogoUrl()).build();
        List<ProductImageResponse> images=p.getImages().stream().map(i->ProductImageResponse.builder().id(i.getImageId()).imageUrl(i.getImageUrl()).isPrimary(i.getIsPrimary()).build()).toList();
        return ProductResponse.builder().id(p.getProductId()).name(p.getName()).slug(p.getSlug()).description(p.getDescription()).price(p.getPrice()).salePrice(p.getSalePrice()).stockQuantity(stock)
                .ratingAverage(p.getAvgRating()==null?0:p.getAvgRating().doubleValue()).reviewCount(p.getReviewCount()).status(p.getStatus()).category(cat).brand(brand).images(images).specifications(List.of()).createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt()).build();
    }
}
