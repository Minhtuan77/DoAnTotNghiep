'use client';

import Link from 'next/link';
import {useSearchParams} from 'next/navigation';

export default function PaymentResult(){
 const s=useSearchParams();
 const success=s.get('success')==='true';
 const signatureValid=s.get('signatureValid')!=='false';
 const code=s.get('responseCode')||s.get('vnp_ResponseCode')||'';
 const transactionNo=s.get('transactionNo')||s.get('vnp_TransactionNo')||'';
 const txnRef=s.get('txnRef')||s.get('vnp_TxnRef')||'';
 const ok=success||(!s.has('success')&&code==='00');

 return <main className="paymentResultPage">
   <section className={`paymentResultCard ${ok?'paymentResultSuccess':'paymentResultFailed'}`}>
     <div className="paymentResultIcon" aria-hidden="true">{ok?'✓':'!'}</div>
     <div className="eyebrow">Kết quả thanh toán VNPay</div>
     <h1>{ok?'Thanh toán thành công':'Thanh toán chưa thành công'}</h1>
     <p className="muted">
       {ok
        ? 'Giao dịch đã được VNPay xác nhận. Bạn có thể xem đơn hàng để theo dõi trạng thái xử lý.'
        : signatureValid
          ? 'Giao dịch chưa hoàn tất hoặc đã bị hủy. Đơn hàng vẫn được giữ theo trạng thái hiện tại.'
          : 'Kết quả trả về không vượt qua bước xác thực chữ ký. Vui lòng kiểm tra lại trạng thái đơn hàng.'}
     </p>

     {(code||transactionNo||txnRef)&&<div className="paymentMeta">
       {code&&<div><span>Mã phản hồi</span><b>{code}</b></div>}
       {transactionNo&&<div><span>Mã giao dịch VNPay</span><b>{transactionNo}</b></div>}
       {txnRef&&<div><span>Mã tham chiếu</span><b>{txnRef}</b></div>}
     </div>}

     <div className="paymentResultActions">
       <Link className="primary" href="/orders">Xem đơn hàng</Link>
       <Link className="secondary" href="/products">Tiếp tục mua sắm</Link>
     </div>
   </section>
 </main>;
}
