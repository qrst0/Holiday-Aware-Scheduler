const VARIANTS = {
  ON_TRACK: { className: 'badge badge-ok', label: 'On track' },
  AT_RISK: { className: 'badge badge-risk', label: 'At risk' },
  UNKNOWN: { className: 'badge badge-unknown', label: 'Unknown' }
};

export default function RiskBadge({ flag }) {
  const variant = VARIANTS[flag] ?? VARIANTS.UNKNOWN;
  return <span className={variant.className}>{variant.label}</span>;
}
