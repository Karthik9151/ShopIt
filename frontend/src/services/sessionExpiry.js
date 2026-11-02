export function shouldExpireSession(error) {
  if (error?.response?.status !== 401) {
    return false;
  }

  const rawUrl = String(error.config?.url ?? '').split('?')[0].replace(/\/+$/, '');
  const path = rawUrl.replace(/^https?:\/\/[^/]+/i, '').replace(/^\/+/, '');
  const normalized = path.startsWith('api/') ? path.slice(4) : path;

  return normalized !== 'auth/login' && normalized !== 'auth/register';
}

export function createSessionExpiryInterceptor(onExpired) {
  let redirectInProgress = false;

  return {
    intercept(error) {
      if (shouldExpireSession(error) && !redirectInProgress) {
        redirectInProgress = true;
        onExpired?.();
      }

      return Promise.reject(error);
    },

    reset() {
      redirectInProgress = false;
    }
  };
}
