import {describe,expect,it} from 'vitest';
import {getFreeShippingMessage,getFreeShippingThreshold,getShippingLabel} from './pricing';

describe('pricing presentation helpers',()=>{
  it('uses configured threshold and defaults to INR 1,999',()=>{
    expect(getFreeShippingThreshold('2500')).toBe(2500);
    expect(getFreeShippingThreshold(undefined)).toBe(1999);
    expect(getFreeShippingThreshold('invalid')).toBe(1999);
  });
  it('labels zero shipping as Free',()=>{
    expect(getShippingLabel(0)).toBe('Free');
    expect(getShippingLabel('49.00')).toContain('49.00');
  });
  it('shows remaining amount only below threshold',()=>{
    expect(getFreeShippingMessage('0.00')).toBe('');
    expect(getFreeShippingMessage('0.01')).toContain('0.01');
  });
});
