import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import { clearHolidayCache, fetchDevStatus, seedOrders, setHolidayApi } from '../api/client';

export default function DevPanel() {
  const navigate = useNavigate();

  const [status, setStatus] = useState(null);
  const [loadError, setLoadError] = useState(null);
  const [busy, setBusy] = useState(false);
  const [seedCount, setSeedCount] = useState('10');
  const [feedback, setFeedback] = useState(null);

  const load = useCallback(() => {
    fetchDevStatus()
      .then(setStatus)
      .catch((cause) => setLoadError(cause.message));
  }, []);

  useEffect(load, [load]);

  function run(section, action, note) {
    setBusy(true);
    setFeedback(null);
    action()
      .then((next) => {
        setStatus(next);
        setFeedback({ section, text: note, ok: true });
      })
      .catch((cause) => setFeedback({ section, text: cause.message, ok: false }))
      .finally(() => setBusy(false));
  }

  function Feedback({ section }) {
    if (feedback?.section !== section) {
      return null;
    }
    return (
      <div className={feedback.ok ? 'notice notice-ok' : 'form-error'}>{feedback.text}</div>
    );
  }

  if (loadError && !status) {
    return (
      <div className="card state state-error">
        <div className="state-title">Dev tools unavailable</div>
        <div>{loadError}</div>
      </div>
    );
  }

  if (!status) {
    return <div className="card state">Loading…</div>;
  }

  const apiOn = status.holidayApiEnabled;

  return (
    <>
      <div className="content-head">
        <div>
          <button className="link-back" type="button" onClick={() => navigate('/')}>
            &lsaquo; Work orders
          </button>
          <h1 className="page-title">Dev tools</h1>
        </div>
      </div>

      <div className="detail-grid">
        <section className="card detail-card">
          <h2 className="section-title">Holiday API</h2>
          <div className="dev-row">
            <span className={apiOn ? 'pill pill-ok' : 'pill pill-risk'}>
              {apiOn ? 'Reachable' : 'Switched off'}
            </span>
            <button
              className={apiOn ? 'btn btn-danger' : 'btn btn-primary'}
              type="button"
              disabled={busy}
              onClick={() => run(
                'holiday',
                () => setHolidayApi(!apiOn),
                apiOn ? 'Holiday API switched off.' : 'Holiday API back on, retry backoff cleared.')}
            >
              {apiOn ? 'Switch off' : 'Switch on'}
            </button>
          </div>

          <h2 className="section-title">Cached holiday data</h2>
          <dl className="facts">
            <div>
              <dt>Cached holidays</dt>
              <dd className="mono cell-primary">{status.cachedHolidays}</dd>
            </div>
            <div>
              <dt>Tracked country/years</dt>
              <dd className="mono cell-primary">{status.trackedCalendars}</dd>
            </div>
          </dl>
          <div className="form-actions">
            <button
              className="btn"
              type="button"
              disabled={busy}
              onClick={() => run('holiday', clearHolidayCache, 'Holiday cache cleared.')}
            >
              Clear holiday cache
            </button>
          </div>

          <Feedback section="holiday" />
        </section>

        <section className="card detail-card">
          <h2 className="section-title">Seed orders</h2>
          <dl className="facts">
            <div>
              <dt>Orders in the database</dt>
              <dd className="mono cell-primary">{status.orderCount}</dd>
            </div>
          </dl>

          <div className="dev-row">
            <input
              className="input input-sm dev-count"
              type="number"
              min="1"
              max="200"
              value={seedCount}
              onChange={(event) => setSeedCount(event.target.value)}
              aria-label="Number of orders to seed"
            />
            <button
              className="btn btn-primary"
              type="button"
              disabled={busy}
              onClick={() => run('seed', () => seedOrders(Number(seedCount)), `Seeded ${seedCount} orders.`)}
            >
              {busy ? 'Working…' : 'Add random orders'}
            </button>
          </div>

          <Feedback section="seed" />
        </section>
      </div>
    </>
  );
}
