import {describe,expect,it} from 'vitest';
import {getPostLoginPath} from './App';

describe('post-login destination',()=>{
 const customer={id:1,role:'CUSTOMER'};
 const admin={id:2,role:'ADMIN'};

 it('never sends a customer back to a requested admin route',()=>{
  expect(getPostLoginPath(customer,'/admin')).toBe('/products');
  expect(getPostLoginPath(customer,'/admin/inventory')).toBe('/products');
 });

 it('preserves safe internal destinations for customers',()=>{
  expect(getPostLoginPath(customer,'/orders')).toBe('/orders');
  expect(getPostLoginPath(customer,null)).toBe('/products');
 });

 it('allows administrators to return to the requested admin route',()=>{
  expect(getPostLoginPath(admin,'/admin/orders')).toBe('/admin/orders');
  expect(getPostLoginPath(admin,null)).toBe('/admin');
 });

 it('does not redirect to an external or authentication loop path',()=>{
  expect(getPostLoginPath(customer,'https://example.invalid')).toBe('/products');
  expect(getPostLoginPath(customer,'//example.invalid')).toBe('/products');
  expect(getPostLoginPath(customer,'/login')).toBe('/products');
  expect(getPostLoginPath(customer,'/register')).toBe('/products');
 });
});
