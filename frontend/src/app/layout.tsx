import './globals.css';
import AppShell from '@/components/AppShell';
export const metadata={title:{default:'Nhà Tiện Nghi',template:'%s | Nhà Tiện Nghi'},description:'Website thương mại điện tử đồ gia dụng - DATN'};
export default function RootLayout({children}:{children:React.ReactNode}){return <html lang="vi"><body><AppShell>{children}</AppShell></body></html>}
