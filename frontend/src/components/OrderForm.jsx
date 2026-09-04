import { useState } from 'react';

import { ApiError, createOrder } from '../api/client';

const EMPTY = {
  productCode: '',
  quantity: '',
  countryCode: '',
  startDate: '',
  dueDate: '',
  requiredDays: '',
  status: 'PLANNED'
};

const STATUSES = ['PLANNED', 'IN_PROGRESS', 'DONE', 'CANCELLED'];

function toPayload(form) {
  const blankToNull = (value) => (value.trim() === '' ? null : value.trim());
  const toNumber = (value) => (value.trim() === '' ? null : Number(value));
  return {
    productCode: form.productCode,
    quantity: toNumber(form.quantity),
    countryCode: blankToNull(form.countryCode),
    startDate: blankToNull(form.startDate),
    dueDate: blankToNull(form.dueDate),
    requiredDays: toNumber(form.requiredDays),
    status: form.status
  };
}

export default function OrderForm({ closing, onCreated, onCancel, onExited }) {
  const [form, setForm] = useState(EMPTY);
  const [fieldErrors, setFieldErrors] = useState({});
  const [formError, setFormError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const update = (field) => (event) => {
    setForm({ ...form, [field]: event.target.value });
  };

  function handleSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    setFieldErrors({});
    setFormError(null);

    createOrder(toPayload(form))
      .then((created) => {
        setForm(EMPTY);
        onCreated(created);
      })
      .catch((cause) => {
        if (cause instanceof ApiError && cause.fieldErrors) {
          setFieldErrors(cause.fieldErrors);
        }
        setFormError(cause.message);
      })
      .finally(() => setSubmitting(false));
  }

  const field = (name, label, input) => (
    <label className="field">
      <span className="field-label">{label}</span>
      {input}
      {fieldErrors[name] && <span className="field-error">{fieldErrors[name]}</span>}
    </label>
  );

  function handleAnimationEnd(event) {
    if (event.target === event.currentTarget && closing) {
      onExited();
    }
  }

  return (
    <form
      className={closing ? 'card form-card form-card-closing' : 'card form-card'}
      onSubmit={handleSubmit}
      onAnimationEnd={handleAnimationEnd}
      noValidate
    >
      <div className="form-grid">
        {field('productCode', 'Product code',
          <input className="input" value={form.productCode} onChange={update('productCode')}
                 placeholder="WIDGET-A" />)}

        {field('quantity', 'Quantity',
          <input className="input" type="number" value={form.quantity} onChange={update('quantity')}
                 placeholder="100" />)}

        {field('countryCode', 'Country',
          <input className="input" value={form.countryCode} onChange={update('countryCode')}
                 placeholder="ID" maxLength={2} />)}

        {field('startDate', 'Start date',
          <input className="input" type="date" value={form.startDate} onChange={update('startDate')} />)}

        {field('dueDate', 'Due date',
          <input className="input" type="date" value={form.dueDate} onChange={update('dueDate')} />)}

        {field('requiredDays', 'Working days needed',
          <input className="input" type="number" value={form.requiredDays}
                 onChange={update('requiredDays')} placeholder="20" />)}

        {field('status', 'Status',
          <select className="input" value={form.status} onChange={update('status')}>
            {STATUSES.map((status) => (
              <option key={status} value={status}>{status}</option>
            ))}
          </select>)}
      </div>

      {formError && <div className="form-error">{formError}</div>}

      <div className="form-actions">
        <button className="btn btn-ghost" type="button" onClick={onCancel} disabled={submitting}>
          Cancel
        </button>
        <button className="btn btn-primary" type="submit" disabled={submitting}>
          {submitting ? 'Creating…' : 'Create order'}
        </button>
      </div>
    </form>
  );
}
