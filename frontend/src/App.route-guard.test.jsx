import {afterEach,beforeEach,describe,expect,it} from 'vitest';
import {cleanup,fireEvent,render,screen} from '@testing-library/react';
import App from './App';

describe('protected route navigation',()=>{
 afterEach(()=>cleanup());
 beforeEach(()=>{
  sessionStorage.clear();
  window.history.replaceState({},'', '/admin');
 });

 it('redirects unauthenticated admin-route visitors to sign in',()=>{
  render(<App/>);

  expect(screen.getByRole('heading',{name:'Good to see you again.'})).toBeTruthy();
  expect(screen.getByLabelText('Email address')).toBeTruthy();
  expect(screen.getByLabelText('Password')).toBeTruthy();
 });

 it('shows a clear access-required page for an authenticated customer',()=>{
  sessionStorage.setItem('shopit_access_token','test-only-token');
  sessionStorage.setItem('shopit_user',JSON.stringify({
   id:7,name:'Test Customer',email:'customer@example.test',role:'CUSTOMER'
  }));

  render(<App/>);

  expect(screen.getByRole('heading',{name:'Admin access required'})).toBeTruthy();
  expect(screen.getByRole('link',{name:'Back to shop'}).getAttribute('href')).toBe('/products');
 });

 it('keeps account creation reachable from the sign-in route',()=>{
  render(<App/>);
  fireEvent.click(screen.getByRole('link',{name:'Create an account'}));

  expect(screen.getByRole('heading',{name:'Make yourself at home.'})).toBeTruthy();
  expect(screen.getByLabelText('Your name')).toBeTruthy();
  expect(screen.getByRole('button',{name:'Create account'})).toBeTruthy();
 });
});
