const API_BASE = '/api';

const JSON_HEADERS = { 'Content-Type': 'application/json' };

export class ApiError extends Error {
  constructor(message, fieldErrors) {
    super(message);
    this.name = 'ApiError';
    this.fieldErrors = fieldErrors ?? null;
  }
}

async function toError(response) {
  try {
    const problem = await response.json();
    if (problem.errors) {
      return new ApiError('Please correct the highlighted fields.', problem.errors);
    }
    if (problem.detail) {
      return new ApiError(problem.detail);
    }
  } catch {
  }
  return new ApiError(`Request failed (${response.status})`);
}

async function request(path, options) {
  const response = await fetch(API_BASE + path, options);
  if (!response.ok) {
    throw await toError(response);
  }
  return response.status === 204 ? null : response.json();
}

function queryString(params) {
  const pairs = Object.entries(params).filter(
    ([, value]) => value !== '' && value !== null && value !== undefined);
  const query = new URLSearchParams(pairs).toString();
  return query ? `?${query}` : '';
}

export function fetchOrders(params = {}) {
  return request(`/orders${queryString(params)}`);
}

export function fetchOrder(id) {
  return request(`/orders/${id}`);
}

export function createOrder(order) {
  return request('/orders', {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify(order)
  });
}

export function updateOrder(id, patch) {
  return request(`/orders/${id}`, {
    method: 'PATCH',
    headers: JSON_HEADERS,
    body: JSON.stringify(patch)
  });
}

export function bulkDeleteOrders(ids) {
  return request(`/orders${queryString({ ids: ids.join(',') })}`, { method: 'DELETE' });
}

export function deleteOrder(id) {
  return request(`/orders/${id}`, { method: 'DELETE' });
}
