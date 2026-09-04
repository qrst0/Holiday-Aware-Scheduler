const VARIANTS = {
  ON_TRACK: { className: 'pill pill-ok', label: 'On track' },
  AT_RISK: { className: 'pill pill-risk', label: 'At risk' },
  UNKNOWN: { className: 'pill pill-neutral', label: 'Unknown' }
};

export default function RiskBadge({ flag }) {
  const variant = VARIANTS[flag] ?? VARIANTS.UNKNOWN;
  return <span className={variant.className}>{variant.label}</span>;
}
