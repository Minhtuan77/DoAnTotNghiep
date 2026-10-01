# Frontend Đợt 1 hoàn chỉnh — Stitch + Spring Boot

Bản này dùng đúng giao diện Stitch làm baseline và nối trực tiếp với backend DATN hiện tại tại `http://localhost:8080/api`.

## Luồng đã nối

- Điều hướng header/menu/search.
- Trang chủ / danh sách / tìm kiếm / chi tiết sản phẩm bằng Product API.
- Đăng ký, đăng nhập, access token, refresh token, logout.
- Quên mật khẩu / đặt lại mật khẩu.
- Thêm vào giỏ từ card và trang chi tiết; tăng/giảm/xóa/xóa giỏ.
- Voucher validate.
- Checkout, địa chỉ nhận hàng, COD và VNPay.
- Danh sách đơn hàng, xem chi tiết, hủy đơn, mở màn đánh giá từ order item.
- Hồ sơ cá nhân, đổi mật khẩu, địa chỉ cơ bản.
- Wishlist: xem, thêm giỏ, bỏ yêu thích.
- Reviews: xem đánh giá của tôi, xóa, mở sửa/viết đánh giá.
- VNPay return đọc kết quả từ backend.

## Chạy dự án

1. Chạy backend Spring Boot ở port 8080.
2. Trong thư mục `frontend`:

```bash
pnpm install
pnpm approve-builds
pnpm dev
```

3. Mở `http://localhost:3000`.

## Ghi chú kỹ thuật

- Giao diện Stitch nằm ở `public/stitch/*/index.html` và được giữ nguyên.
- Tất cả màn Stitch dùng chung một integration layer: `public/stitch/bridge.js`.
- Integration layer chịu trách nhiệm JWT, refresh token, API calls, routing và gắn hành vi vào UI Stitch.
- Đây là một lớp tích hợp dùng chung, không phải các bản vá riêng lẻ trên từng màn.

## Fix 2026-10-01: Product detail -> Add to cart
- Product cards now open `/products/{id}` from the title, image, or card body.
- Product detail action buttons use stable IDs (`btn-add-to-cart`, `btn-buy-now`) instead of relying only on visible text matching.
- Add-to-cart sends `{ productId, quantity }` to `POST /api/cart/items`.
- If not authenticated, the app redirects to `/login` and returns to the product after login.
- Auth/session storage is read from the top-level Next.js window so all Stitch iframes share the same JWT state.
- Add-to-cart refreshes the header cart badge after a successful request.
