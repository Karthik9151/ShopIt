import React from 'react';
import {afterEach,describe,expect,it} from 'vitest';
import {cleanup,render,screen} from '@testing-library/react';
import {MemoryRouter} from 'react-router-dom';
import AdminLayout from './AdminLayout';

describe('AdminLayout',()=>{
 afterEach(()=>cleanup());

 it('shows all owner-console sections and its nested page content',()=>{
  render(<MemoryRouter initialEntries={['/admin/orders']}>
   <AdminLayout><h1>Orders</h1></AdminLayout>
  </MemoryRouter>);

  expect(screen.getByRole('heading',{name:'Orders'})).toBeTruthy();
  for(const label of ['Overview','Orders','Products','Categories','Customers','Coupons','Inventory']){
   expect(screen.getAllByRole('link',{name:label}).length).toBeGreaterThan(0);
  }
  expect(screen.getByRole('link',{name:/Back to storefront/}).getAttribute('href')).toBe('/products');
 });
});
