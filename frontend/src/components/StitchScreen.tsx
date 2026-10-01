"use client";
export default function StitchScreen({screen,title}:{screen:string;title:string}){
  return <iframe title={title} src={`/stitch/${screen}/index.html`} style={{position:"fixed",inset:0,width:"100vw",height:"100vh",border:0,background:"#f7f9fb"}} />;
}
