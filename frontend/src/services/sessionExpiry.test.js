import {describe,expect,it,vi} from 'vitest';
import {createSessionExpiryInterceptor,shouldExpireSession} from './sessionExpiry';

function unauthorized(url) {
  return {response:{status:401},config:{url}};
}

describe('session expiry interceptor',()=>{
  it('expires a protected request after a 401 response',()=>{
    expect(shouldExpireSession(unauthorized('/orders/my'))).toBe(true);
    expect(shouldExpireSession(unauthorized('/api/profile'))).toBe(true);
  });

  it('does not treat login or registration failures as an expired session',()=>{
    expect(shouldExpireSession(unauthorized('/auth/login'))).toBe(false);
    expect(shouldExpireSession(unauthorized('/auth/register'))).toBe(false);
    expect(shouldExpireSession(unauthorized('https://shopit.example/api/auth/login'))).toBe(false);
  });

  it('ignores non-401 responses',()=>{
    expect(shouldExpireSession({response:{status:403},config:{url:'/admin'}})).toBe(false);
    expect(shouldExpireSession({response:{status:500},config:{url:'/orders'}})).toBe(false);
  });

  it('dispatches one expiry event for concurrent 401 responses and permits a later expiry after reset',async()=>{
    const onExpired=vi.fn();
    const interceptor=createSessionExpiryInterceptor(onExpired);
    const first=unauthorized('/orders/my');
    const second=unauthorized('/profile');

    await expect(interceptor.intercept(first)).rejects.toBe(first);
    await expect(interceptor.intercept(second)).rejects.toBe(second);
    expect(onExpired).toHaveBeenCalledTimes(1);

    interceptor.reset();
    await expect(interceptor.intercept(second)).rejects.toBe(second);
    expect(onExpired).toHaveBeenCalledTimes(2);
  });

  it('does not intercept failed credential submissions',async()=>{
    const onExpired=vi.fn();
    const interceptor=createSessionExpiryInterceptor(onExpired);
    const loginError=unauthorized('/auth/login');

    await expect(interceptor.intercept(loginError)).rejects.toBe(loginError);
    expect(onExpired).not.toHaveBeenCalled();
  });
});
