'use client';
import Link from 'next/link';
import {useEffect,useState} from 'react';
import {api} from '@/lib/api';
import type {Page,Product,Category} from '@/lib/types';
import {ProductCard,ErrorBox,Icon} from '@/components/UI';

export default function Home(){
 const [p,setP]=useState<Product[]>([]),[count,setCount]=useState(0),[c,setC]=useState<Category[]>([]),[e,setE]=useState('');
 useEffect(()=>{Promise.all([api.products('?size=8&sortBy=createdAt&sortDir=desc') as Promise<Page<Product>>,api.categories() as Promise<Category[]>]).then(([x,y])=>{setP(x.content);setCount(x.totalElements);setC(y)}).catch(x=>setE(x.message))},[]);
 return <>
  <section className="hero">
   <div><span className="heroKicker"><Icon name="shield" size={16}/> DỮ LIỆU ĐỒNG BỘ TỪ HỆ THỐNG</span><h1>Đồ gia dụng phù hợp cho từng không gian sống.</h1><p>Khám phá sản phẩm theo danh mục, thương hiệu, giá và tình trạng tồn kho. Thông tin hiển thị được lấy từ backend của dự án, không sử dụng danh sách sản phẩm giả.</p><div className="heroActions"><Link className="primary" href="/products">Khám phá sản phẩm <Icon name="arrow" size={18}/></Link><Link className="secondary" href="/search"><Icon name="search" size={18}/> Tìm sản phẩm</Link></div></div>
   <div className="heroBadge"><strong>{count||'…'}</strong><span>Sản phẩm hiện có trong catalog</span></div>
  </section>
  <div className="trustStrip"><div className="trustItem"><Icon name="shield"/><div><b>Dữ liệu minh bạch</b><span>Giá và tồn kho lấy từ backend</span></div></div><div className="trustItem"><Icon name="truck"/><div><b>Luồng mua hàng rõ ràng</b><span>Giỏ hàng → thanh toán → đơn hàng</span></div></div><div className="trustItem"><Icon name="tag"/><div><b>Voucher theo điều kiện thật</b><span>Được backend kiểm tra trước khi áp dụng</span></div></div></div>
  <section className="container"><div className="sectionHead"><div><div className="eyebrow">Mua theo nhu cầu</div><h2>Danh mục sản phẩm</h2><p>{c.length?`${c.length} danh mục hiện đang được cung cấp từ hệ thống`:'Đang tải danh mục từ hệ thống'}</p></div><Link href="/products">Xem tất cả</Link></div><div className="grid categoryGrid">{c.map(x=><Link key={x.id} href={`/products?categoryId=${x.id}`} className="categoryCard"><div className="categoryIcon"><Icon name="category"/></div><div><b>{x.name}</b><p className="muted smallText">{x.description||'Xem các sản phẩm trong danh mục'}</p></div><div className="categoryArrow"><span>Khám phá</span><Icon name="arrow" size={16}/></div></Link>)}</div></section>
  <section className="container"><div className="sectionHead"><div><div className="eyebrow">Catalog</div><h2>Sản phẩm mới cập nhật</h2><p>Hiển thị trực tiếp theo dữ liệu sản phẩm hiện có.</p></div><Link href="/products">Xem tất cả</Link></div>{e&&<ErrorBox message={e}/>}<div className="grid productsGrid">{p.map(x=><ProductCard key={x.id} p={x}/>)}</div></section>
 </>
}
