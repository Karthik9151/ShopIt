import {describe,expect,it} from 'vitest';
import {shouldRetryGet} from './api';

describe('GET retry policy',()=>{
 it('retries a GET for gateway 502, 503 and 504 once',()=>{
  for(const status of [502,503,504]){
   expect(shouldRetryGet({config:{method:'get'},response:{status}})).toBe(true);
  }
  expect(shouldRetryGet({config:{method:'get',__shopitRetried:true},response:{status:503}})).toBe(false);
 });
 it('retries a GET network failure but not a cancelled request',()=>{
  expect(shouldRetryGet({config:{method:'get'},request:{},code:'ERR_NETWORK'})).toBe(true);
  expect(shouldRetryGet({config:{method:'get'},request:{},code:'ERR_CANCELED'})).toBe(false);
 });
 it('never retries a mutating HTTP request',()=>{
  for(const method of ['post','put','patch','delete']){
   expect(shouldRetryGet({config:{method},response:{status:503}})).toBe(false);
   expect(shouldRetryGet({config:{method},request:{},code:'ERR_NETWORK'})).toBe(false);
  }
 });
});
