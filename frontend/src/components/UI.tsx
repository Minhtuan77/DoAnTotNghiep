'use client';
import Link from 'next/link';
import type {Product} from '@/lib/types';
import {api,isLoggedIn} from '@/lib/api';

export const money=(n?:number|null)=>new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(Number(n||0));
export const img=(p:Product)=>p.images?.find(x=>x.isPrimary)?.imageUrl||p.images?.[0]?.imageUrl||'/placeholder.svg';

export function Icon({name,size=20}:{name:'cart'|'heart'|'search'|'user'|'box'|'arrow'|'trash'|'menu'|'close'|'shield'|'truck'|'tag'|'grid'|'package'|'orders'|'users'|'inventory'|'review'|'voucher'|'category';size?:number}){
 const p:{[k:string]:React.ReactNode}={
  cart:<><circle cx="9" cy="20" r="1"/><circle cx="19" cy="20" r="1"/><path d="M3 4h2l2.4 10.2a2 2 0 0 0 2 1.5h7.8a2 2 0 0 0 2-1.6L21 8H7"/></>,
  heart:<path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8L12 21l8.9-8.6a5.5 5.5 0 0 0-.1-7.8Z"/>,
  search:<><circle cx="11" cy="11" r="7"/><path d="m20 20-3.6-3.6"/></>,
  user:<><circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/></>,
  box:<><path d="m21 8-9 5-9-5 9-5 9 5Z"/><path d="m3 8 9 5 9-5v9l-9 5-9-5V8Z"/><path d="M12 13v9"/></>,
  arrow:<><path d="M5 12h14"/><path d="m13 6 6 6-6 6"/></>,
  trash:<><path d="M3 6h18"/><path d="M8 6V4h8v2"/><path d="m19 6-1 14H6L5 6"/></>,
  menu:<><path d="M4 7h16"/><path d="M4 12h16"/><path d="M4 17h16"/></>,
  close:<><path d="m6 6 12 12"/><path d="m18 6-12 12"/></>,
  shield:<><path d="M12 3 4 6v5c0 5 3.4 8.6 8 10 4.6-1.4 8-5 8-10V6l-8-3Z"/><path d="m9 12 2 2 4-4"/></>,
  truck:<><path d="M3 6h11v10H3z"/><path d="M14 10h4l3 3v3h-7z"/><circle cx="7" cy="18" r="2"/><circle cx="18" cy="18" r="2"/></>,
  tag:<><path d="M20 12 12 20 4 12V4h8l8 8Z"/><circle cx="9" cy="9" r="1"/></>,
  grid:<><rect x="4" y="4" width="6" height="6"/><rect x="14" y="4" width="6" height="6"/><rect x="4" y="14" width="6" height="6"/><rect x="14" y="14" width="6" height="6"/></>,
  package:<><path d="m21 8-9 5-9-5 9-5 9 5Z"/><path d="m3 8 9 5 9-5v9l-9 5-9-5V8Z"/></>,
  orders:<><path d="M6 3h12v18H6z"/><path d="M9 8h6M9 12h6M9 16h4"/></>,
  users:<><circle cx="9" cy="8" r="3"/><circle cx="17" cy="9" r="2"/><path d="M3 20a6 6 0 0 1 12 0M14 16a5 5 0 0 1 7 4"/></>,
  inventory:<><path d="M4 7h16v13H4z"/><path d="M8 7V4h8v3M9 12h6"/></>,
  review:<><path d="M4 4h16v12H8l-4 4V4Z"/><path d="M8 9h8M8 12h5"/></>,
  voucher:<><path d="M4 7h16v10H4z"/><path d="M9 7v10M15 7v10"/></>,
  category:<><path d="M4 5h7v6H4zM13 5h7v6h-7zM4 13h7v6H4zM13 13h7v6h-7z"/></>
 };
 return <svg aria-hidden="true" viewBox="0 0 24 24" width={size} height={size} fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">{p[name]}</svg>
}

export function Empty({title,text}:{title:string;text?:string}){return <div className="empty"><div className="emptyVisual"><Icon name="box" size={34}/></div><h3>{title}</h3>{text&&<p>{text}</p>}</div>}
export function ErrorBox({message}:{message:string}){return <div className="errorBox" role="alert"><b>Không thể hoàn tất thao tác.</b><span>{message}</span></div>}

export function ProductCard({p}:{p:Product}){
 async function add(e:React.MouseEvent){e.preventDefault();e.stopPropagation();if(!isLoggedIn()){location.href='/login';return}try{await api.addCart(p.id,1);alert('Đã thêm sản phẩm vào giỏ hàng')}catch(e:any){alert(e.message)}}
 const sale=p.salePrice&&p.salePrice<p.price;
 return <article className="productCard"><Link className="productCardLink" href={`/products/${p.id}`} aria-label={`Xem ${p.name}`}><div className="productImg"><img src={img(p)} alt={p.name}/>{sale&&<span className="sale">-{Math.round((1-Number(p.salePrice)/p.price)*100)}%</span>}<span className={`stockBadge ${p.stockQuantity>0?'inStock':'outStock'}`}>{p.stockQuantity>0?'Còn hàng':'Hết hàng'}</span></div><div className="productBody"><div className="eyebrow">{p.brand?.name||p.category?.name||'Đồ gia dụng'}</div><h3>{p.name}</h3><div className="rating"><span aria-hidden="true">★</span> {Number(p.ratingAverage||0).toFixed(1)} <span className="muted">({p.reviewCount||0})</span></div><div className="priceLine"><b>{money(p.salePrice||p.price)}</b>{sale&&<del>{money(p.price)}</del>}</div></div></Link><div className="productAction"><button disabled={p.stockQuantity<=0} className="primary full" onClick={add}><Icon name="cart" size={18}/> Thêm vào giỏ</button></div></article>
}
export function Status({value}:{value:string}){const map:any={ACTIVE:'Hoạt động',INACTIVE:'Tạm ẩn',OUT_OF_STOCK:'Hết hàng',DISCONTINUED:'Ngừng bán',PENDING_CONFIRMATION:'Chờ xác nhận',CONFIRMED:'Đã xác nhận',SHIPPING:'Đang giao',DELIVERED:'Đã giao',COMPLETED:'Hoàn thành',CANCELLED:'Đã hủy',RETURNED:'Hoàn trả',VISIBLE:'Hiển thị',HIDDEN:'Đã ẩn',PENDING:'Chờ duyệt',LOCKED:'Đã khóa',UNVERIFIED:'Chưa xác minh'};return <span className={`status s-${value}`}>{map[value]||value}</span>}
