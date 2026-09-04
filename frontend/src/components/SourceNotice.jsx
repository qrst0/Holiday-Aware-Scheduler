const NOTICES = {
  CACHE_STALE: {
    className: 'notice notice-warn',
    text: 'The holiday API was unreachable, so this uses the last cached calendar. The flag may be out of date.'
  },
  UNAVAILABLE: {
    className: 'notice notice-warn',
    text: 'No holiday data available, so working days exclude weekends only and the risk flag is unknown.'
  }
};

export default function SourceNotice({ source }) {
  const notice = NOTICES[source];
  return notice ? <div className={notice.className}>{notice.text}</div> : null;
}
