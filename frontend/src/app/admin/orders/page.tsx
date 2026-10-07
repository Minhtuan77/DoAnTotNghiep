'use client';

import {useEffect,useState} from 'react';
import {api} from '@/lib/api';
import type {Order} from '@/lib/types';
import {money,Status} from '@/components/UI';

const nextStatuses:Record<string,string[]>={
 PENDING_PAYMENT:['CANCELLED'],
 PENDING_CONFIRMATION:['CONFIRMED','CANCELLED'],
 CONFIRMED:['SHIPPING','CANCELLED'],
 SHIPPING:['DELIVERED'],
 DELIVERED:['COMPLETED','RETURNED'],
 COMPLETED:['RETURNED'],
 CANCELLED:[],
 RETURNED:[]
};

const statusLabel:Record<string,string>={
 PENDING_PAYMENT:'Chờ thanh toán',
 PENDING_CONFIRMATION:'Chờ xác nhận',
 CONFIRMED:'Đã xác nhận',
 SHIPPING:'Đang giao',
 DELIVERED:'Đã giao',
 COMPLETED:'Hoàn thành',
 CANCELLED:'Đã hủy',
 RETURNED:'Đã trả hàng'
};

export default function AdminOrders(){
 const[a,setA]=useState<Order[]>([]);
 const[updating,setUpdating]=useState<number|null>(null);
 const load=()=>api.adminOrders().then((x:any)=>setA(x.content||[]));
 useEffect(()=>{load().catch((e:any)=>alert(e.message))},[]);

 async function updateStatus(order:Order,target:string){
   if(!target||target===order.status)return;
   setUpdating(order.orderId);
   try{
     await api.adminOrderStatus(order.orderId,target);
     await load();
   }catch(e:any){
     alert(e.message||'Không thể cập nhật trạng thái đơn hàng');
   }finally{setUpdating(null)}
 }

 return <>
  <div className="sectionHead"><div><h1>Quản lý đơn hàng</h1><p>Xem và cập nhật trạng thái đơn theo đúng luồng xử lý của hệ thống.</p></div></div>
  <div className="tableWrap"><table className="table"><thead><tr><th>Mã</th><th>Khách nhận</th><th>Ngày đặt</th><th>Tổng tiền</th><th>Trạng thái</th><th>Cập nhật</th></tr></thead><tbody>
   {a.map(o=>{const choices=nextStatuses[o.status]||[];return <tr key={o.orderId}>
    <td><b>{o.orderCode}</b></td>
    <td>{o.recipientName}<div className="muted smallText">{o.recipientPhone}</div></td>
    <td>{new Date(o.createdAt).toLocaleDateString('vi-VN')}</td>
    <td>{money(o.totalAmount)}</td>
    <td><Status value={o.status}/></td>
    <td>
      {choices.length===0
       ? <span className="muted smallText">Không còn bước cập nhật</span>
       : <select disabled={updating===o.orderId} value="" onChange={e=>updateStatus(o,e.target.value)}>
          <option value="">{updating===o.orderId?'Đang cập nhật...':'Chọn trạng thái mới'}</option>
          {choices.map(s=><option key={s} value={s}>{statusLabel[s]||s}</option>)}
         </select>}
    </td>
   </tr>})}
  </tbody></table></div>
 </>;
}
