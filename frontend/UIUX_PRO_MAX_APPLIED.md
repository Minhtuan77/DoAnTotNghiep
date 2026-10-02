# UI/UX Pro Max — DATN Applied Design System

Frontend này được refactor dựa trên bộ `ui-ux-pro-max-skill-main.zip`, ưu tiên nhóm khuyến nghị dành cho **E-commerce** và **Next.js**.

## Hướng thiết kế đã chọn

- E-commerce: Vibrant & Block-based kết hợp Flat/Soft UI Evolution.
- Light mode, nền trung tính sáng, thương hiệu xanh lá tạo cảm giác tin cậy và phù hợp đồ gia dụng.
- Product card rõ ảnh, tên, rating, giá, sale và trạng thái tồn kho.
- Admin dashboard ưu tiên khả năng quét nhanh: navigation cố định, KPI, bảng dữ liệu, status badge.
- Không dùng glassmorphism tối cho storefront vì không phù hợp bằng hướng retail sáng, dễ đọc.

## Design tokens chính

- Primary: `#176B49`
- Primary strong: `#0F5639`
- Background: `#F7F8F6`
- Surface: `#FFFFFF`
- Text: `#15251D`
- Accent: `#F2A900`
- Destructive: `#B42318`
- Radius: 10 / 16 / 24 px
- Motion: 150–250 ms, tôn trọng `prefers-reduced-motion`

## UX đã áp dụng

- Visible keyboard focus.
- Skip link đến nội dung chính.
- Mobile navigation đầy đủ thay vì ẩn navigation.
- Responsive tại desktop/tablet/mobile.
- Form có label, inputMode phù hợp cho số điện thoại.
- Error/empty/loading state rõ ràng.
- Loading skeleton ở cấp route/catalog.
- Button có disabled state.
- Table admin nằm trong vùng overflow an toàn trên màn nhỏ.
- Không dùng emoji làm icon thao tác; icon SVG nội bộ.

## Nguyên tắc dữ liệu

- Không tạo catalog/sản phẩm/đơn hàng/voucher giả trong frontend.
- Product/category/brand/cart/order/review/wishlist/user/inventory/voucher lấy từ API backend.
- Khi database không có dữ liệu, UI hiển thị empty state.
- Ảnh placeholder chỉ được dùng khi bản ghi sản phẩm không trả ảnh; seed hiện tại cũng có image URL riêng trong database.

## API

Không thay đổi contract API trong `src/lib/api.ts`. Việc refactor tập trung vào presentation/UX để không phá backend đã hoàn thiện.
