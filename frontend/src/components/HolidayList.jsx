function formatDate(isoDate) {
  const [year, month, day] = isoDate.split('-');
  return `${day}/${month}/${year}`;
}

export default function HolidayList({ holidays }) {
  if (holidays.length === 0) {
    return <p className="muted">No public holidays fall inside this window.</p>;
  }
  return (
    <ul className="holiday-list">
      {holidays.map((holiday) => (
        <li key={holiday.date}>
          <span className="mono">{formatDate(holiday.date)}</span>
          <span>{holiday.name}</span>
        </li>
      ))}
    </ul>
  );
}
