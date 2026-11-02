import React,{useCallback,useEffect,useState} from 'react';
import {Link,useNavigate,useParams} from 'react-router-dom';
import api from '../services/api';
import {formatCurrency,formatDate} from '../utils';
import {getOrderStatusOptions} from '../orderStatus';

function ErrorMessage({children}){return children?<div className="error" role="alert">{children}</div>:null;}
function Loading(){return <div className="loading" role="status">Loading administrator data…</div>;}
function Pagination({page,setPage,totalPages,totalElements}){
 return <div className="pagination-controls">
  <button className="secondary" disabled={page<=0} onClick={()=>setPage(value=>value-1)}>Previous</button>
  <span>Page {totalPages?page+1:0} of {totalPages} · {totalElements||0} records</span>
  <button className="secondary" disabled={page+1>=totalPages} onClick={()=>setPage(value=>value+1)}>Next</button>
 </div>;
}
function downloadCsv(response,filename){
 const blob=new Blob([response.data],{type:'text/csv;charset=utf-8'});
 const url=URL.createObjectURL(blob);
 const anchor=document.createElement('a');
 anchor.href=url;anchor.download=filename;anchor.click();
 URL.revokeObjectURL(url);
}
function AdminPageHeader({eyebrow,title,description,action}){
 return <header className="admin-page-header">
  <div><span className="eyebrow">{eyebrow}</span><h1>{title}</h1>{description&&<p className="muted">{description}</p>}</div>
  {action}
 </header>;
}

export function AdminOrdersPage(){
 const [filters,setFilters]=useState({status:'',orderId:'',email:'',from:'',to:''});
 const [applied,setApplied]=useState({});
 const [result,setResult]=useState(null),[page,setPage]=useState(0),[loading,setLoading]=useState(true),[error,setError]=useState(''),[exporting,setExporting]=useState(false);
 const load=useCallback(async()=>{
  setLoading(true);setError('');
  try{
   const params={page,size:20,...applied};
   const response=await api.get('/admin/orders',{params});
   setResult(response.data);
  }catch(e){setError(e.response?.data?.message||'Unable to load orders.');}
  finally{setLoading(false);}
 },[page,applied]);
 useEffect(()=>{load();},[load]);
 const apply=event=>{
  event.preventDefault();
  const params={};
  if(filters.status)params.status=filters.status;
  if(filters.orderId.trim())params.orderId=filters.orderId.trim();
  if(filters.email.trim())params.email=filters.email.trim();
  if(filters.from)params.from=new Date(filters.from).toISOString();
  if(filters.to)params.to=new Date(filters.to).toISOString();
  setPage(0);setApplied(params);
 };
 const exportOrders=async()=>{
  setExporting(true);setError('');
  try{const response=await api.get('/admin/orders/export.csv',{params:applied,responseType:'blob'});downloadCsv(response,'shopit-orders.csv');}
  catch(e){setError('Could not export the filtered order list.');}
  finally{setExporting(false);}
 };
 return <section className="section admin admin-page">
  <AdminPageHeader eyebrow="Owner workspace" title="Orders" description="Find orders, inspect delivery details, review the timeline and safely move each order forward." action={<button className="secondary" disabled={exporting} onClick={exportOrders}>{exporting?'Preparing CSV…':'Export CSV'}</button>}/>
  <form className="summary admin-filter-form" onSubmit={apply}>
   <label>Status<select value={filters.status} onChange={e=>setFilters({...filters,status:e.target.value})}><option value="">All statuses</option>{['PENDING_PAYMENT','CONFIRMED','PROCESSING','SHIPPED','DELIVERED','CANCELLED'].map(value=><option key={value}>{value}</option>)}</select></label>
   <label>Order ID<input inputMode="numeric" value={filters.orderId} onChange={e=>setFilters({...filters,orderId:e.target.value})} placeholder="e.g. 1042"/></label>
   <label>Customer email<input type="email" maxLength="190" value={filters.email} onChange={e=>setFilters({...filters,email:e.target.value})} placeholder="name@example.com"/></label>
   <label>From<input type="datetime-local" value={filters.from} onChange={e=>setFilters({...filters,from:e.target.value})}/></label>
   <label>To<input type="datetime-local" value={filters.to} onChange={e=>setFilters({...filters,to:e.target.value})}/></label>
   <div className="row-actions"><button className="primary">Apply filters</button><button type="button" className="secondary" onClick={()=>{setFilters({status:'',orderId:'',email:'',from:'',to:''});setPage(0);setApplied({});}}>Clear</button></div>
  </form>
  <ErrorMessage>{error}</ErrorMessage>
  {loading?<Loading/>:result?.content?.length?<div className="summary admin-orders">
   {result.content.map(order=><article className="admin-list-row" key={order.id}>
    <div><b>Order #{order.id}</b><small>{formatDate(order.createdAt)} · {order.status.replaceAll('_',' ')}</small><small>{order.shippingAddress}</small></div>
    <div><b>{formatCurrency(order.total)}</b><small>{order.items?.length||0} line items</small></div>
    <Link className="text-link" to={'/admin/orders/'+order.id}>Open order →</Link>
   </article>)}
   <Pagination page={page} setPage={setPage} totalPages={result.totalPages} totalElements={result.totalElements}/>
  </div>:<div className="empty"><h3>No orders match these filters.</h3><p>Change the filters or check again when a customer places an order.</p></div>}
 </section>;
}

export function AdminOrderDetailPage(){
 const {id}=useParams(),[order,setOrder]=useState(null),[loading,setLoading]=useState(true),[error,setError]=useState(''),[busy,setBusy]=useState(false),[next,setNext]=useState('');
 const load=useCallback(async()=>{
  setLoading(true);setError('');
  try{const response=await api.get('/admin/orders/'+id);setOrder(response.data);setNext(response.data.allowedNextStatuses?.[0]||'');}
  catch(e){setError(e.response?.data?.message||'Could not load this order.');}
  finally{setLoading(false);}
 },[id]);
 useEffect(()=>{load();},[load]);
 const updateStatus=async()=>{
  if(!next||!order)return;
  if(!window.confirm('Move order #'+order.id+' from '+order.status+' to '+next+'?'))return;
  setBusy(true);setError('');
  try{const response=await api.put('/admin/orders/'+id+'/status',{status:next});setOrder(response.data);setNext(response.data.allowedNextStatuses?.[0]||'');}
  catch(e){setError(e.response?.data?.message||'Order status update failed.');}
  finally{setBusy(false);}
 };
 return <section className="section admin admin-page">
  <Link className="back" to="/admin/orders">← All orders</Link>
  {loading?<Loading/>:order&&<>
   <AdminPageHeader eyebrow={'Order #'+order.id} title={order.status.replaceAll('_',' ')} description={'Placed '+formatDate(order.createdAt)+' · Total '+formatCurrency(order.total)}/>
   <ErrorMessage>{error}</ErrorMessage>
   <div className="admin-detail-grid">
    <div className="summary"><h2>Delivery address</h2><p>{order.shippingAddress}</p><h2>Items</h2>{order.items?.map((item,index)=><div className="analytics-list-row" key={index}><span>{item.productName}<small>{item.quantity} × {formatCurrency(item.unitPrice)}</small></span><b>{formatCurrency(item.lineTotal)}</b></div>)}</div>
    <div className="summary"><h2>Payment and totals</h2><div className="analytics-list-row"><span>Subtotal</span><b>{formatCurrency(order.subtotal)}</b></div><div className="analytics-list-row"><span>Discount {order.couponCode?'· '+order.couponCode:''}</span><b>−{formatCurrency(order.discountAmount)}</b></div><div className="analytics-list-row"><span>Shipping</span><b>{formatCurrency(order.shipping)}</b></div><div className="analytics-list-row"><span>Tax</span><b>{formatCurrency(order.tax)}</b></div><div className="analytics-list-row"><span>Total</span><b>{formatCurrency(order.total)}</b></div>
     <h2>Update status</h2>
     {order.allowedNextStatuses?.length?<div className="row-actions"><select aria-label="Next order status" value={next} onChange={e=>setNext(e.target.value)}>{order.allowedNextStatuses.map(value=><option key={value} value={value}>{value.replaceAll('_',' ')}</option>)}</select><button className="primary" disabled={busy||!next} onClick={updateStatus}>{busy?'Saving…':'Confirm status'}</button></div>:<p className="muted">No further status changes are allowed.</p>}
    </div>
   </div>
   <div className="summary admin-orders"><h2>Order timeline</h2>{order.timeline?.length?order.timeline.map((event,index)=><div className="timeline-row" key={index}><span className="timeline-dot"/><div><b>{event.newStatus.replaceAll('_',' ')}</b><small>{new Date(event.changedAt).toLocaleString()} · {event.actor}</small></div></div>):<p className="muted">No status history is available.</p>}</div>
  </>}
  {!loading&&!order&&!error&&<div className="empty"><h3>Order not found.</h3><Link to="/admin/orders">Return to orders</Link></div>}
 </section>;
}

export function AdminProductsPage(){
 const [filters,setFilters]=useState({q:'',active:''}),[applied,setApplied]=useState({}),[page,setPage]=useState(0),[result,setResult]=useState(null),[loading,setLoading]=useState(true),[error,setError]=useState('');
 const load=useCallback(async()=>{
  setLoading(true);setError('');
  try{const response=await api.get('/admin/products',{params:{page,size:20,...applied}});setResult(response.data);}
  catch(e){setError(e.response?.data?.message||'Could not load the product catalogue.');}
  finally{setLoading(false);}
 },[page,applied]);
 useEffect(()=>{load();},[load]);
 const toggle=async product=>{
  if(!window.confirm((product.active?'Deactivate':'Reactivate')+' '+product.name+'?'))return;
  try{await api.patch('/admin/products/'+product.id+'/status',{active:!product.active});await load();}
  catch(e){setError(e.response?.data?.message||'Could not change product status.');}
 };
 return <section className="section admin admin-page">
  <AdminPageHeader eyebrow="Catalogue management" title="Products" description="Manage product content, selling price, private unit cost and availability." action={<Link className="primary" to="/admin/products/new">Add product</Link>}/>
  <form className="admin-pagination-filters" onSubmit={e=>{e.preventDefault();setPage(0);setApplied({q:filters.q.trim()||undefined,active:filters.active===''?undefined:filters.active});}}>
   <input aria-label="Search products" placeholder="Search products" value={filters.q} onChange={e=>setFilters({...filters,q:e.target.value})}/>
   <select aria-label="Product status" value={filters.active} onChange={e=>setFilters({...filters,active:e.target.value})}><option value="">All products</option><option value="true">Active</option><option value="false">Inactive</option></select>
   <button className="primary">Search</button>
  </form>
  <ErrorMessage>{error}</ErrorMessage>
  {loading?<Loading/>:result?.content?.length?<div className="summary admin-orders">
   {result.content.map(product=><article className="admin-list-row" key={product.id}>
    <div className="admin-product-identity">{product.imageUrl&&<img src={product.imageUrl} alt=""/>}<span><b>{product.name}</b><small>{product.active?'Active':'Inactive'} · {product.stock} in stock</small></span></div>
    <div><b>{formatCurrency(product.price)}</b><small>Private cost {formatCurrency(product.costPrice??0)}</small></div>
    <div className="row-actions"><Link className="text-link" to={'/admin/products/'+product.id}>Edit</Link><button className="text-link" onClick={()=>toggle(product)}>{product.active?'Deactivate':'Reactivate'}</button></div>
   </article>)}
   <Pagination page={page} setPage={setPage} totalPages={result.totalPages} totalElements={result.totalElements}/>
  </div>:<div className="empty"><h3>No products found.</h3><Link className="primary" to="/admin/products/new">Create the first product</Link></div>}
 </section>;
}

const emptyProduct=()=>({name:'',description:'',price:'',costPrice:'0',stock:'0',imageUrl:'',categoryId:'',active:true});
export function AdminProductFormPage(){
 const {id}=useParams(),editing=Boolean(id),navigate=useNavigate();
 const [form,setForm]=useState(emptyProduct),[categories,setCategories]=useState([]),[loading,setLoading]=useState(editing),[busy,setBusy]=useState(false),[error,setError]=useState('');
 useEffect(()=>{
  Promise.all([api.get('/admin/categories'),editing?api.get('/admin/products/'+id):Promise.resolve({data:null})])
   .then(([categoryResponse,productResponse])=>{
    setCategories(categoryResponse.data||[]);
    if(productResponse.data){const product=productResponse.data;setForm({name:product.name,description:product.description||'',price:String(product.price),costPrice:String(product.costPrice??0),stock:String(product.stock),imageUrl:product.imageUrl||'',categoryId:product.categoryId?String(product.categoryId):'',active:product.active});}
   })
   .catch(e=>setError(e.response?.data?.message||'Could not load product details.'))
   .finally(()=>setLoading(false));
 },[id,editing]);
 const update=(key,value)=>setForm(current=>({...current,[key]:value}));
 const submit=async event=>{
  event.preventDefault();setBusy(true);setError('');
  const payload={...form,price:Number(form.price),costPrice:Number(form.costPrice||0),stock:Number(form.stock),categoryId:form.categoryId?Number(form.categoryId):null};
  try{if(editing)await api.put('/admin/products/'+id,payload);else await api.post('/admin/products',payload);navigate('/admin/products',{replace:true});}
  catch(e){setError(e.response?.data?.message||'Could not save the product.');}
  finally{setBusy(false);}
 };
 if(loading)return <section className="section admin"><Loading/></section>;
 return <section className="section admin admin-page">
  <Link className="back" to="/admin/products">← All products</Link>
  <AdminPageHeader eyebrow="Catalogue management" title={editing?'Edit product':'Create product'} description="Selling price and stock are validated and saved by the server."/>
  <ErrorMessage>{error}</ErrorMessage>
  <form className="summary form admin-product-form" onSubmit={submit}>
   <label>Product name<input required maxLength="180" value={form.name} onChange={e=>update('name',e.target.value)}/></label>
   <label>Description<textarea maxLength="10000" rows="5" value={form.description} onChange={e=>update('description',e.target.value)}/></label>
   <div className="admin-form-grid"><label>Selling price (INR)<input required type="number" min="0" max="9999999999.99" step=".01" value={form.price} onChange={e=>update('price',e.target.value)}/></label><label>Purchase cost / unit (INR)<input type="number" min="0" step=".01" value={form.costPrice} onChange={e=>update('costPrice',e.target.value)}/></label><label>Available stock<input required type="number" min="0" max="1000000" step="1" value={form.stock} onChange={e=>update('stock',e.target.value)}/></label><label>Category<select value={form.categoryId} onChange={e=>update('categoryId',e.target.value)}><option value="">No category</option>{categories.map(category=><option key={category.id} value={category.id}>{category.name} {category.active?'':'(Inactive)'}</option>)}</select></label></div>
   <label>Product image URL<input type="url" maxLength="1000" value={form.imageUrl} onChange={e=>update('imageUrl',e.target.value)} placeholder="https://…"/></label>
   {form.imageUrl&&<div className="admin-image-preview"><img src={form.imageUrl} alt="Product preview" onError={e=>{e.currentTarget.hidden=true;}}/><span>Image preview</span></div>}
   <label className="checkline"><input type="checkbox" checked={form.active} onChange={e=>update('active',e.target.checked)}/> Published and visible to customers</label>
   <div className="row-actions"><button className="primary" disabled={busy}>{busy?'Saving…':editing?'Save changes':'Create product'}</button><Link className="secondary" to="/admin/products">Cancel</Link></div>
  </form>
 </section>;
}

export function AdminCategoriesPage(){
 const [categories,setCategories]=useState([]),[form,setForm]=useState({id:null,name:'',description:''}),[loading,setLoading]=useState(true),[busy,setBusy]=useState(false),[error,setError]=useState('');
 const load=useCallback(async()=>{setLoading(true);setError('');try{const response=await api.get('/admin/categories');setCategories(response.data||[]);}catch(e){setError(e.response?.data?.message||'Could not load categories.');}finally{setLoading(false);}},[]);
 useEffect(()=>{load();},[load]);
 const submit=async event=>{
  event.preventDefault();setBusy(true);setError('');
  try{const body={name:form.name.trim(),description:form.description};if(form.id)await api.put('/admin/categories/'+form.id,body);else await api.post('/admin/categories',body);setForm({id:null,name:'',description:''});await load();}
  catch(e){setError(e.response?.data?.message||'Could not save category.');}
  finally{setBusy(false);}
 };
 const toggle=async category=>{
  if(!window.confirm((category.active?'Deactivate':'Reactivate')+' category '+category.name+'?'))return;
  try{await api.patch('/admin/categories/'+category.id+'/status',{active:!category.active});await load();}
  catch(e){setError(e.response?.data?.message||'Could not change category status.');}
 };
 return <section className="section admin admin-page">
  <AdminPageHeader eyebrow="Catalogue structure" title="Categories" description="Inactive categories and their products are hidden from the public catalogue. Product associations are retained."/>
  <ErrorMessage>{error}</ErrorMessage>
  <div className="admin-detail-grid"><form className="summary form" onSubmit={submit}>
   <h2>{form.id?'Rename category':'Create category'}</h2><label>Category name<input required maxLength="100" value={form.name} onChange={e=>setForm({...form,name:e.target.value})}/></label><label>Description<textarea maxLength="500" value={form.description} onChange={e=>setForm({...form,description:e.target.value})}/></label>
   <button className="primary" disabled={busy}>{busy?'Saving…':form.id?'Save category':'Create category'}</button>
   {form.id&&<button type="button" className="secondary" onClick={()=>setForm({id:null,name:'',description:''})}>Cancel edit</button>}
  </form>
  <div className="summary admin-orders"><h2>All categories</h2>
   {loading?<Loading/>:categories.length?categories.map(category=><article className="admin-list-row" key={category.id}>
    <div><b>{category.name}</b><small>{category.productCount} products · {category.active?'Active':'Inactive'}</small><small>{category.slug}</small></div>
    <div className="row-actions"><button className="text-link" onClick={()=>setForm({id:category.id,name:category.name,description:category.description||''})}>Rename</button><button className="text-link" onClick={()=>toggle(category)}>{category.active?'Deactivate':'Reactivate'}</button></div>
   </article>):<p className="muted">No categories have been created.</p>}
  </div></div>
 </section>;
}

export function AdminCustomersPage(){
 const [q,setQ]=useState(''),[applied,setApplied]=useState(''),[page,setPage]=useState(0),[result,setResult]=useState(null),[loading,setLoading]=useState(true),[error,setError]=useState('');
 const load=useCallback(async()=>{setLoading(true);setError('');try{const response=await api.get('/admin/customers',{params:{q:applied||undefined,page,size:20}});setResult(response.data);}catch(e){setError(e.response?.data?.message||'Could not load customers.');}finally{setLoading(false);}},[page,applied]);
 useEffect(()=>{load();},[load]);
 const toggle=async customer=>{
  if(!window.confirm((customer.enabled?'Disable':'Enable')+' customer '+customer.email+'?'))return;
  try{await api.put('/admin/customers/'+customer.id+'/status',{enabled:!customer.enabled});await load();}
  catch(e){setError(e.response?.data?.message||'Could not change customer access.');}
 };
 return <section className="section admin admin-page">
  <AdminPageHeader eyebrow="Customer accounts" title="Customers" description="Search customer accounts and review order history. Administrator accounts are never listed here."/>
  <form className="admin-pagination-filters" onSubmit={event=>{event.preventDefault();setPage(0);setApplied(q.trim());}}><input aria-label="Search customers" maxLength="120" value={q} onChange={e=>setQ(e.target.value)} placeholder="Name, email or phone"/><button className="primary">Search</button></form>
  <ErrorMessage>{error}</ErrorMessage>
  {loading?<Loading/>:result?.content?.length?<div className="summary admin-orders">
   {result.content.map(customer=><article className="admin-list-row" key={customer.id}>
    <div><b>{customer.name}</b><small>{customer.email} · {customer.phone||'No phone'}</small><small>{customer.enabled?'Enabled':'Disabled'} · Joined {formatDate(customer.createdAt)}</small></div>
    <div className="row-actions"><Link className="text-link" to={'/admin/customers/'+customer.id}>Details and orders</Link><button className="text-link" onClick={()=>toggle(customer)}>{customer.enabled?'Disable':'Enable'}</button></div>
   </article>)}
   <Pagination page={page} setPage={setPage} totalPages={result.totalPages} totalElements={result.totalElements}/>
  </div>:<div className="empty"><h3>No customers found.</h3><p>Try another search.</p></div>}
 </section>;
}
