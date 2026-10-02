# Frontend DATN - Rework theo Backend + SRS + Database

## Nguyên tắc
- Không dùng mockdata trong source `src/`.
- Catalog, giá, tồn kho, giỏ hàng, đơn hàng, wishlist, review, voucher, hồ sơ... đều lấy từ REST API của backend.
- Đã bỏ toàn bộ `public/stitch`/iframe tĩnh của bản cũ.
- Chỉ xây màn hình có nghiệp vụ/API hiện tại; không giả lập AI Chatbot, AI Recommendation hay System Configuration khi backend chưa có controller tương ứng.

## Customer routes
- `/` Trang chủ dữ liệu thật
- `/products` Danh sách + tìm kiếm/lọc/sắp xếp
- `/products/[id]` Chi tiết + thông số + đánh giá + sản phẩm cùng danh mục
- `/search` Tìm kiếm
- `/cart` Giỏ hàng
- `/checkout` Địa chỉ + voucher + COD/VNPay
- `/orders`, `/orders/[id]` Lịch sử/chi tiết/hủy đơn
- `/wishlist` Yêu thích
- `/reviews`, `/reviews/write` Đánh giá
- `/account` Hồ sơ + địa chỉ + đổi mật khẩu
- `/login`, `/register`, `/forgot-password`, `/reset-password`
- `/payment-result`

## Admin routes
- `/admin` Tổng quan được aggregate từ API thật
- `/admin/products`
- `/admin/categories`
- `/admin/orders`
- `/admin/inventory`
- `/admin/vouchers`
- `/admin/reviews`
- `/admin/users`

## Cấu hình
`.env.local`:
```
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

## Kiểm tra đã thực hiện
- `tsc --noEmit`: PASS
- `npm run lint`: PASS
- `next build`: sandbox không thể tải binary `@next/swc-linux-x64-gnu` từ npm do không có network. Đây là giới hạn môi trường kiểm tra, không phải lỗi TypeScript/lint của source.

## Lưu ý permission từ file Data(2).sql
Dump DB được cung cấp chỉ có 16 permissions cũ (PRODUCT/USER/ORDER/ROLE/INVENTORY). Backend hiện tại còn dùng CATEGORY_*, BRAND_*, VOUCHER_*, REVIEW_* tại `@PreAuthorize`. Nếu DB thực tế của bạn chưa bổ sung các permission mới và gán cho ADMIN/STAFF, các thao tác quản trị tương ứng sẽ trả 403. Frontend không bypass quyền backend.
