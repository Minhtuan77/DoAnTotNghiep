'use client';

import Link from 'next/link';
import {useEffect,useMemo,useState} from 'react';
import {api} from '@/lib/api';
import {money,Status,Icon} from '@/components/UI';

type RevenuePoint={date:string;revenue:number;orders:number};
type TopProduct={productId:number;name:string;quantitySold:number;revenue:number};
type RecentOrder={orderId:number;orderCode:string;recipientName:string;status:string;totalAmount:number;createdAt:string};
type LowStockItem={productId:number;productName:string;quantityOnHand:number;quantityReserved:number;availableQuantity:number;lowStockThreshold:number};
type Dashboard={
 totalProducts:number;activeProducts:number;totalOrders:number;totalCustomers:number;totalRevenue:number;
 pendingOrders:number;lowStockProducts:number;pendingReviews:number;todayOrders:number;todayRevenue:number;
 orderStatusCounts:Record<string,number>;revenueLast7Days:RevenuePoint[];topProducts:TopProduct[];
 recentOrders:RecentOrder[];lowStockItems:LowStockItem[];
};

const empty:Dashboard={totalProducts:0,activeProducts:0,totalOrders:0,totalCustomers:0,totalRevenue:0,pendingOrders:0,lowStockProducts:0,pendingReviews:0,todayOrders:0,todayRevenue:0,orderStatusCounts:{},revenueLast7Days:[],topProducts:[],recentOrders:[],lowStockItems:[]};
const statusLabel:Record<string,string>={PENDING_PAYMENT:'Chờ thanh toán',PENDING_CONFIRMATION:'Chờ xác nhận',CONFIRMED:'Đã xác nhận',SHIPPING:'Đang giao',DELIVERED:'Đã giao',COMPLETED:'Hoàn thành',CANCELLED:'Đã hủy',RETURNED:'Hoàn trả'};

export default function AdminDashboard(){
 const[d,setD]=useState<Dashboard>(empty),[loading,setLoading]=useState(true),[error,setError]=useState('');
 const load=()=>{setLoading(true);setError('');api.adminDashboard().then((x:any)=>setD(x||empty)).catch((e:any)=>setError(e.message||'Không tải được dashboard')).finally(()=>setLoading(false))};
 useEffect(()=>{load()},[]);
 const maxRevenue=useMemo(()=>Math.max(1,...d.revenueLast7Days.map(x=>Number(x.revenue||0))),[d.revenueLast7Days]);
 const totalStatus=useMemo(()=>Object.values(d.orderStatusCounts||{}).reduce<number>((a,b)=>a+Number(b||0),0)||1,[d.orderStatusCounts]);
 const cards=[
  {label:'Doanh thu hoàn tất',value:money(d.totalRevenue),sub:`Hôm nay ${money(d.todayRevenue)}`,icon:'grid' as const},
  {label:'Tổng đơn hàng',value:d.totalOrders,sub:`${d.todayOrders} đơn hôm nay`,icon:'orders' as const},
  {label:'Khách hàng',value:d.totalCustomers,sub:'Tài khoản CUSTOMER',icon:'users' as const},
  {label:'Sản phẩm',value:d.totalProducts,sub:`${d.activeProducts} đang hoạt động`,icon:'package' as const},
 ] as const;
 return <>
  <div className="sectionHead"><div><div className="eyebrow">Dashboard</div><h1>Tổng quan quản trị</h1><p>Số liệu được tổng hợp trực tiếp từ dữ liệu thật trong hệ thống.</p></div><button className="secondary small" onClick={load} disabled={loading}>↻ {loading?'Đang tải':'Làm mới'}</button></div>
  {error&&<div className="errorBox"><b>Không tải được dashboard.</b><span>{error}</span></div>}

  <div className="grid kpis">{cards.map(c=><div className="kpi" key={c.label}><span className="kpiLabel">{c.label}</span><strong className={c.label==='Doanh thu hoàn tất'?'kpiMoney':''}>{loading?'…':c.value}</strong><span className="kpiSub">{c.sub}</span><span className="kpiIcon"><Icon name={c.icon}/></span></div>)}</div>

  <div className="dashboardAlerts">
   <Link href="/admin/orders" className="dashboardAlert"><span className="dashboardAlertIcon">!</span><div><b>{d.pendingOrders} đơn cần xử lý</b><span>Chờ thanh toán hoặc chờ xác nhận</span></div><span>→</span></Link>
   <Link href="/admin/inventory" className="dashboardAlert"><span className="dashboardAlertIcon">↓</span><div><b>{d.lowStockProducts} sản phẩm sắp hết</b><span>Số lượng tồn kho dưới ngưỡng cảnh báo</span></div><span>→</span></Link>
   <Link href="/admin/reviews" className="dashboardAlert"><span className="dashboardAlertIcon">★</span><div><b>{d.pendingReviews} đánh giá chờ duyệt</b><span>Cần kiểm tra nội dung trước khi hiển thị</span></div><span>→</span></Link>
  </div>

  <div className="dashboardSplit">
   <section className="dashboardCard"><div className="dashboardCardHead"><div><h2>Doanh thu 7 ngày gần nhất</h2><p>Chỉ tính đơn Đã giao/Hoàn thành.</p></div></div><div className="revenueChart" aria-label="Biểu đồ doanh thu 7 ngày">{d.revenueLast7Days.map(x=>{const h=Math.max(4,Math.round(Number(x.revenue||0)/maxRevenue*100));return <div className="revenueCol" key={x.date} title={`${x.date}: ${money(x.revenue)}`}><div className="revenueValue">{Number(x.revenue)>0?money(x.revenue).replace('₫','').trim():'0'}</div><div className="revenueTrack"><div className="revenueBar" style={{height:`${h}%`}}/></div><b>{new Date(x.date+'T00:00:00').toLocaleDateString('vi-VN',{day:'2-digit',month:'2-digit'})}</b><span>{x.orders} đơn</span></div>})}</div></section>
   <section className="dashboardCard"><div className="dashboardCardHead"><div><h2>Trạng thái đơn hàng</h2><p>Phân bố toàn bộ đơn hiện tại.</p></div><Link href="/admin/orders">Xem tất cả</Link></div><div className="statusBreakdown">{Object.entries(d.orderStatusCounts||{}).map(([status,count])=>{const pct=Math.round(Number(count)/totalStatus*100);return <div className="statusRow" key={status}><div><span>{statusLabel[status]||status}</span><b>{count}</b></div><div className="statusTrack"><i style={{width:`${pct}%`}}/></div><small>{pct}%</small></div>})}</div></section>
  </div>

  <div className="dashboardSplit dashboardBottom">
   <section className="dashboardCard"><div className="dashboardCardHead"><div><h2>Sản phẩm bán chạy</h2><p>Theo số lượng ở các đơn đã giao/hoàn thành.</p></div><Link href="/admin/products">Sản phẩm</Link></div>{d.topProducts.length===0?<div className="dashboardEmpty">Chưa có dữ liệu bán hàng hoàn tất.</div>:<div className="topProductList">{d.topProducts.map((p,i)=><div className="topProductRow" key={p.productId}><span className="rank">{i+1}</span><div><b>{p.name}</b><span>{p.quantitySold} sản phẩm đã bán</span></div><strong>{money(p.revenue)}</strong></div>)}</div>}</section>
   <section className="dashboardCard"><div className="dashboardCardHead"><div><h2>Cảnh báo tồn kho</h2><p>Các sản phẩm cần nhập thêm sớm.</p></div><Link href="/admin/inventory">Quản lý kho</Link></div>{d.lowStockItems.length===0?<div className="dashboardEmpty">Không có sản phẩm dưới ngưỡng cảnh báo.</div>:<div className="lowStockList">{d.lowStockItems.map(i=><div className="lowStockRow" key={i.productId}><div><b>{i.productName}</b><span>Tồn: {i.quantityOnHand} · Giữ: {i.quantityReserved} · Ngưỡng: {i.lowStockThreshold}</span></div><strong>{i.availableQuantity}</strong></div>)}</div>}</section>
  </div>

  <div className="dashboardCard dashboardRecent"><div className="dashboardCardHead"><div><h2>Đơn hàng gần đây</h2><p>8 đơn mới nhất trong hệ thống.</p></div><Link href="/admin/orders">Quản lý đơn hàng</Link></div><div className="tableWrap flat"><table className="table"><thead><tr><th>Mã đơn</th><th>Người nhận</th><th>Ngày tạo</th><th>Trạng thái</th><th>Tổng tiền</th></tr></thead><tbody>{d.recentOrders.map(o=><tr key={o.orderId}><td><b>{o.orderCode}</b></td><td>{o.recipientName}</td><td>{new Date(o.createdAt).toLocaleString('vi-VN')}</td><td><Status value={o.status}/></td><td><b>{money(o.totalAmount)}</b></td></tr>)}{!loading&&d.recentOrders.length===0&&<tr><td colSpan={5} className="dashboardTableEmpty">Chưa có đơn hàng.</td></tr>}</tbody></table></div></div>
 </>;
}
