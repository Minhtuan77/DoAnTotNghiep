# DATN Frontend - Đợt 1 (Stitch Exact)

Bản này ưu tiên giữ giao diện Stitch gần 1:1. Mỗi màn Stitch export được giữ nguyên HTML/CSS trong `public/stitch/*/index.html` và được Next.js route bằng `StitchScreen`.

## Chạy
```bash
pnpm install
pnpm approve-builds
pnpm dev
```
Mở http://localhost:3000

## Route
- `/` Trang chủ
- `/products` Danh mục / danh sách sản phẩm
- `/search` Tìm kiếm
- `/products/1` Chi tiết sản phẩm
- `/login`, `/register`, `/forgot-password`, `/reset-password`
- `/cart`, `/checkout`, `/payment-result`
- `/account`, `/orders`, `/orders/1`, `/wishlist`, `/reviews`, `/reviews/write`

## API Spring Boot
`src/services/datn.ts` đã khai báo lớp gọi API theo backend hiện tại. Giai đoạn kế tiếp sẽ thay dữ liệu tĩnh trong HTML Stitch bằng component React nhưng giữ nguyên visual.

## DATN Đợt 1 - bản chức năng hoàn chỉnh trên giao diện Stitch
- Giao diện Stitch gốc được giữ nguyên qua các file `public/stitch/*/index.html`.
- `public/stitch/bridge.js` nối UI Stitch với backend Spring Boot: điều hướng, tìm kiếm, sản phẩm, đăng ký/đăng nhập/JWT refresh/logout, giỏ hàng, voucher, checkout/COD/VNPay, đơn hàng, tài khoản, wishlist và review.
- Backend mặc định: `http://localhost:8080/api`.
- Chạy: `pnpm install` rồi `pnpm dev`.
