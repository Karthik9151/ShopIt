import {describe,expect,it} from 'vitest';
import {formatCurrency} from './utils';
describe('formatCurrency',()=>{it('formats INR amounts',()=>{expect(formatCurrency('1250.5')).toContain('1,250.50');});});