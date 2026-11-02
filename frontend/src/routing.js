export function getPostLoginPath(user, requestedPath) {
 const requested=typeof requestedPath==='string'&&requestedPath.startsWith('/')&&!requestedPath.startsWith('//')?requestedPath:null;
 const isAdminPath=requested==='/admin'||requested?.startsWith('/admin/');
 if(user?.role!=='ADMIN'&&isAdminPath)return '/products';
 if(requested==='/login'||requested==='/register')return user?.role==='ADMIN'?'/admin':'/products';
 return requested||(user?.role==='ADMIN'?'/admin':'/products');
}

export function getPageTitle(pathname, isAdmin=false){
 if(pathname==='/')return 'Home';
 if(pathname==='/products')return 'Shop products';
 if(pathname.startsWith('/products/'))return 'Product details';
 if(pathname==='/login')return 'Sign in';
 if(pathname==='/register')return 'Create account';
 if(pathname==='/cart')return 'Shopping bag';
 if(pathname==='/checkout')return 'Checkout';
 if(pathname==='/wishlist')return 'Wishlist';
 if(pathname==='/orders')return 'Your orders';
 if(pathname.startsWith('/orders/'))return 'Order details';
 if(pathname==='/profile')return 'My account';
 if(pathname==='/offers')return 'Offers';
 if(pathname==='/admin'&&!isAdmin)return 'Admin access required';
 if(pathname==='/admin')return 'Admin dashboard';
 if(pathname==='/admin/inventory')return 'Admin inventory';
 if(pathname==='/admin/orders')return 'Admin orders';
 if(pathname.startsWith('/admin/orders/'))return 'Admin order details';
 if(pathname==='/admin/products')return 'Admin products';
 if(pathname==='/admin/products/new')return 'Create product';
 if(pathname.startsWith('/admin/products/'))return 'Edit product';
 if(pathname==='/admin/categories')return 'Admin categories';
 if(pathname==='/admin/customers')return 'Admin customers';
 if(pathname==='/admin/coupons')return 'Admin coupons';
 if(pathname.startsWith('/admin/customers/'))return 'Admin customer details';
 if(pathname.startsWith('/admin'))return isAdmin?'Admin console':'Admin access required';
 return 'Page not found';
}
