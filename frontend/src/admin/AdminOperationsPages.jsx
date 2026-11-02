import React,{useEffect,useState} from 'react';
import {Link} from 'react-router-dom';
import api from '../services/api';
import {formatCurrency,formatDate} from '../utils';
import {getFreeShippingThreshold} from '../pricing';

const FREE_SHIPPING_THRESHOLD=getFreeShippingThreshold();

export function AdminCoupons(){
 const blank=()=>({id:null,code:'',description:'',discountType:'PERCENT',discountValue:'10',minimumOrderAmount:'0',maximumDiscount:'',startsAt:new Date(Date.now()-new Date().getTimezoneOffset()*60000).toISOString().slice(0,16),endsAt:new Date(Date.now()+7*86400000-new Date().getTimezoneOffset()*60000).toISOString().slice(0,16),usageLimit:''});
 const [form,setForm]=useState(blank),[couponResult,setCouponResult]=useState(null),[tab,setTab]=useState('Active'),[couponPage,setCouponPage]=useState(0),[couponLoading,setCouponLoading]=useState(true),[error,setError]=useState(''),[message,setMessage]=useState(''),[busy,setBusy]=useState(false),[previewSubtotal,setPreviewSubtotal]=useState('1500');
 const coupons=couponResult?.content||[];
 const refresh=async()=>{
  setCouponLoading(true);
  try{const response=await api.get('/admin/coupons/paged',{params:{bucket:tab.toUpperCase(),page:couponPage,size:20}});setCouponResult(response.data);}
  catch(e){setError(e.response?.data?.message||'Could not load coupon management.');}
  finally{setCouponLoading(false);}
 };
 useEffect(()=>{refresh();},[tab,couponPage]);
 const tabs=['Active','Scheduled','Expired','Paused'];
 const visible=coupons;
 const subtotal=Math.max(0,Number(previewSubtotal)||0),minimum=Number(form.minimumOrderAmount||0);
 const eligible=subtotal>=minimum&&subtotal>0;
 const rawDiscount=form.discountType==='PERCENT'?subtotal*Number(form.discountValue||0)/100:Number(form.discountValue||0);
 const discount=eligible?Math.min(subtotal,form.maximumDiscount?Number(form.maximumDiscount):Infinity,rawDiscount):0;
 const afterDiscount=Math.max(0,subtotal-discount);
 const shipping=afterDiscount>=FREE_SHIPPING_THRESHOLD?0:49;
 const tax=Math.round(afterDiscount*.18*100)/100;
 const total=afterDiscount+shipping+tax;
 const submit=async e=>{
  e.preventDefault();setBusy(true);setError('');setMessage('');
  try{
   const payload={code:form.code.trim().toUpperCase(),description:form.description,discountType:form.discountType,discountValue:Number(form.discountValue),minimumOrderAmount:Number(form.minimumOrderAmount||0),maximumDiscount:form.maximumDiscount?Number(form.maximumDiscount):null,startsAt:new Date(form.startsAt).toISOString(),endsAt:new Date(form.endsAt).toISOString(),usageLimit:form.usageLimit?Number(form.usageLimit):null};
   if(form.id)await api.put('/admin/coupons/'+form.id,payload);else await api.post('/admin/coupons',payload);
   setForm(blank());setMessage('Coupon saved. Its availability is controlled by the start/end time and usage limit.');await refresh();
  }catch(e){setError(e.response?.data?.message||'Could not save coupon. Check the date range and discount values.');}
  finally{setBusy(false);}
 };
 const edit=o=>{
  const local=v=>{const d=new Date(v),x=new Date(d.getTime()-d.getTimezoneOffset()*60000);return x.toISOString().slice(0,16);};
  setForm({id:o.id,code:o.code,description:o.description||'',discountType:o.discountType,discountValue:String(o.discountValue),minimumOrderAmount:String(o.minimumOrderAmount??0),maximumDiscount:o.maximumDiscount?String(o.maximumDiscount):'',startsAt:local(o.startsAt),endsAt:local(o.endsAt),usageLimit:o.usageLimit?String(o.usageLimit):''});
 };
 const toggle=async o=>{
  const next=!o.active;
  if(!window.confirm((next?'Activate':'Pause')+' coupon '+o.code+'?'))return;
  setError('');
  try{await api.put('/admin/coupons/'+o.id+'/status',{active:next});await refresh();}
  catch(e){setError(e.response?.data?.message||'Could not change coupon status.');}
 };
 return <section className="section admin admin-page">
  <Link className="back" to="/admin">← Owner dashboard</Link>
  <span className="eyebrow">Promotions and campaigns</span><h1>Discount coupons</h1>
  <p className="muted">Create and schedule offers, pause codes and track redemptions. Coupon values are checked again by the server at checkout.</p>
  {error&&<div className="error" role="alert">{error}</div>}{message&&<div className="notice" role="status">{message}</div>}
  <div className="coupon-admin-layout">
   <form className="summary form" onSubmit={submit}>
    <h2>{form.id?'Edit coupon':'Create a coupon'}</h2>
    <label>Coupon code<input required maxLength="40" pattern="[A-Za-z0-9_-]{3,40}" value={form.code} onChange={e=>setForm({...form,code:e.target.value.toUpperCase()})} placeholder="WELCOME10"/></label>
    <label>Customer-facing description<input maxLength="240" value={form.description} onChange={e=>setForm({...form,description:e.target.value})} placeholder="10% off your first order"/></label>
    <label>Discount type<select value={form.discountType} onChange={e=>setForm({...form,discountType:e.target.value})}><option value="PERCENT">Percentage discount</option><option value="FIXED">Fixed amount discount</option></select></label>
    <label>Discount value<input required type="number" min=".01" max={form.discountType==='PERCENT'?100:undefined} step=".01" value={form.discountValue} onChange={e=>setForm({...form,discountValue:e.target.value})}/></label>
    <label>Minimum cart value (INR)<input type="number" min="0" step=".01" value={form.minimumOrderAmount} onChange={e=>setForm({...form,minimumOrderAmount:e.target.value})}/></label>
    <label>Maximum discount cap (INR, optional)<input type="number" min=".01" step=".01" value={form.maximumDiscount} onChange={e=>setForm({...form,maximumDiscount:e.target.value})}/></label>
    <label>Starts at<input required type="datetime-local" value={form.startsAt} onChange={e=>setForm({...form,startsAt:e.target.value})}/></label>
    <label>Expires at<input required type="datetime-local" value={form.endsAt} onChange={e=>setForm({...form,endsAt:e.target.value})}/></label>
    <label>Maximum total redemptions (blank = unlimited)<input type="number" min="1" step="1" value={form.usageLimit} onChange={e=>setForm({...form,usageLimit:e.target.value})}/></label>
    <button className="primary" disabled={busy}>{busy?'Saving…':form.id?'Save coupon':'Publish coupon'}</button>
    {form.id&&<button type="button" className="secondary" onClick={()=>setForm(blank())}>Cancel edit</button>}
    <div className="coupon-live-preview"><h3>Live discount preview</h3><label>Example cart value (INR)<input type="number" min="0" step=".01" value={previewSubtotal} onChange={e=>setPreviewSubtotal(e.target.value)}/></label>
     <div className="analytics-list-row"><span>Discount</span><b>{formatCurrency(discount)}</b></div>
     {!eligible&&<small className="muted">Minimum cart value is not met; this coupon would not apply.</small>}
     <div className="analytics-list-row"><span>Shipping</span><b>{formatCurrency(shipping)}</b></div>
     <div className="analytics-list-row"><span>Tax · 18%</span><b>{formatCurrency(tax)}</b></div>
     <div className="analytics-list-row"><span>Estimated total</span><b>{formatCurrency(total)}</b></div>
     <small className="muted">Illustrative preview only. Checkout uses the server's current pricing and validates eligibility again.</small>
    </div>
   </form>
   <div className="summary"><h2>Coupon register</h2>
    <div className="coupon-tabs" role="tablist" aria-label="Coupon status">{tabs.map(value=><button type="button" role="tab" aria-selected={tab===value} className={tab===value?'active':''} key={value} onClick={()=>{setTab(value);setCouponPage(0);}}>{value}{tab===value&&couponResult&&<span>{couponResult.totalElements}</span>}</button>)}</div>
    {couponLoading?<div className="loading" role="status">Loading coupons…</div>:visible.length?visible.map(o=>{
     const percent=o.usageLimit?Math.min(100,o.usageCount/o.usageLimit*100):null;
     return <article className="coupon-row" key={o.id}>
      <div><b>{o.code}</b><small>{o.discountType==='PERCENT'?o.discountValue+'% off':formatCurrency(o.discountValue)+' off'} · {formatDate(o.startsAt)} – {formatDate(o.endsAt)}</small>
       <span className={'status '+(tab==='Active'?'confirmed':'pending_payment')}>{tab}</span>
       <div className="coupon-usage"><div><span>{o.usageCount} {o.usageLimit?'/ '+o.usageLimit:'uses · no cap'}</span>{percent!==null&&<b>{Math.round(percent)}%</b>}</div>{percent!==null&&<div className="coupon-usage-track"><span style={{width:percent+'%'}}/></div>}</div>
      </div>
      <div className="row-actions"><button className="text-link" onClick={()=>edit(o)}>Edit</button>{tab==='Expired'?<span className="muted">Ended</span>:<button className="text-link" onClick={()=>toggle(o)}>{o.active?'Pause':'Activate'}</button>}</div>
     </article>;
    }):<p className="muted">There are no {tab.toLowerCase()} coupons.</p>}
    <div className="pagination-controls"><button className="secondary" disabled={couponPage<=0} onClick={()=>setCouponPage(value=>value-1)}>Previous</button><span>Page {couponResult?.totalPages?couponPage+1:0} of {couponResult?.totalPages||0} · {couponResult?.totalElements||0} coupons</span><button className="secondary" disabled={!couponResult||couponPage+1>=couponResult.totalPages} onClick={()=>setCouponPage(value=>value+1)}>Next</button></div>
   </div>
  </div>
 </section>;
}
export function AdminInventory(){
 const [data,setData]=useState(null),[products,setProducts]=useState([]),[error,setError]=useState(''),[loading,setLoading]=useState(true);
 const [page,setPage]=useState(0),[totalPages,setTotalPages]=useState(0),[totalElements,setTotalElements]=useState(0);
 const [threshold,setThreshold]=useState(''),[appliedThreshold,setAppliedThreshold]=useState(''),[sort,setSort]=useState('stock'),[direction,setDirection]=useState('asc');
 const [draftStock,setDraftStock]=useState({}),[savingStock,setSavingStock]=useState({}),[exporting,setExporting]=useState(false);
 const load=async()=>{
  setLoading(true);setError('');
  try{
   const [dashboard,inventory]=await Promise.all([
    api.get('/admin/dashboard'),
    api.get('/admin/inventory',{params:{page,size:20,threshold:appliedThreshold===''?undefined:Number(appliedThreshold),sort,direction}})
   ]);
   setData(dashboard.data);setProducts(inventory.data.content||[]);setTotalPages(inventory.data.totalPages||0);setTotalElements(inventory.data.totalElements||0);
  }catch(e){setError(e.response?.data?.message||'Could not load inventory.');}
  finally{setLoading(false);}
 };
 useEffect(()=>{load();},[page,appliedThreshold,sort,direction]);
 const applyFilter=event=>{event.preventDefault();setPage(0);setAppliedThreshold(threshold.trim());};
 const saveStock=async product=>{
  const value=Number(draftStock[product.id]??product.stock);
  if(!Number.isInteger(value)||value<0||value>1000000){setError('Stock must be a whole number from 0 to 1,000,000.');return;}
  setSavingStock(current=>({...current,[product.id]:true}));setError('');
  try{
   const response=await api.patch('/admin/products/'+product.id+'/stock',{stock:value});
   setProducts(current=>current.map(item=>item.id===product.id?response.data:item));
   setDraftStock(current=>{const next={...current};delete next[product.id];return next;});
  }catch(e){setError(e.response?.data?.message||'Stock update failed.');}
  finally{setSavingStock(current=>({...current,[product.id]:false}));}
 };
 const exportCsv=async()=>{
  setExporting(true);setError('');
  try{
   const response=await api.get('/admin/inventory/export.csv',{params:{threshold:appliedThreshold===''?undefined:Number(appliedThreshold),sort,direction},responseType:'blob'});
   const url=URL.createObjectURL(new Blob([response.data],{type:'text/csv;charset=utf-8'}));
   const anchor=document.createElement('a');anchor.href=url;anchor.download='shopit-inventory.csv';anchor.click();URL.revokeObjectURL(url);
  }catch{setError('Could not export inventory.');}
  finally{setExporting(false);}
 };
 return <section className="section admin admin-page">
  <Link className="back" to="/admin">← Owner dashboard</Link>
  <span className="eyebrow">Stock operations</span><h1>Inventory</h1>
  <p className="muted">Unit cost and stock quantities are private administrator data. Quick edits are saved through the server.</p>
  {error&&<div className="error" role="alert">{error}</div>}
  {data&&<div className="stats budget-stats">{[['Inventory cost budget',formatCurrency(data.inventoryCostBudget)],['Potential retail value',formatCurrency(data.potentialRetailValue)],['Estimated gross margin potential',formatCurrency(data.estimatedGrossMargin)]].map(([label,value])=><div className="stat" key={label}><span>{label}</span><b>{value}</b></div>)}</div>}
  <form className="summary admin-inventory-filters" onSubmit={applyFilter}>
   <label>Stock at or below<input aria-label="Stock threshold" type="number" min="0" max="1000000" step="1" value={threshold} onChange={e=>setThreshold(e.target.value)} placeholder="All stock levels"/></label>
   <label>Sort by<select value={sort} onChange={e=>{setSort(e.target.value);setPage(0);}}><option value="stock">Stock quantity</option><option value="name">Product name</option><option value="createdAt">Created date</option></select></label>
   <label>Direction<select value={direction} onChange={e=>{setDirection(e.target.value);setPage(0);}}><option value="asc">Ascending</option><option value="desc">Descending</option></select></label>
   <div className="row-actions"><button className="primary">Apply</button><button type="button" className="secondary" disabled={exporting} onClick={exportCsv}>{exporting?'Preparing…':'Export CSV'}</button></div>
  </form>
  <div className="summary admin-orders"><h2>Stock register</h2>
   {loading?<div className="loading" role="status">Loading inventory…</div>:products.length?products.map(product=><div className="inventory-row" key={product.id}>
    <div><b>{product.name}</b><small>{product.active?'Active':'Inactive'} · {product.categoryName||'Uncategorised'}</small></div>
    <div><small>Unit cost</small><b>{formatCurrency(product.costPrice??0)}</b></div>
    <div><small>Selling price</small><b>{formatCurrency(product.price)}</b></div>
    <div><small>Stock quantity</small><input className="stock-quick-edit" aria-label={'Stock for '+product.name} type="number" min="0" max="1000000" step="1" value={draftStock[product.id]??product.stock} onChange={e=>setDraftStock(current=>({...current,[product.id]:e.target.value}))}/></div>
    <div className="inventory-stock-action"><small>Stock value</small><b>{formatCurrency(Number(product.costPrice??0)*product.stock)}</b><button className="text-link" disabled={savingStock[product.id]||Number(draftStock[product.id]??product.stock)===product.stock} onClick={()=>saveStock(product)}>{savingStock[product.id]?'Saving…':'Save stock'}</button></div>
   </div>):<p className="muted">No products match this stock threshold.</p>}
   {!loading&&<div className="pagination-controls"><button className="secondary" disabled={page<=0} onClick={()=>setPage(value=>value-1)}>Previous</button><span>Page {totalPages?page+1:0} of {totalPages} · {totalElements} products</span><button className="secondary" disabled={page+1>=totalPages} onClick={()=>setPage(value=>value+1)}>Next</button></div>}
  </div>
  <p className="muted">Estimated margin assumes all current stock sells at the current list price. It excludes coupons, tax, shipping subsidies, payment fees, returns and operating expenses; it is not accounting profit.</p>
 </section>;
}
