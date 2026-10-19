'use client';

import {FormEvent, useState} from 'react';
import {api} from '@/lib/api';
import type {Product} from '@/lib/types';
import {ProductCard, Empty, ErrorBox} from '@/components/UI';

export default function Search(){
  const [q,setQ]=useState('');
  const [items,setItems]=useState<Product[]>([]);
  const [done,setDone]=useState(false);
  const [loading,setLoading]=useState(false);
  const [error,setError]=useState('');

  async function go(e:FormEvent<HTMLFormElement>){
    e.preventDefault();
    const keyword=q.trim();
    if(!keyword)return;

    setLoading(true);
    setError('');
    setDone(false);
    try{
      const params=new URLSearchParams({size:'100',keyword});
      const x:any=await api.products(`?${params.toString()}`);
      setItems(x?.content||[]);
      setDone(true);
    }catch(err:any){
      setItems([]);
      setDone(true);
      setError(err?.message||'Không thể tìm kiếm sản phẩm. Vui lòng thử lại.');
    }finally{
      setLoading(false);
    }
  }

  return <div className="container">
    <div className="sectionHead"><div><h1>Tìm kiếm sản phẩm</h1><p>Tìm trực tiếp trên catalog backend.</p></div></div>
    <form onSubmit={go} className="toolbar">
      <input className="input" style={{maxWidth:600}} value={q} onChange={e=>setQ(e.target.value)} placeholder="Nhập tên hoặc từ khóa sản phẩm" required/>
      <button className="primary" disabled={loading}>{loading?'Đang tìm...':'Tìm kiếm'}</button>
    </form>
    {error&&<ErrorBox message={error}/>}
    {!loading&&items.length>0&&<><div className="sectionHead" style={{marginTop:18}}><div><p>Tìm thấy {items.length} sản phẩm</p></div></div><div className="grid productsGrid">{items.map(x=><ProductCard key={x.id} p={x}/>)}</div></>}
    {!loading&&done&&!error&&items.length===0&&<Empty title="Không tìm thấy sản phẩm" text="Hãy thử từ khóa khác hoặc kiểm tra lại tên sản phẩm."/>}
  </div>
}
