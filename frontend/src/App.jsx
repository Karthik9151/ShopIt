import React,{useEffect,useState} from 'react';
import {BrowserRouter,Routes,Route,Link,NavLink,useNavigate,Navigate,useLocation} from 'react-router-dom';
import {Heart,ShoppingBag,UserRound,Search,Menu,X,LogOut} from 'lucide-react';
import {AuthProvider,useAuth} from './auth/AuthContext';
import {getPageTitle} from './routing';
export {getPageTitle,getPostLoginPath} from './routing';
import {formatCurrency} from './utils';
import {getFreeShippingThreshold} from './pricing';
import {Home,Products,ProductDetails,AuthPage,Cart,Checkout,Orders,OrderDetails,Wishlist,Profile,Offers} from './pages/StorefrontPages';
import AdminLayout from './admin/AdminLayout';
import {AdminOrdersPage,AdminOrderDetailPage,AdminProductsPage,AdminProductFormPage,AdminCategoriesPage,AdminCustomersPage} from './admin/AdminPages';
import {Admin,AdminCustomerDetail} from './admin/AdminDashboardPages';
import {AdminCoupons,AdminInventory} from './admin/AdminOperationsPages';

const FREE_SHIPPING_THRESHOLD=getFreeShippingThreshold();

function SessionExpiryRedirect(){
 const {sessionExpired,expiredFrom,clearSessionExpired}=useAuth();
 const navigate=useNavigate();
 useEffect(()=>{
  if(!sessionExpired)return;
  navigate('/login',{replace:true,state:{from:expiredFrom,reason:'expired'}});
  clearSessionExpired();
 },[sessionExpired,expiredFrom,clearSessionExpired,navigate]);
 return null;
}


function ApiPendingBanner(){
 const [pending,setPending]=useState(0),[visible,setVisible]=useState(false);
 useEffect(()=>{
  const update=event=>setPending(Number(event.detail?.pending)||0);
  window.addEventListener('shopit:api-pending',update);
  return ()=>window.removeEventListener('shopit:api-pending',update);
 },[]);
 const active=pending>0;
 useEffect(()=>{
  if(!active){setVisible(false);return;}
  const timer=window.setTimeout(()=>setVisible(true),5000);
  return ()=>window.clearTimeout(timer);
 },[active]);
 if(!visible||!active)return null;
 return <div className="api-pending-banner" role="status" aria-live="polite">
  The server is taking longer than usual to respond. Your request is still in progress.
 </div>;
}

function AdminAccessRequired(){
 return <section className="section"><div className="empty" role="status">
  <span className="eyebrow">Restricted area</span>
  <h1>Admin access required</h1>
  <p>This area is available only to authorized ShopIt administrators.</p>
  <Link className="primary" to="/products">Back to shop</Link>
 </div></section>;
}

function Guard({children,admin=false}){
 const {user}=useAuth(),loc=useLocation();
 if(!user)return <Navigate to="/login" replace state={{from:loc.pathname}}/>;
 if(admin&&user.role!=='ADMIN')return <AdminAccessRequired/>;
 return children;
}

function RouteTitle(){
 const location=useLocation();
 const {isAdmin}=useAuth();
 useEffect(()=>{
  document.title=getPageTitle(location.pathname,isAdmin)+' | ShopIt';
 },[location.pathname,isAdmin]);
 return null;
}

function Layout(){
 const {user,logout,isAdmin}=useAuth(),[query,setQuery]=useState(''),[open,setOpen]=useState(false),navigate=useNavigate();
 const search=e=>{e.preventDefault();navigate('/products?q='+encodeURIComponent(query));setOpen(false);};
 return <><RouteTitle/><a className="skip-link" href="#main-content">Skip to main content</a><div className="announcement">A little something good, delivered to your door. <span>Free shipping over {formatCurrency(FREE_SHIPPING_THRESHOLD)}</span></div><header className="header">
 <Link to="/" className="brand"><span className="brand-mark">s.</span> shopit</Link>
 <form className="search" onSubmit={search}><Search size={17}/><input aria-label="Search products" placeholder="Search for something lovely..." value={query} onChange={e=>setQuery(e.target.value)}/><button>Search</button></form>
 <nav id="primary-navigation" aria-label="Primary navigation" className={open?'nav open':'nav'}><NavLink to="/products" onClick={()=>setOpen(false)}>Shop</NavLink><NavLink to="/products?sort=createdAt" onClick={()=>setOpen(false)}>New arrivals</NavLink><NavLink to="/offers" onClick={()=>setOpen(false)}>Offers</NavLink>{isAdmin&&<><NavLink to="/admin" onClick={()=>setOpen(false)}>Admin</NavLink><NavLink to="/admin/inventory" onClick={()=>setOpen(false)}>Inventory</NavLink><NavLink to="/admin/coupons" onClick={()=>setOpen(false)}>Coupons</NavLink></>}{user?<><NavLink to="/orders" onClick={()=>setOpen(false)}>My orders</NavLink><button className="nav-button" onClick={()=>{logout();navigate('/');}}><LogOut size={15}/> Sign out</button></>:<NavLink to="/login" onClick={()=>setOpen(false)}>Sign in</NavLink>}</nav>
 <div className="header-actions"><Link to="/wishlist" aria-label="Wishlist"><Heart size={19}/></Link><Link to="/cart" aria-label="Shopping bag"><ShoppingBag size={19}/></Link><Link to={user?'/profile':'/login'} aria-label="Account"><UserRound size={19}/></Link></div>
 <button className="mobile-menu" aria-label="Toggle navigation" aria-controls="primary-navigation" aria-expanded={open} onClick={()=>setOpen(v=>!v)}>{open?<X/>:<Menu/>}</button></header>
 <main id="main-content" tabIndex={-1}><Routes>
 <Route index element={<Home/>}/><Route path="/products" element={<Products/>}/><Route path="/products/:id" element={<ProductDetails/>}/><Route path="/login" element={<AuthPage/>}/><Route path="/register" element={<AuthPage register/>}/>
 <Route path="/cart" element={<Guard><Cart/></Guard>}/><Route path="/checkout" element={<Guard><Checkout/></Guard>}/><Route path="/wishlist" element={<Guard><Wishlist/></Guard>}/><Route path="/orders" element={<Guard><Orders/></Guard>}/><Route path="/orders/:id" element={<Guard><OrderDetails/></Guard>}/><Route path="/profile" element={<Guard><Profile/></Guard>}/><Route path="/offers" element={<Offers/>}/><Route path="/admin" element={<Guard admin><AdminLayout><Admin/></AdminLayout></Guard>}/>
 <Route path="/admin/orders" element={<Guard admin><AdminLayout><AdminOrdersPage/></AdminLayout></Guard>}/>
 <Route path="/admin/orders/:id" element={<Guard admin><AdminLayout><AdminOrderDetailPage/></AdminLayout></Guard>}/>
 <Route path="/admin/products" element={<Guard admin><AdminLayout><AdminProductsPage/></AdminLayout></Guard>}/>
 <Route path="/admin/products/new" element={<Guard admin><AdminLayout><AdminProductFormPage/></AdminLayout></Guard>}/>
 <Route path="/admin/products/:id" element={<Guard admin><AdminLayout><AdminProductFormPage/></AdminLayout></Guard>}/>
 <Route path="/admin/categories" element={<Guard admin><AdminLayout><AdminCategoriesPage/></AdminLayout></Guard>}/>
 <Route path="/admin/customers" element={<Guard admin><AdminLayout><AdminCustomersPage/></AdminLayout></Guard>}/>
 <Route path="/admin/customers/:id" element={<Guard admin><AdminLayout><AdminCustomerDetail/></AdminLayout></Guard>}/>
 <Route path="/admin/inventory" element={<Guard admin><AdminLayout><AdminInventory/></AdminLayout></Guard>}/>
 <Route path="/admin/coupons" element={<Guard admin><AdminLayout><AdminCoupons/></AdminLayout></Guard>}/><Route path="*" element={<NotFound/>}/>
 </Routes></main><footer className="footer"><div><Link to="/" className="brand"><span className="brand-mark">s.</span> shopit</Link><p>Good things for everyday life.</p></div><div><b>Explore</b><Link to="/products">All products</Link><Link to="/orders">Your orders</Link></div><div><b>Help</b><a href="mailto:hello@shopit.local">Contact support</a><span>Secure checkout · Easy returns</span></div><small>© {new Date().getFullYear()} ShopIt. Built with care.</small></footer></>;
}

function NotFound(){return <div className="empty not-found"><span className="eyebrow">404 · Off the map</span><h1>This page wandered off.</h1><p>Let's get you back to the good stuff.</p><Link to="/" className="primary">Go home</Link></div>;}

export default function App(){return <BrowserRouter><AuthProvider><SessionExpiryRedirect/><ApiPendingBanner/><Layout/></AuthProvider></BrowserRouter>;}
