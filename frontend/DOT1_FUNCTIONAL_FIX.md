# DATN Frontend Đợt 1 - Functional Fix

Bản này sửa trực tiếp trên `frontend(1).zip`, giữ nguyên UI Stitch và thay integration layer `public/stitch/bridge.js` bằng bản ổn định hơn.

## Luồng đã nối

- Danh sách sản phẩm -> Chi tiết sản phẩm
- Chi tiết -> Thêm giỏ / Mua ngay / Yêu thích
- Giỏ hàng -> tăng/giảm/xóa -> Checkout
- Checkout -> COD -> tạo đơn -> Chi tiết đơn
- Đơn hàng -> xem chi tiết / hủy / mở trang đánh giá
- Wishlist -> xem chi tiết / thêm giỏ / bỏ yêu thích
- Account -> hồ sơ / đổi mật khẩu / địa chỉ / điều hướng Orders-Wishlist-Reviews
- Reviews -> sửa / xóa / viết đánh giá từ orderItemId

## API backend khớp `backend(7)`

- GET `/api/v1/products`
- GET `/api/v1/products/{id}`
- GET/POST/PUT/DELETE `/api/cart`
- POST `/api/orders`, GET `/api/orders`, GET/PATCH `/api/orders/{id}`
- GET/POST/DELETE `/api/wishlist`
- GET/POST/PUT/DELETE `/api/reviews`
- GET/PUT `/api/users/me`, address endpoints, password endpoint
- POST `/api/auth/*`

## Lưu ý kiểm thử

1. Chạy Spring Boot ở `http://localhost:8080`.
2. Đăng nhập bằng tài khoản có role `CUSTOMER` trước khi thử cart/order/wishlist/review.
3. Backend cần có ít nhất một sản phẩm `ACTIVE`.
4. Đánh giá mới phải được mở từ một order item hợp lệ vì backend yêu cầu `orderItemId`.
