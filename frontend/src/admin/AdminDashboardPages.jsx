import React,{useEffect,useState} from 'react';
import {Link,useParams} from 'react-router-dom';
import api from '../services/api';
import {formatCurrency,formatDate} from '../utils';

function AdminAnalytics({data}){
 const sales=data?.salesByDay||[];
 const maxSales=Math.max(1,...sales.map(item=>Number(item.verifiedSales||0)));
 const statuses=data?.statusBreakdown||[];
 const topProducts=data?.topProducts||[];
 const lowStock=data?.lowStockItems||[];
 const recentOrders=data?.recentOrders||[];
 return <div className="admin-analytics">
  <section className="summary">
   <h2>Verified sales · last 30 days</h2>
   {sales.length?sales.map(item=><div className="analytics-bar-row" key={item.day}>
    <span>{item.day}</span>
    <div className="analytics-bar-track" role="img" aria-label={item.day+" verified sales "+formatCurrency(item.verifiedSales)}>
     <span style={{width:Math.max(0,Math.min(100,Number(item.verifiedSales||0)/maxSales*100))+'%'}}/>
    </div>
    <b>{formatCurrency(item.verifiedSales)}</b>
   </div>):<p className="muted">No verified sales in the last 30 days.</p>}
   <p className="muted">Simulated payments are excluded from this verified-sales chart.</p>
  </section>
  <section className="summary">
   <h2>Orders by status</h2>
   {statuses.length?statuses.map(item=><div className="analytics-list-row" key={item.status}>
    <span>{item.status.replaceAll('_',' ')}</span><b>{item.count}</b>
   </div>):<p className="muted">No orders have been recorded.</p>}
  </section>
  <section className="summary">
   <h2>Top products</h2>
   {topProducts.length?topProducts.map(item=><div className="analytics-list-row" key={item.productName}>
    <span>{item.productName}<small>{item.quantitySold} units sold</small></span><b>{formatCurrency(item.grossSales)}</b>
   </div>):<p className="muted">No successfully paid product sales yet.</p>}
  </section>
  <section className="summary">
   <h2>Low stock · 5 or fewer</h2>
   {lowStock.length?lowStock.map(item=><div className="analytics-list-row" key={item.id}>
    <span>{item.name}</span><b>{item.stock} left</b>
   </div>):<p className="muted">No active products are at the low-stock threshold.</p>}
   <Link className="text-link" to="/admin/inventory">Open inventory →</Link>
  </section>
  <section className="summary admin-analytics-wide">
   <h2>Recent orders</h2>
   {recentOrders.length?recentOrders.map(item=><div className="analytics-list-row" key={item.id}>
    <span><Link to={'/admin/orders/'+item.id}>#{item.id} · {item.customerName}</Link> <small>{item.customerEmail} · {formatDate(item.createdAt)}</small></span>
    <b>{formatCurrency(item.total)} · {item.status.replaceAll('_',' ')}</b>
   </div>):<p className="muted">No orders have been placed yet.</p>}
  </section>
 </div>;
}

export function Admin(){
 const [data,setData]=useState(null),[loading,setLoading]=useState(true),[error,setError]=useState('');
 const load=async()=>{
  setLoading(true);setError('');
  try{const response=await api.get('/admin/dashboard');setData(response.data);}
  catch(e){setError(e.response?.data?.message||'Could not load the owner dashboard.');}
  finally{setLoading(false);}
 };
 useEffect(()=>{load();},[]);
 const statusCount=(statuses)=>(data?.statusBreakdown||[])
  .filter(item=>statuses.includes(item.status))
  .reduce((total,item)=>total+Number(item.count||0),0);
 return <section className="section admin admin-page">
  <span className="eyebrow">Owner workspace · ShopIt operations</span>
  <h1>Store dashboard</h1>
  <div className="admin-shortcuts">
   <Link className="secondary" to="/admin/orders">Manage orders</Link>
   <Link className="secondary" to="/admin/products">Manage products</Link>
   <Link className="secondary" to="/admin/categories">Manage categories</Link>
   <Link className="secondary" to="/admin/customers">Manage customers</Link>
   <Link className="secondary" to="/admin/inventory">Inventory</Link>
   <Link className="secondary" to="/admin/coupons">Coupons</Link>
  </div>
  {error&&<div className="error" role="alert">{error}</div>}
  {loading?<div className="loading" role="status">Loading store performance…</div>:data&&<>
   <div className="stats admin-kpis">
    {[
     ['Awaiting payment',data.pendingOrders],
     ['To ship',statusCount(['CONFIRMED','PROCESSING'])],
     ['Low-stock products',data.lowStockProducts],
     ['Customers',data.customers],
     ['Verified sales',formatCurrency(data.verifiedSales)],
     ['Simulated payment value',formatCurrency(data.simulatedPaymentValue)],
     ['Estimated stock margin',formatCurrency(data.estimatedGrossMargin)]
    ].map(([label,value])=><div className="stat" key={label}><span>{label}</span><b>{value}</b></div>)}
   </div>
   <div className="stats budget-stats">
    {[
     ['Active products',data.products],
     ['All orders',data.orders],
     ['Successful payment records',data.paidPayments],
     ['Inventory cost budget',formatCurrency(data.inventoryCostBudget)],
     ['Potential retail value',formatCurrency(data.potentialRetailValue)]
    ].map(([label,value])=><div className="stat" key={label}><span>{label}</span><b>{value}</b></div>)}
   </div>
   <p className="muted">Estimated stock margin is potential retail value minus current unit-cost value. It is not realized profit or accounting margin. Simulated payment value is kept separate from verified sales.</p>
   <AdminAnalytics data={data}/>
  </>}
 </section>;
}
export function AdminCustomerDetail(){
 const {id}=useParams();
 const [customer,setCustomer]=useState(null),[history,setHistory]=useState(null),[page,setPage]=useState(0);
 const [loading,setLoading]=useState(true),[saving,setSaving]=useState(false),[error,setError]=useState('');
 const load=async()=>{
  setLoading(true);setError('');
  try{
   const [customerResult,historyResult]=await Promise.all([
    api.get('/admin/customers/'+id),
    api.get('/admin/customers/'+id+'/orders',{params:{page,size:20}})
   ]);
   setCustomer(customerResult.data);setHistory(historyResult.data);
  }catch(e){setError(e.response?.data?.message||'Could not load this customer.');}
  finally{setLoading(false);}
 };
 useEffect(()=>{load();},[id,page]);
 const toggleStatus=async()=>{
  if(!customer)return;
  setSaving(true);setError('');
  try{
   const result=await api.put('/admin/customers/'+id+'/status',{enabled:!customer.enabled});
   setCustomer(result.data);
  }catch(e){setError(e.response?.data?.message||'Could not update customer access.');}
  finally{setSaving(false);}
 };
 return <section className="section admin">
  <Link className="back" to="/admin">← Owner dashboard</Link>
  <span className="eyebrow">Customer account</span>
  <h1>{customer?.name||'Customer details'}</h1>
  {error&&<div className="error" role="alert">{error}</div>}
  {loading?<div className="loading" role="status">Loading customer details…</div>:customer&&<>
   <div className="summary customer-detail-summary">
    <div><b>{customer.email}</b><span>{customer.phone||'No phone number saved'}</span><small>Joined {formatDate(customer.createdAt)}</small></div>
    <div><span className={'status '+(customer.enabled?'confirmed':'cancelled')}>{customer.enabled?'Enabled':'Disabled'}</span>
     <button className="secondary" disabled={saving} onClick={toggleStatus}>{saving?'Saving…':customer.enabled?'Disable account':'Enable account'}</button>
    </div>
   </div>
   <div className="summary admin-orders">
    <h2>Order history</h2>
    {history?.content?.length?history.content.map(order=><article className="customer-order-row" key={order.id}>
     <div><b>Order #{order.id}</b><small>{formatDate(order.createdAt)} · {order.status.replaceAll('_',' ')}</small></div>
     <b>{formatCurrency(order.total)}</b>
     <details><summary>Items and timeline</summary>
      {(order.items||[]).map((item,index)=><small key={index}>{item.productName} × {item.quantity} · {formatCurrency(item.lineTotal)}</small>)}
      {(order.timeline||[]).map((event,index)=><small key={'timeline-'+index}>{event.newStatus.replaceAll('_',' ')} · {event.actor} · {new Date(event.changedAt).toLocaleString()}</small>)}
     </details>
    </article>):<p className="muted">This customer has not placed any orders.</p>}
    <div className="pagination-controls">
     <button className="secondary" disabled={!history||page<=0} onClick={()=>setPage(value=>value-1)}>Previous orders</button>
     <span>Page {history?.totalPages?page+1:0} of {history?.totalPages||0}</span>
     <button className="secondary" disabled={!history||page+1>=history.totalPages} onClick={()=>setPage(value=>value+1)}>Next orders</button>
    </div>
   </div>
  </>}
 </section>;
}
