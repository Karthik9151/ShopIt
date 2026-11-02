import {beforeEach,describe,expect,it} from 'vitest';
import {fireEvent,render,screen} from '@testing-library/react';
import App from './App';

describe('protected route navigation',()=>{
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

 it('keeps account creation reachable from the sign-in route',()=>{
  render(<App/>);
  fireEvent.click(screen.getByRole('link',{name:'Create an account'}));

  expect(screen.getByRole('heading',{name:'Make yourself at home.'})).toBeTruthy();
  expect(screen.getByLabelText('Your name')).toBeTruthy();
  expect(screen.getByRole('button',{name:'Create account'})).toBeTruthy();
 });
});
