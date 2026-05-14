const API_BASE = import.meta.env.VITE_API_BASE || '';

async function request(url, options = {}) {
  const res = await fetch(`${API_BASE}${url}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  });
  if (!res.ok) {
    const err = new Error(`HTTP ${res.status}`);
    err.status = res.status;
    try { err.body = await res.json(); } catch {}
    throw err;
  }
  if (res.status === 204) return null;
  return res.json();
}

export const api = {
  get:    (url) => request(url),
  post:   (url, data) => request(url, { method: 'POST', body: JSON.stringify(data) }),
  put:    (url, data) => request(url, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (url) => request(url, { method: 'DELETE' }),
};
