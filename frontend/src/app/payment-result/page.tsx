'use client';
import Link from 'next/link';
import {useSearchParams} from 'next/navigation';
export default function PaymentResult(){const s=useSearchParams();const code=s.get('vnp_ResponseCode')||s.get('responseCode');const ok=code==='00'||s.get('success')==='true';return <div className="authWrap"><h1>{ok?'Thanh toán thành công':'Kết quả thanh toán'}</h1><p className="muted">{ok?'Giao dịch đã được cổng thanh toán xác nhận.':'Vui lòng kiểm tra trạng thái đơn hàng để biết kết quả giao dịch mới nhất.'}</p>{code&&<p>Mã phản hồi: <b>{code}</b></p>}<div className="toolbar"><Link className="primary" href="/orders">Xem đơn hàng</Link><Link className="secondary" href="/products">Tiếp tục mua sắm</Link></div></div>}
