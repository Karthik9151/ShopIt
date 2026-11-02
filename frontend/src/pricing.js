import {formatCurrency} from './utils';

export function getFreeShippingThreshold(value = import.meta.env.VITE_FREE_SHIPPING_THRESHOLD) {
  const parsed = Number(value ?? 1999);
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : 1999;
}

export function getShippingLabel(shipping) {
  return Number(shipping ?? 0) === 0 ? 'Free' : formatCurrency(shipping);
}

export function getFreeShippingMessage(remaining) {
  const amount = Number(remaining ?? 0);
  return amount > 0 ? `Add ${formatCurrency(amount)} more for free shipping` : '';
}
