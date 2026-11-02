import React from 'react';
import {afterEach,beforeEach,describe,expect,it,vi} from 'vitest';
import {cleanup,render,screen,waitFor} from '@testing-library/react';
import {MemoryRouter,Route,Routes} from 'react-router-dom';
import api from '../services/api';
import {AdminCustomersPage,AdminOrdersPage,AdminProductsPage} from './AdminPages';

vi.mock('../services/api',()=>({default:{get:vi.fn(),post:vi.fn(),put:vi.fn(),patch:vi.fn()}}));

describe('Admin management pages',()=>{
 afterEach(()=>cleanup());
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
});
