import {describe,expect,it} from 'vitest';
import {getOrderStatusOptions} from './orderStatus';

describe('admin order status options',()=>{
  it('offers the current status and only server-approved next statuses',()=>{
    expect(getOrderStatusOptions({
      status:'PENDING_PAYMENT',
      allowedNextStatuses:['CANCELLED']
    })).toEqual(['PENDING_PAYMENT','CANCELLED']);
    expect(getOrderStatusOptions({
      status:'CONFIRMED',
      allowedNextStatuses:['PROCESSING']
    })).toEqual(['CONFIRMED','PROCESSING']);
  });

  it('does not offer reverse or skipped transitions for terminal orders',()=>{
    expect(getOrderStatusOptions({
      status:'DELIVERED',
      allowedNextStatuses:[]
    })).toEqual(['DELIVERED']);
    expect(getOrderStatusOptions(null)).toEqual([]);
  });

  it('removes duplicate current status from server-provided next statuses',()=>{
    expect(getOrderStatusOptions({
      status:'SHIPPED',
      allowedNextStatuses:['SHIPPED','DELIVERED']
    })).toEqual(['SHIPPED','DELIVERED']);
  });
});
