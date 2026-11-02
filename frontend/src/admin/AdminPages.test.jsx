import React from 'react';
import {afterEach,beforeEach,describe,expect,it,vi} from 'vitest';
import {cleanup,fireEvent,render,screen,waitFor} from '@testing-library/react';
import {MemoryRouter,Route,Routes} from 'react-router-dom';
import api from '../services/api';
import {AdminCustomersPage,AdminOrderDetailPage,AdminOrdersPage,AdminProductsPage} from './AdminPages';

vi.mock('../services/api',()=>({default:{get:vi.fn(),post:vi.fn(),put:vi.fn(),patch:vi.fn()}}));

describe('Admin management pages',()=>{
 afterEach(()=>{cleanup();vi.restoreAllMocks();});
 beforeEach(()=>vi.clearAllMocks());

 it('renders an empty customer state from the paginated API response',async()=>{
  api.get.mockResolvedValueOnce({data:{content:[],totalPages:0,totalElements:0}});
  render(<MemoryRouter><AdminCustomersPage/></MemoryRouter>);
  expect(screen.getByRole('heading',{name:'Customers'})).toBeTruthy();
  expect(await screen.findByText('No customers found.')).toBeTruthy();
  expect(api.get).toHaveBeenCalledWith('/admin/customers',{params:{q:undefined,page:0,size:20}});
 });

 it('shows the empty order state and supports CSV button presence',async()=>{
  api.get.mockResolvedValueOnce({data:{content:[],totalPages:0,totalElements:0}});
  render(<MemoryRouter><AdminOrdersPage/></MemoryRouter>);
  expect(screen.getByRole('heading',{name:'Orders'})).toBeTruthy();
  expect(screen.getByRole('button',{name:'Export CSV'})).toBeTruthy();
  expect(await screen.findByText('No orders match these filters.')).toBeTruthy();
 });

 it('shows the create-product action and paginated empty state',async()=>{
  api.get.mockResolvedValueOnce({data:{content:[],totalPages:0,totalElements:0}});
  render(<MemoryRouter><AdminProductsPage/></MemoryRouter>);
  expect(screen.getByRole('link',{name:'Add product'}).getAttribute('href')).toBe('/admin/products/new');
  expect(await screen.findByText('No products found.')).toBeTruthy();
 });
 it('lets an admin choose any later fulfillment status',async()=>{
  vi.spyOn(window,'confirm').mockReturnValue(true);
  const order={id:501,status:'CONFIRMED',createdAt:'2026-11-02T10:00:00Z',shippingAddress:'12 Example Street',items:[{productName:'Ceramic Cup',quantity:1,unitPrice:100,lineTotal:100}],subtotal:100,discountAmount:0,couponCode:null,shipping:0,tax:0,total:100,timeline:[],allowedNextStatuses:['PROCESSING','SHIPPED','DELIVERED'],simulatedPaymentEnabled:true};
  const updated={...order,status:'SHIPPED',timeline:[{newStatus:'SHIPPED',changedAt:'2026-11-02T11:00:00Z',actor:'ADMIN'}],allowedNextStatuses:['DELIVERED']};
  api.get.mockResolvedValueOnce({data:order});
  api.put.mockResolvedValueOnce({data:updated});
  render(<MemoryRouter initialEntries={['/admin/orders/501']}><Routes><Route path="/admin/orders/:id" element={<AdminOrderDetailPage/>}/></Routes></MemoryRouter>);
  const select=await screen.findByRole('combobox',{name:'Next order status'});
  expect(Array.from(select.options).map(option=>option.value)).toEqual(['PROCESSING','SHIPPED','DELIVERED']);
  fireEvent.change(select,{target:{value:'SHIPPED'}});
  fireEvent.click(screen.getByRole('button',{name:'Confirm status'}));
  await waitFor(()=>expect(api.put).toHaveBeenCalledWith('/admin/orders/501/status',{status:'SHIPPED'}));
 });

});
