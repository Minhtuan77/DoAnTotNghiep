const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api";
export async function api<T>(path:string, init:RequestInit={}) : Promise<T> {
  const token = typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;
  const headers = new Headers(init.headers);
  if (!headers.has("Content-Type") && init.body) headers.set("Content-Type","application/json");
  if (token) headers.set("Authorization",`Bearer ${token}`);
  const res = await fetch(`${API_URL}${path}`,{...init,headers,cache:"no-store"});
  if(!res.ok){let message=`HTTP ${res.status}`;try{const e=await res.json();message=e.message??message}catch{}throw new Error(message)}
  if(res.status===204) return undefined as T;
  return res.json() as Promise<T>;
}
