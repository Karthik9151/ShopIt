import React from 'react';
import {NavLink,Link} from 'react-router-dom';

const sections=[
 {to:'/admin',label:'Overview',end:true},
 {to:'/admin/orders',label:'Orders'},
 {to:'/admin/products',label:'Products'},
 {to:'/admin/categories',label:'Categories'},
 {to:'/admin/customers',label:'Customers'},
 {to:'/admin/coupons',label:'Coupons'},
 {to:'/admin/inventory',label:'Inventory'}
];

export default function AdminLayout({children}){
 return <div className="admin-shell">
  <aside className="admin-sidebar" aria-label="Administrator navigation">
   <Link to="/admin" className="admin-sidebar-brand">ShopIt <span>Owner console</span></Link>
   <nav>{sections.map(section=><NavLink key={section.to} to={section.to} end={section.end}>{section.label}</NavLink>)}</nav>
   <Link to="/products" className="admin-store-link">← Back to storefront</Link>
  </aside>
  <div className="admin-workspace">
   <nav className="admin-mobile-tabs" aria-label="Administrator sections">
    {sections.map(section=><NavLink key={section.to} to={section.to} end={section.end}>{section.label}</NavLink>)}
   </nav>
   {children}
  </div>
 </div>;
}
