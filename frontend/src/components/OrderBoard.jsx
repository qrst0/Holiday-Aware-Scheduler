import { useCallback, useEffect, useState } from 'react';

import { fetchOrders } from '../api/client';
import RiskBadge from './RiskBadge';

const STATUS_LABELS = {
  PLANNED: 'Planned',
  IN_PROGRESS: 'In progress',
  DONE: 'Done',
  CANCELLED: 'Cancelled'
};

function formatDate(isoDate) {
  const [year, month, day] = isoDate.split('-');
  return `${day}/${month}/${year}`;
}

export default function OrderBoard() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const load = useCallback(() => {
    setLoading(true);
    setError(null);
    fetchOrders()
      .then(setOrders)
      .catch((cause) => setError(cause.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(load, [load]);

  return (
    <>
      <div className="page-head">
        <div>
          <h1 className="page-title">Work orders</h1>
          <p className="page-subtitle">
            Working days exclude weekends and public holidays in the plant&apos;s country.
          </p>
        </div>
        {!loading && !error && (
          <span className="count-pill">
            {orders.length} {orders.length === 1 ? 'order' : 'orders'}
          </span>
        )}
      </div>

      <div className="card">
        {loading && <LoadingState />}
        {!loading && error && <ErrorState message={error} onRetry={load} />}
        {!loading && !error && orders.length === 0 && <EmptyState />}
        {!loading && !error && orders.length > 0 && <OrderTable orders={orders} />}
      </div>
    </>
  );
}

function OrderTable({ orders }) {
  return (
    <div className="table-scroll">
      <table>
        <thead>
          <tr>
            <th>Order</th>
            <th>ID</th>
            <th>Country</th>
            <th>Window</th>
            <th>Working days</th>
            <th>Status</th>
            <th>Risk</th>
          </tr>
        </thead>
        <tbody>
          {orders.map((order) => (
            <tr key={order.id}>
              <td>
                <div className="cell-primary">{order.productCode}</div>
                <div className="cell-sub">Qty {order.quantity}</div>
              </td>
              <td className="cell-sub mono">#{order.id}</td>
              <td>
                <span className="country">{order.countryCode}</span>
              </td>
              <td className="mono">
                <div>{formatDate(order.startDate)}</div>
                <div className="cell-sub">to {formatDate(order.dueDate)}</div>
              </td>
              <td className="mono">
                <div className="cell-primary">{order.workingDays}</div>
                <div className="cell-sub">{order.requiredDays} needed</div>
              </td>
              <td className="status-text">{STATUS_LABELS[order.status] ?? order.status}</td>
              <td>
                <RiskBadge flag={order.riskFlag} />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function LoadingState() {
  return (
    <div aria-busy="true" aria-label="Loading orders">
      {[0, 1, 2, 3].map((row) => (
        <div className="skeleton-row" key={row} />
      ))}
    </div>
  );
}

function ErrorState({ message, onRetry }) {
  return (
    <div className="state state-error">
      <div className="state-title">Could not load orders</div>
      <div>{message}</div>
      <button className="retry" type="button" onClick={onRetry}>
        Try again
      </button>
    </div>
  );
}

function EmptyState() {
  return (
    <div className="state">
      <div className="state-title">No work orders yet</div>
      <div>Create one to see its holiday-aware risk flag.</div>
    </div>
  );
}
