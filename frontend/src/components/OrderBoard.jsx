import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import { fetchOrders } from '../api/client';
import OrderForm from './OrderForm';
import RiskBadge from './RiskBadge';

const PAGE_SIZE = 10;

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

const EMPTY_PAGE = { content: [], page: 0, totalPages: 0, totalElements: 0, hasNext: false };

function formatDate(isoDate) {
  const [year, month, day] = isoDate.split('-');
  return `${day}/${month}/${year}`;
}

export default function OrderBoard() {
  const navigate = useNavigate();

  const [result, setResult] = useState(EMPTY_PAGE);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [formOpen, setFormOpen] = useState(false);
  const [formClosing, setFormClosing] = useState(false);
  const [selected, setSelected] = useState(() => new Set());

  const [search, setSearch] = useState('');
  const [appliedSearch, setAppliedSearch] = useState('');
  const [status, setStatus] = useState('');
  const [countryCode, setCountryCode] = useState('');
  const [riskFlag, setRiskFlag] = useState('');
  const [page, setPage] = useState(0);

  const searchRef = useRef(null);

  // Debounced so typing does not fire one request per keystroke.
  useEffect(() => {
    const timer = setTimeout(() => setAppliedSearch(search), 300);
    return () => clearTimeout(timer);
  }, [search]);

  // Any filter change invalidates the current page number.
  useEffect(() => {
    setPage(0);
  }, [appliedSearch, status, countryCode, riskFlag]);

  const load = useCallback(() => {
    setLoading(true);
    setError(null);
    fetchOrders({
      search: appliedSearch,
      status,
      countryCode,
      riskFlag,
      page,
      size: PAGE_SIZE,
      sort: 'id'
    })
      .then(setResult)
      .catch((cause) => setError(cause.message))
      .finally(() => setLoading(false));
  }, [appliedSearch, status, countryCode, riskFlag, page]);

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

  // With filters and paging on the server, a new order can belong on any page, so reload
  // rather than splicing it into the rows currently on screen.
  function handleCreated() {
    closeForm();
    load();
  }

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
      result.content.every((order) => current.has(order.id))
        ? new Set()
        : new Set(result.content.map((order) => order.id)));
  }

  const orders = result.content;
  const selectedCount = orders.filter((order) => selected.has(order.id)).length;
  const filtered = Boolean(appliedSearch || status || countryCode || riskFlag);

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
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Search product code"
              aria-label="Search product code"
            />
            <span className="kbd">Ctrl K</span>
          </div>

          <Pager
            page={result.page}
            totalPages={result.totalPages}
            onPrev={() => setPage((current) => Math.max(0, current - 1))}
            onNext={() => setPage((current) => current + 1)}
          />

          <div className="toolbar-filters">
            <select
              className="input input-sm"
              value={status}
              onChange={(event) => setStatus(event.target.value)}
              aria-label="Filter by status"
            >
              <option value="">All statuses</option>
              {Object.entries(STATUS_LABELS).map(([value, label]) => (
                <option key={value} value={value}>{label}</option>
              ))}
            </select>

            <select
              className="input input-sm"
              value={riskFlag}
              onChange={(event) => setRiskFlag(event.target.value)}
              aria-label="Filter by risk"
            >
              <option value="">All risks</option>
              <option value="ON_TRACK">On track</option>
              <option value="AT_RISK">At risk</option>
              <option value="UNKNOWN">Unknown</option>
            </select>

            <input
              className="input input-sm input-country"
              value={countryCode}
              onChange={(event) => setCountryCode(event.target.value)}
              placeholder="Country"
              maxLength={2}
              aria-label="Filter by country"
            />
          </div>

          {!loading && !error && (
            <span className="result-note">
              {selectedCount > 0 ? `${selectedCount} selected` : `${result.totalElements} total`}
            </span>
          )}
        </div>

        {loading && <LoadingState />}
        {!loading && error && <ErrorState message={error} onRetry={load} />}
        {!loading && !error && orders.length === 0 && <EmptyState filtered={filtered} />}
        {!loading && !error && orders.length > 0 && (
          <OrderTable
            orders={orders}
            selected={selected}
            onToggleOne={toggleOne}
            onToggleAll={toggleAll}
            onOpen={(id) => navigate(`/orders/${id}`)}
          />
        )}
      </div>
    </>
  );
}

function Pager({ page, totalPages, onPrev, onNext }) {
  const shown = totalPages === 0 ? 0 : page + 1;
  return (
    <div className="pager">
      <button
        className="btn btn-icon"
        type="button"
        onClick={onPrev}
        disabled={page <= 0}
        aria-label="Previous page"
      >
        &lsaquo;
      </button>
      <span className="pager-label mono">{shown} / {totalPages}</span>
      <button
        className="btn btn-icon"
        type="button"
        onClick={onNext}
        disabled={totalPages === 0 || page >= totalPages - 1}
        aria-label="Next page"
      >
        &rsaquo;
      </button>
    </div>
  );
}

function OrderTable({ orders, selected, onToggleOne, onToggleAll, onOpen }) {
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
            <tr
              key={order.id}
              className={selected.has(order.id) ? 'row-link row-selected' : 'row-link'}
              onClick={() => onOpen(order.id)}
            >
              <td className="checkbox-cell" onClick={(event) => event.stopPropagation()}>
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

function EmptyState({ filtered }) {
  return (
    <div className="state">
      <div className="state-title">
        {filtered ? 'No orders match these filters' : 'No work orders yet'}
      </div>
      <div>
        {filtered ? 'Try clearing the search or filters.' : 'Add one to see its holiday-aware risk flag.'}
      </div>
    </div>
  );
}
