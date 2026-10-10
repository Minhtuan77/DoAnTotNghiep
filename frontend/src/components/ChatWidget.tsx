'use client';
import {useEffect,useRef,useState} from 'react';
import {api,getBehaviorSessionId} from '@/lib/api';
import type {ChatMessage} from '@/lib/types';

export default function ChatWidget(){
 const [open,setOpen]=useState(false),[conversationId,setConversationId]=useState<number|null>(null),[messages,setMessages]=useState<ChatMessage[]>([]),[text,setText]=useState(''),[busy,setBusy]=useState(false),[status,setStatus]=useState('OPEN');
 const endRef=useRef<HTMLDivElement|null>(null);
 useEffect(()=>{if(open)endRef.current?.scrollIntoView({behavior:'smooth'})},[open,messages]);
 async function send(){const value=text.trim();if(!value||busy)return;setText('');setMessages(m=>[...m,{senderType:'CUSTOMER',messageText:value} as ChatMessage]);setBusy(true);try{const r:any=await api.chat({conversationId,message:value,sessionId:getBehaviorSessionId()});setConversationId(r.conversationId);setStatus(r.status);setMessages(m=>[...m,r.reply]);}catch(e:any){setMessages(m=>[...m,{senderType:'BOT',messageText:`Không thể kết nối chatbot: ${e.message}`} as ChatMessage])}finally{setBusy(false)}}
 async function closeConversation(){if(conversationId)try{await api.closeChat(conversationId);setStatus('CLOSED')}catch{}setConversationId(null);setMessages([]);setStatus('OPEN');setOpen(false)}
 return <div className="chatWidget"><button className="chatLauncher" onClick={()=>setOpen(v=>!v)} aria-label={open?'Đóng chatbot':'Mở chatbot AI'}>{open?'×':'AI'}</button>{open&&<section className="chatPanel" aria-label="Chatbot hỗ trợ khách hàng"><div className="chatHeader"><div><b>Trợ lý Nhà Tiện Nghi</b><span>{status==='ESCALATED'?'Cần nhân viên hỗ trợ':'Tư vấn theo dữ liệu hệ thống'}</span></div><button className="ghost" onClick={closeConversation}>Đóng</button></div><div className="chatMessages">{messages.length===0&&<div className="chatWelcome"><b>Xin chào!</b><p>Mình có thể hỗ trợ tìm sản phẩm, kiểm tra giá/tồn kho, tra cứu đơn hàng của bạn và giải đáp thanh toán/voucher.</p></div>}{messages.map((m,i)=><div key={m.messageId||i} className={`chatBubble ${m.senderType==='CUSTOMER'?'mine':'bot'}`}>{m.messageText}</div>)}{busy&&<div className="chatBubble bot">Đang kiểm tra dữ liệu...</div>}<div ref={endRef}/></div><div className="chatInput"><textarea value={text} onChange={e=>setText(e.target.value)} onKeyDown={e=>{if(e.key==='Enter'&&!e.shiftKey){e.preventDefault();send()}}} placeholder="Ví dụ: Tôi cần nồi cho gia đình 4 người..." rows={2}/><button className="primary" onClick={send} disabled={busy||!text.trim()}>Gửi</button></div></section>}</div>
}
