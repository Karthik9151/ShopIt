export function getOrderStatusOptions(order) {
  if (!order?.status) {
    return [];
  }

  const nextStatuses = Array.isArray(order.allowedNextStatuses)
    ? order.allowedNextStatuses
    : [];

  return [order.status, ...nextStatuses.filter(status => status !== order.status)];
}
