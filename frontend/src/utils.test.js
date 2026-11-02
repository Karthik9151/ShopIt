import { describe, expect, it } from 'vitest';
import { formatCurrency, formatDate } from './utils';

describe('formatCurrency', () => {
  it('formats numeric strings as Indian rupees', () => {
    expect(formatCurrency('1250.5')).toContain('1,250.50');
  });

  it('treats nullish values as zero', () => {
    expect(formatCurrency(null)).toContain('0.00');
    expect(formatCurrency(undefined)).toContain('0.00');
  });
});

describe('formatDate', () => {
  it('formats a valid date using the Indian locale', () => {
    expect(formatDate('2026-11-02T00:00:00Z')).toContain('2026');
  });
});
