package com.datn.backend.service.impl;

import com.datn.backend.dto.request.AddCartItemRequest;
import com.datn.backend.dto.request.UpdateCartItemRequest;
import com.datn.backend.dto.response.CartItemResponse;
import com.datn.backend.dto.response.CartResponse;
import com.datn.backend.entity.*;
import com.datn.backend.entity.enums.ProductStatus;
import com.datn.backend.exception.BusinessException;
import com.datn.backend.exception.ResourceNotFoundException;
import com.datn.backend.repository.*;
import com.datn.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public CartResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(Long userId, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        ensureProductPurchasable(product);

        CartItem item = cartItemRepository
                .findByCart_CartIdAndProduct_ProductId(cart.getCartId(), product.getProductId())
                .orElse(null);
        int newQuantity = request.getQuantity() + (item == null ? 0 : item.getQuantity());
        ensureStock(product.getProductId(), newQuantity);

        if (item == null) {
            item = CartItem.builder().cart(cart).product(product).quantity(newQuantity).build();
            cart.getItems().add(item);
        } else {
            item.setQuantity(newQuantity);
        }
        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        CartItem item = cartItemRepository.findByCartItemIdAndCart_User_UserId(cartItemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng"));
        ensureProductPurchasable(item.getProduct());
        ensureStock(item.getProduct().getProductId(), request.getQuantity());
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        return toResponse(item.getCart());
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long userId, Long cartItemId) {
        CartItem item = cartItemRepository.findByCartItemIdAndCart_User_UserId(cartItemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng"));
        Cart cart = item.getCart();
        cart.getItems().removeIf(i -> i.getCartItemId().equals(cartItemId));
        cartItemRepository.delete(item);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUser_UserId(userId).orElse(null);
        if (cart != null) {
            cart.getItems().clear();
            cartRepository.save(cart);
        }
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUser_UserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
            return cartRepository.save(Cart.builder().user(user).build());
        });
    }

    private void ensureProductPurchasable(Product product) {
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BusinessException("Sản phẩm hiện không thể mua");
        }
    }

    private void ensureStock(Long productId, int requestedQuantity) {
        Inventory inventory = inventoryRepository.findByProduct_ProductId(productId)
                .orElseThrow(() -> new BusinessException("Sản phẩm chưa có thông tin tồn kho"));
        int available = inventory.getQuantityOnHand() - inventory.getQuantityReserved();
        if (requestedQuantity > available) {
            throw new BusinessException("Số lượng yêu cầu vượt quá tồn kho khả dụng. Hiện còn " + available + " sản phẩm");
        }
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .sorted(Comparator.comparing(CartItem::getCartItemId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toItemResponse)
                .toList();
        BigDecimal subtotal = items.stream().map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shippingFee = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
        int totalItems = items.stream().mapToInt(CartItemResponse::getQuantity).sum();
        return CartResponse.builder()
                .cartId(cart.getCartId()).userId(cart.getUser().getUserId()).items(items)
                .totalItems(totalItems).subtotal(subtotal).shippingFee(shippingFee)
                .discount(discount).totalAmount(subtotal.add(shippingFee).subtract(discount))
                .updatedAt(cart.getUpdatedAt()).build();
    }

    private CartItemResponse toItemResponse(CartItem item) {
        Product p = item.getProduct();
        BigDecimal unitPrice = p.getSalePrice() != null ? p.getSalePrice() : p.getPrice();
        Inventory inventory = inventoryRepository.findByProduct_ProductId(p.getProductId()).orElse(null);
        int available = inventory == null ? 0 : inventory.getQuantityOnHand() - inventory.getQuantityReserved();
        String imageUrl = p.getImages().stream().filter(i -> Boolean.TRUE.equals(i.getIsPrimary()))
                .findFirst().or(() -> p.getImages().stream().findFirst()).map(ProductImage::getImageUrl).orElse(null);
        return CartItemResponse.builder().cartItemId(item.getCartItemId()).productId(p.getProductId())
                .sku(p.getSku()).productName(p.getName()).imageUrl(imageUrl).originalPrice(p.getPrice())
                .unitPrice(unitPrice).quantity(item.getQuantity()).availableQuantity(available)
                .lineTotal(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()))).build();
    }
}
