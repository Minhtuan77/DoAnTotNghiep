import type { Metadata } from "next";
import "./globals.css";
export const metadata: Metadata = { title: "GiaDụngViệt", description: "DATN - Thương mại điện tử gia dụng" };
export default function RootLayout({children}:{children:React.ReactNode}){return <html lang="vi"><body>{children}</body></html>}
