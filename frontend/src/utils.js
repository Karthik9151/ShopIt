const INR_CURRENCY_FORMAT = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  maximumFractionDigits: 2
});

const INDIA_DATE_FORMAT = new Intl.DateTimeFormat('en-IN', {
  dateStyle: 'medium'
});

export function formatCurrency(value) {
  return INR_CURRENCY_FORMAT.format(Number(value ?? 0));
}

export function formatDate(value) {
  return INDIA_DATE_FORMAT.format(new Date(value));
}
