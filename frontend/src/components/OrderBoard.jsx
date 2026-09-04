import { useCallback, useEffect, useMemo, useRef, useState } from 'react';

import { fetchOrders } from '../api/client';
import OrderForm from './OrderForm';
import RiskBadge from './RiskBadge';

const STATUS_LABELS = {
  PLANNED: 'Planned',
  IN_PROGRESS: 'In progress',
  DONE: 'Done',
  CANCELLED: 'Cancelled'
};

const STATUS_STYLES = {
  PLANNED: 'pill pill-plain pill-info',
  IN_PROGRESS: 'pill pill-plain pill-info',
  DONE: 'pill pill-plain pill-ok',
  CANCELLED: 'pill pill-plain pill-neutral'
};

function formatDate(isoDate) {
  const [year, month, day] = isoDate.split('-');
  return `${day}/${month}/${year}`;
}

export default function OrderBoard() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [formOpen, setFormOpen] = useState(false);
  const [formClosing, setFormClosing] = useState(false);
  const [query, setQuery] = useState('');
  const [selected, setSelected] = useState(() => new Set());

  const searchRef = useRef(null);

  const load = useCallback(() => {
    setLoading(true);
    setError(null);
    fetchOrders()
      .then(setOrders)
      .catch((cause) => setError(cause.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(load, [load]);

  useEffect(() => {
    function onKeyDown(event) {
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
        event.preventDefault();
        searchRef.current?.focus();
      }
    }
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, []);

  useEffect(() => {
    if (!formClosing) {
      return undefined;
    }
    const timer = setTimeout(() => {
      setFormOpen(false);
      setFormClosing(false);
    }, 260);
    return () => clearTimeout(timer);
  }, [formClosing]);

  function openForm() {
    setFormClosing(false);
    setFormOpen(true);
  }

  function closeForm() {
    setFormClosing(true);
  }

  function handleFormExited() {
    setFormOpen(false);
    setFormClosing(false);
  }

  function handleCreated(created) {
    setOrders((current) => [...current, created]);
    closeForm();
  }

  const visible = useMemo(() => {
    const needle = query.trim().toLowerCase();
    if (!needle) {
      return orders;
    }
    return orders.filter((order) =>
      `${order.productCode} ${order.countryCode} ${order.status} ${order.riskFlag}`
        .toLowerCase()
        .includes(needle));
  }, [orders, query]);

  function toggleOne(id) {
    setSelected((current) => {
      const next = new Set(current);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  }

  function toggleAll() {
    setSelected((current) =>
      visible.every((order) => current.has(order.id))
        ? new Set()
        : new Set(visible.map((order) => order.id)));
  }

  const selectedCount = visible.filter((order) => selected.has(order.id)).length;

  function resultNote() {
    if (selectedCount > 0) {
      return `${selectedCount} selected`;
    }
    if (query.trim()) {
      return `${visible.length} of ${orders.length} matching`;
    }
    return `${orders.length} ${orders.length === 1 ? 'order' : 'orders'}`;
  }

  return (
    <>
      <div className="content-head">
        <h1 className="page-title">Work orders</h1>
        <div className="head-actions">
          <button className="btn" type="button" onClick={load} disabled={loading}>
            Refresh
          </button>
          <button
            className="btn btn-primary"
            type="button"
            onClick={formOpen && !formClosing ? closeForm : openForm}
          >
            {formOpen && !formClosing ? 'Close' : 'Add order'}
          </button>
        </div>
      </div>

      {formOpen && (
        <OrderForm
          closing={formClosing}
          onCreated={handleCreated}
          onCancel={closeForm}
          onExited={handleFormExited}
        />
      )}

      <div className="card">
        <div className="card-toolbar">
          <div className="search-field">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="7" />
              <path d="m20 20-3.5-3.5" />
            </svg>
            <input
              ref={searchRef}
              type="search"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Search orders"
              aria-label="Search orders"
            />
            <span className="kbd">Ctrl K</span>
          </div>

          {!loading && !error && <span className="result-note">{resultNote()}</span>}
        </div>

        {loading && <LoadingState />}
        {!loading && error && <ErrorState message={error} onRetry={load} />}
        {!loading && !error && orders.length === 0 && <EmptyState />}
        {!loading && !error && orders.length > 0 && visible.length === 0 && <NoMatchState query={query} />}
        {!loading && !error && visible.length > 0 && (
          <OrderTable
            orders={visible}
            selected={selected}
            onToggleOne={toggleOne}
            onToggleAll={toggleAll}
          />
        )}
      </div>
    </>
  );
}

function OrderTable({ orders, selected, onToggleOne, onToggleAll }) {
  const headerRef = useRef(null);
  const selectedCount = orders.filter((order) => selected.has(order.id)).length;
  const allSelected = selectedCount === orders.length;

  // indeterminate is a DOM property with no JSX attribute, so it has to be set directly.
  useEffect(() => {
    if (headerRef.current) {
      headerRef.current.indeterminate = selectedCount > 0 && !allSelected;
    }
  }, [selectedCount, allSelected]);

  return (
    <div className="table-scroll">
      <table>
        <thead>
          <tr>
            <th className="checkbox-cell">
              <input
                ref={headerRef}
                type="checkbox"
                checked={allSelected}
                onChange={onToggleAll}
                aria-label="Select all orders"
              />
            </th>
            <th>Order</th>
            <th>Quantity</th>
            <th>Risk</th>
            <th>Working days</th>
            <th>Start date</th>
            <th>Due date</th>
            <th>Country</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {orders.map((order) => (
            <tr key={order.id} className={selected.has(order.id) ? 'row-selected' : undefined}>
              <td className="checkbox-cell">
                <input
                  type="checkbox"
                  checked={selected.has(order.id)}
                  onChange={() => onToggleOne(order.id)}
                  aria-label={`Select ${order.productCode}`}
                />
              </td>
              <td className="cell-primary">{order.productCode}</td>
              <td className="mono">{order.quantity}</td>
              <td><RiskBadge flag={order.riskFlag} /></td>
              <td className="mono">
                <span className="cell-primary">{order.workingDays}</span>
                <span className="cell-sub"> / {order.requiredDays} needed</span>
              </td>
              <td className="mono">{formatDate(order.startDate)}</td>
              <td className="mono">{formatDate(order.dueDate)}</td>
              <td className="cell-sub">{order.countryCode}</td>
              <td>
                <span className={STATUS_STYLES[order.status] ?? 'pill pill-plain pill-neutral'}>
                  {STATUS_LABELS[order.status] ?? order.status}
                </span>
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
      {[0, 1, 2, 3].map((row) => <div className="skeleton-row" key={row} />)}
    </div>
  );
}

function ErrorState({ message, onRetry }) {
  return (
    <div className="state state-error">
      <div className="state-title">Could not load orders</div>
      <div>{message}</div>
      <button className="btn" type="button" onClick={onRetry}>Try again</button>
    </div>
  );
}

function EmptyState() {
  return (
    <div className="state">
      <div className="state-title">No work orders yet</div>
      <div>Add one to see its holiday-aware risk flag.</div>
    </div>
  );
}

function NoMatchState({ query }) {
  return (
    <div className="state">
      <div className="state-title">No orders match &ldquo;{query.trim()}&rdquo;</div>
      <div>Try a product code, country, status or risk flag.</div>
    </div>
  );
}
