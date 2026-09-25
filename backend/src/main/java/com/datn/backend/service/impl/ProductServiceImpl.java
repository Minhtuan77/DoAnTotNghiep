package com.datn.backend.service.impl;

import com.datn.backend.dto.request.ProductRequest;
import com.datn.backend.dto.request.ProductSpecificationRequest;
import com.datn.backend.dto.response.BrandResponse;
import com.datn.backend.dto.response.CategoryResponse;
import com.datn.backend.dto.response.PageResponse;
import com.datn.backend.dto.response.ProductImageResponse;
import com.datn.backend.dto.response.ProductResponse;
import com.datn.backend.dto.response.ProductSpecificationResponse;
import com.datn.backend.entity.Brand;
import com.datn.backend.entity.Category;
import com.datn.backend.entity.Inventory;
import com.datn.backend.entity.Product;
import com.datn.backend.entity.ProductImage;
import com.datn.backend.entity.ProductSpecification;
import com.datn.backend.entity.enums.ProductStatus;
import com.datn.backend.exception.ResourceNotFoundException;
import com.datn.backend.repository.BrandRepository;
import com.datn.backend.repository.CategoryRepository;
import com.datn.backend.repository.InventoryRepository;
import com.datn.backend.repository.ProductRepository;
import com.datn.backend.service.ProductService;
import com.datn.backend.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    private final BrandRepository brandRepository;

    private final InventoryRepository inventoryRepository;

    private final InventoryService inventoryService;

    // =========================================================
    // GET ALL PRODUCTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAllProducts(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String keyword,
            Long categoryId,
            Long brandId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            ProductStatus status
    ) {

        // -------------------------
        // Validate pagination
        // -------------------------

        if (page < 0) {
            page = 0;
        }

        if (size <= 0) {
            size = 10;
        }

        // -------------------------
        // Default sorting
        // -------------------------

        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "createdAt";
        }

        if (sortDir == null || sortDir.isBlank()) {
            sortDir = "DESC";
        }

        Sort sort;

        if ("ASC".equalsIgnoreCase(sortDir)) {
            sort = Sort.by(sortBy).ascending();
        } else {
            sort = Sort.by(sortBy).descending();
        }

        Pageable pageable =
                PageRequest.of(page, size, sort);

        // -------------------------
        // Filter
        // -------------------------

        Specification<Product> specification =
                com.datn.backend.repository.spec.ProductSpecification
                        .filterProducts(
                                keyword,
                                categoryId,
                                brandId,
                                minPrice,
                                maxPrice,
                                status
                        );

        // -------------------------
        // Query
        // -------------------------

        Page<Product> productPage =
                productRepository.findAll(
                        specification,
                        pageable
                );

        // -------------------------
        // PageResponse
        // -------------------------

        return PageResponse.from(
                productPage,
                this::mapToResponse
        );
    }

    // =========================================================
    // GET PRODUCT BY ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {

        Product product =
                productRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy sản phẩm với ID: "
                                                + id
                                )
                        );

        return mapToResponse(product);
    }

    // =========================================================
    // GET PRODUCT BY SLUG
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductBySlug(
            String slug
    ) {

        Product product =
                productRepository.findBySlug(slug)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy sản phẩm với slug: "
                                                + slug
                                )
                        );

        return mapToResponse(product);
    }

    // =========================================================
    // CREATE PRODUCT
    // =========================================================

    @Override
    public ProductResponse createProduct(
            ProductRequest request,
            Long userId
    ) {

        // -------------------------
        // Validate SKU
        // -------------------------

        String sku = request.getSku().trim();

        if (productRepository.existsBySku(sku)) {

            throw new IllegalArgumentException(
                    "SKU đã tồn tại: " + sku
            );
        }

        // -------------------------
        // Validate name
        // -------------------------

        String name = request.getName().trim();

        if (productRepository.existsByName(name)) {

            throw new IllegalArgumentException(
                    "Tên sản phẩm đã tồn tại: " + name
            );
        }

        // -------------------------
        // Find Category
        // -------------------------

        Category category =
                categoryRepository.findById(
                                request.getCategoryId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy danh mục với ID: "
                                                + request.getCategoryId()
                                )
                        );

        // -------------------------
        // Find Brand
        // -------------------------

        Brand brand = null;

        if (request.getBrandId() != null) {

            brand =
                    brandRepository.findById(
                                    request.getBrandId()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Không tìm thấy thương hiệu với ID: "
                                                    + request.getBrandId()
                                    )
                            );
        }

        // -------------------------
        // Generate slug
        // -------------------------

        String slug = createUniqueSlug(name);

        // -------------------------
        // Create Product
        // -------------------------

        Product product =
                Product.builder()
                        .sku(sku)
                        .name(name)
                        .slug(slug)
                        .category(category)
                        .brand(brand)
                        .description(
                                request.getDescription()
                        )
                        .price(request.getPrice())
                        .salePrice(request.getSalePrice())
                        .status(
                                request.getStatus() != null
                                        ? request.getStatus()
                                        : ProductStatus.ACTIVE
                        )
                        .avgRating(BigDecimal.ZERO)
                        .reviewCount(0)
                        .soldCount(0)
                        .viewCount(0)
                        .images(new ArrayList<>())
                        .specifications(new ArrayList<>())
                        .build();

        // -------------------------
        // Images
        // -------------------------

        addImages(
                product,
                request.getImageUrls()
        );

        // -------------------------
        // Specifications
        // -------------------------

        addSpecifications(
                product,
                request.getSpecifications()
        );

        // -------------------------
        // Save Product
        // -------------------------

        Product savedProduct =
                productRepository.save(product);

        // -------------------------
        // Initialize Inventory
        // -------------------------

        inventoryService.initializeInventory(
                savedProduct.getProductId(),
                request.getStockQuantity() != null
                        ? request.getStockQuantity()
                        : 0,
                userId
        );

        // -------------------------
        // Return response
        // -------------------------

        return mapToResponse(savedProduct);
    }

    // =========================================================
    // UPDATE PRODUCT
    // =========================================================

    @Override
    public ProductResponse updateProduct(
            Long id,
            ProductRequest request
    ) {

        // -------------------------
        // Find Product
        // -------------------------

        Product product =
                productRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy sản phẩm với ID: "
                                                + id
                                )
                        );

        // -------------------------
        // Validate SKU
        // -------------------------

        String newSku = request.getSku().trim();

        if (!product.getSku().equals(newSku)
                && productRepository.existsBySku(newSku)) {

            throw new IllegalArgumentException(
                    "SKU đã tồn tại: " + newSku
            );
        }

        // -------------------------
        // Validate name
        // -------------------------

        String oldName = product.getName();

        String newName =
                request.getName().trim();

        if (!oldName.equalsIgnoreCase(newName)
                && productRepository.existsByName(newName)) {

            throw new IllegalArgumentException(
                    "Tên sản phẩm đã tồn tại: "
                            + newName
            );
        }

        // -------------------------
        // Find Category
        // -------------------------

        Category category =
                categoryRepository.findById(
                                request.getCategoryId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy danh mục với ID: "
                                                + request.getCategoryId()
                                )
                        );

        // -------------------------
        // Find Brand
        // -------------------------

        Brand brand = null;

        if (request.getBrandId() != null) {

            brand =
                    brandRepository.findById(
                                    request.getBrandId()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Không tìm thấy thương hiệu với ID: "
                                                    + request.getBrandId()
                                    )
                            );
        }

        // -------------------------
        // Update basic information
        // -------------------------

        product.setSku(newSku);

        product.setName(newName);

        // Chỉ tạo slug mới khi tên thay đổi
        if (!oldName.equalsIgnoreCase(newName)) {

            product.setSlug(
                    createUniqueSlug(newName)
            );
        }

        product.setCategory(category);

        product.setBrand(brand);

        product.setDescription(
                request.getDescription()
        );

        product.setPrice(
                request.getPrice()
        );

        product.setSalePrice(
                request.getSalePrice()
        );

        if (request.getStatus() != null) {

            product.setStatus(
                    request.getStatus()
            );
        }

        // -------------------------
        // Update Images
        // -------------------------

        product.getImages().clear();

        addImages(
                product,
                request.getImageUrls()
        );

        // -------------------------
        // Update Specifications
        // -------------------------

        product.getSpecifications().clear();

        addSpecifications(
                product,
                request.getSpecifications()
        );

        // -------------------------
        // Save Product
        // -------------------------

        Product updatedProduct =
                productRepository.save(product);

        // ---------------------------------------------------------
        // KHÔNG cập nhật tồn kho tại đây.
        // Tồn kho phải đi qua InventoryService để luôn tạo
        // InventoryTransaction và giữ được lịch sử audit.
        // ---------------------------------------------------------

        // -------------------------
        // Return response
        // -------------------------

        return mapToResponse(updatedProduct);
    }

    // =========================================================
    // DELETE PRODUCT
    // =========================================================

    @Override
    public void deleteProduct(Long id) {

        Product product =
                productRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy sản phẩm với ID: "
                                                + id
                                )
                        );

        // Inventory dùng product_id làm PK/FK
        // nên xóa Inventory trước
        if (inventoryRepository.existsById(id)) {
            inventoryRepository.deleteById(id);
        }

        // ProductImage và ProductSpecification
        // sẽ được xóa nhờ cascade + orphanRemoval
        productRepository.delete(product);
    }

    // =========================================================
    // ADD IMAGES
    // =========================================================

    private void addImages(
            Product product,
            List<String> imageUrls
    ) {

        if (imageUrls == null
                || imageUrls.isEmpty()) {

            return;
        }

        for (int i = 0;
             i < imageUrls.size();
             i++) {

            String imageUrl =
                    imageUrls.get(i);

            if (imageUrl == null
                    || imageUrl.isBlank()) {

                continue;
            }

            ProductImage image =
                    ProductImage.builder()
                            .product(product)
                            .imageUrl(
                                    imageUrl.trim()
                            )
                            .isPrimary(i == 0)
                            .sortOrder(
                                    (short) i
                            )
                            .build();

            product.getImages().add(image);
        }
    }

    // =========================================================
    // ADD SPECIFICATIONS
    // =========================================================

    private void addSpecifications(
            Product product,
            List<ProductSpecificationRequest> requests
    ) {

        if (requests == null
                || requests.isEmpty()) {

            return;
        }

        for (int i = 0;
             i < requests.size();
             i++) {

            ProductSpecificationRequest request =
                    requests.get(i);

            ProductSpecification specification =
                    ProductSpecification.builder()
                            .product(product)
                            .specName(
                                    request.getSpecName()
                                            .trim()
                            )
                            .specValue(
                                    request.getSpecValue()
                                            .trim()
                            )
                            .sortOrder(
                                    (short) i
                            )
                            .build();

            product.getSpecifications()
                    .add(specification);
        }
    }

    // =========================================================
    // CREATE UNIQUE SLUG
    // =========================================================

    private String createUniqueSlug(
            String name
    ) {

        String baseSlug =
                toSlug(name);

        String slug = baseSlug;

        int counter = 1;

        while (
                productRepository
                        .findBySlug(slug)
                        .isPresent()
        ) {

            slug =
                    baseSlug
                            + "-"
                            + counter;

            counter++;
        }

        return slug;
    }

    // =========================================================
    // CONVERT NAME -> SLUG
    // =========================================================

    private String toSlug(String text) {

        if (text == null
                || text.isBlank()) {

            return "product";
        }

        String normalized =
                Normalizer.normalize(
                        text,
                        Normalizer.Form.NFD
                );

        normalized =
                normalized.replaceAll(
                        "\\p{InCombiningDiacriticalMarks}+",
                        ""
                );

        normalized =
                normalized
                        .toLowerCase(Locale.ENGLISH)
                        .replace("đ", "d");

        return normalized
                .replaceAll(
                        "[^a-z0-9]+",
                        "-"
                )
                .replaceAll(
                        "^-+",
                        ""
                )
                .replaceAll(
                        "-+$",
                        "");
    }

    // =========================================================
    // MAP PRODUCT -> RESPONSE
    // =========================================================

    private ProductResponse mapToResponse(
            Product product
    ) {

        // -------------------------
        // Get Inventory
        // -------------------------

        Integer stockQuantity = 0;

        Inventory inventory =
                inventoryRepository
                        .findById(
                                product.getProductId()
                        )
                        .orElse(null);

        if (inventory != null) {

            stockQuantity =
                    inventory.getQuantityOnHand();
        }

        // -------------------------
        // Build Response
        // -------------------------

        return ProductResponse.builder()
                .id(product.getProductId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(
                        product.getDescription()
                )
                .price(product.getPrice())
                .salePrice(product.getSalePrice())
                .stockQuantity(stockQuantity)
                .ratingAverage(
                        product.getAvgRating() != null
                                ? product.getAvgRating()
                                        .doubleValue()
                                : 0.0
                )
                .reviewCount(
                        product.getReviewCount()
                )
                .status(
                        product.getStatus()
                )
                .category(
                        mapCategoryToResponse(
                                product.getCategory()
                        )
                )
                .brand(
                        mapBrandToResponse(
                                product.getBrand()
                        )
                )
                .images(
                        mapImagesToResponse(
                                product
                        )
                )
                .specifications(
                        mapSpecificationsToResponse(
                                product
                        )
                )
                .createdAt(
                        product.getCreatedAt()
                )
                .updatedAt(
                        product.getUpdatedAt()
                )
                .build();
    }

    // =========================================================
    // CATEGORY -> RESPONSE
    // =========================================================

    private CategoryResponse mapCategoryToResponse(
            Category category
    ) {

        if (category == null) {
            return null;
        }

        return CategoryResponse.builder()
                .id(
                        category.getCategoryId()
                                .longValue()
                )
                .name(
                        category.getName()
                )
                .slug(
                        category.getSlug()
                )
                .description(
                        category.getDescription()
                )
                .parentId(
                        category.getParent() != null
                                ? category.getParent()
                                        .getCategoryId()
                                        .longValue()
                                : null
                )
                .createdAt(
                        category.getCreatedAt()
                )
                .updatedAt(null)
                .build();
    }

    // =========================================================
    // BRAND -> RESPONSE
    // =========================================================

    private BrandResponse mapBrandToResponse(
            Brand brand
    ) {

        if (brand == null) {
            return null;
        }

        return BrandResponse.builder()
                .id(
                        brand.getBrandId()
                                .longValue()
                )
                .name(
                        brand.getName()
                )
                .slug(null)
                .description(
                        brand.getDescription()
                )
                .logoUrl(
                        brand.getLogoUrl()
                )
                .createdAt(null)
                .updatedAt(null)
                .build();
    }

    // =========================================================
    // IMAGES -> RESPONSE
    // =========================================================

    private List<ProductImageResponse>
    mapImagesToResponse(
            Product product
    ) {

        if (product.getImages() == null
                || product.getImages().isEmpty()) {

            return new ArrayList<>();
        }

        return product.getImages()
                .stream()
                .map(image ->
                        ProductImageResponse.builder()
                                .id(
                                        image.getImageId()
                                )
                                .imageUrl(
                                        image.getImageUrl()
                                )
                                .isPrimary(
                                        image.getIsPrimary()
                                )
                                .build()
                )
                .toList();
    }

    // =========================================================
    // SPECIFICATIONS -> RESPONSE
    // =========================================================

    private List<ProductSpecificationResponse>
    mapSpecificationsToResponse(
            Product product
    ) {

        if (product.getSpecifications() == null
                || product.getSpecifications().isEmpty()) {

            return new ArrayList<>();
        }

        return product.getSpecifications()
                .stream()
                .map(spec ->
                        ProductSpecificationResponse
                                .builder()
                                .id(
                                        spec.getSpecId()
                                )
                                .specName(
                                        spec.getSpecName()
                                )
                                .specValue(
                                        spec.getSpecValue()
                                )
                                .build()
                )
                .toList();
    }
}