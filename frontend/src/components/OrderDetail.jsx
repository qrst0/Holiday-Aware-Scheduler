import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import { ApiError, deleteOrder, fetchOrder, updateOrder } from '../api/client';
import HolidayList from './HolidayList';
import RiskBadge from './RiskBadge';
import SourceNotice from './SourceNotice';

const STATUSES = ['PLANNED', 'IN_PROGRESS', 'DONE', 'CANCELLED'];

function toForm(order) {
  return {
    productCode: order.productCode,
    quantity: String(order.quantity),
    countryCode: order.countryCode,
    startDate: order.startDate,
    dueDate: order.dueDate,
    requiredDays: String(order.requiredDays),
    status: order.status
  };
}

export default function OrderDetail() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [order, setOrder] = useState(null);
  const [form, setForm] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});
  const [saveError, setSaveError] = useState(null);
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [confirmingDelete, setConfirmingDelete] = useState(false);

  const load = useCallback(() => {
    setLoading(true);
    setError(null);
    fetchOrder(id)
      .then((loaded) => {
        setOrder(loaded);
        setForm(toForm(loaded));
      })
      .catch((cause) => setError(cause.message))
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(load, [load]);

  const update = (field) => (event) => {
    setForm({ ...form, [field]: event.target.value });
    setSaved(false);
  };

  function changedFields() {
    const original = toForm(order);
    const patch = {};
    Object.keys(form).forEach((field) => {
      if (form[field] !== original[field]) {
        patch[field] = field === 'quantity' || field === 'requiredDays'
          ? Number(form[field])
          : form[field];
      }
    });
    return patch;
  }

  function handleSave(event) {
    event.preventDefault();
    const patch = changedFields();
    if (Object.keys(patch).length === 0) {
      setSaved(true);
      return;
    }

    setSaving(true);
    setFieldErrors({});
    setSaveError(null);
    updateOrder(id, patch)
      .then((updated) => {
        setOrder(updated);
        setForm(toForm(updated));
        setSaved(true);
      })
      .catch((cause) => {
        if (cause instanceof ApiError && cause.fieldErrors) {
          setFieldErrors(cause.fieldErrors);
        }
        setSaveError(cause.message);
      })
      .finally(() => setSaving(false));
  }

  function handleDelete() {
    setSaving(true);
    deleteOrder(id)
      .then(() => navigate('/'))
      .catch((cause) => {
        setSaveError(cause.message);
        setSaving(false);
      });
  }

  if (loading) {
    return <div className="card state">Loading order…</div>;
  }

  if (error) {
    return (
      <div className="card state state-error">
        <div className="state-title">Could not load this order</div>
        <div>{error}</div>
        <button className="btn" type="button" onClick={() => navigate('/')}>Back to orders</button>
      </div>
    );
  }

  const field = (name, label, input) => (
    <label className="field">
      <span className="field-label">{label}</span>
      {input}
      {fieldErrors[name] && <span className="field-error">{fieldErrors[name]}</span>}
    </label>
  );

  return (
    <>
      <div className="content-head">
        <div>
          <button className="link-back" type="button" onClick={() => navigate('/')}>
            &lsaquo; Work orders
          </button>
          <h1 className="page-title">{order.productCode}</h1>
        </div>
        <div className="head-actions">
          <RiskBadge flag={order.riskFlag} />
        </div>
      </div>

      <SourceNotice source={order.holidaySource} />

      <div className="detail-grid">
        <section className="card detail-card">
          <h2 className="section-title">Working days status</h2>
          <dl className="facts">
            <div>
              <dt>Working days available</dt>
              <dd className="mono cell-primary">{order.workingDays}</dd>
            </div>
            <div>
              <dt>Working days needed</dt>
              <dd className="mono cell-primary">{order.requiredDays}</dd>
            </div>
          </dl>

          <h2 className="section-title">Holidays in this window</h2>
          <HolidayList holidays={order.holidaysInWindow} />
        </section>

        <form className="card detail-card" onSubmit={handleSave} noValidate>
          <h2 className="section-title">Edit order</h2>
          <div className="form-grid">
            {field('productCode', 'Product code',
              <input className="input" value={form.productCode} onChange={update('productCode')} />)}
            {field('quantity', 'Quantity',
              <input className="input" type="number" value={form.quantity} onChange={update('quantity')} />)}
            {field('countryCode', 'Country',
              <input className="input" value={form.countryCode} onChange={update('countryCode')} maxLength={2} />)}
            {field('startDate', 'Start date',
              <input className="input" type="date" value={form.startDate} onChange={update('startDate')} />)}
            {field('dueDate', 'Due date',
              <input className="input" type="date" value={form.dueDate} onChange={update('dueDate')} />)}
            {field('requiredDays', 'Working days needed',
              <input className="input" type="number" value={form.requiredDays} onChange={update('requiredDays')} />)}
            {field('status', 'Status',
              <select className="input" value={form.status} onChange={update('status')}>
                {STATUSES.map((status) => <option key={status} value={status}>{status}</option>)}
              </select>)}
          </div>

          {saveError && <div className="form-error">{saveError}</div>}
          {saved && !saveError && <div className="notice notice-ok">Saved.</div>}

          <div className="form-actions">
            {confirmingDelete ? (
              <>
                <span className="confirm-text">Delete this order?</span>
                <button className="btn" type="button" onClick={() => setConfirmingDelete(false)} disabled={saving}>
                  Keep
                </button>
                <button className="btn btn-danger" type="button" onClick={handleDelete} disabled={saving}>
                  Delete
                </button>
              </>
            ) : (
              <>
                <button className="btn" type="button" onClick={() => setConfirmingDelete(true)} disabled={saving}>
                  Delete
                </button>
                <button className="btn btn-primary" type="submit" disabled={saving}>
                  {saving ? 'Saving…' : 'Save changes'}
                </button>
              </>
            )}
          </div>
        </form>
      </div>
    </>
  );
}
