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

export function fetchOrders(params = {}) {
  const query = new URLSearchParams(
    Object.entries(params).filter(([, value]) => value !== '' && value != null));
  const suffix = query.toString() ? `?${query}` : '';
  return request(`/orders${suffix}`).then((page) => page.content);
}

export function createOrder(order) {
  return request('/orders', {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify(order)
  });
}
