const API_BASE = '/api';

async function toError(response) {
  let message = `Request failed (${response.status})`;
  try {
    const problem = await response.json();
    if (problem.errors) {
      message = Object.entries(problem.errors)
        .map(([field, reason]) => `${field}: ${reason}`)
        .join(', ');
    } else if (problem.detail) {
      message = problem.detail;
    }
  } catch {
  }
  return new Error(message);
}

async function request(path, options) {
  const response = await fetch(API_BASE + path, options);
  if (!response.ok) {
    throw await toError(response);
  }
  return response.status === 204 ? null : response.json();
}

export function fetchOrders() {
  return request('/orders');
}
