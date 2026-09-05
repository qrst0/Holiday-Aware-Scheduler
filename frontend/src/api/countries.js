const CODES = [
  'AD', 'AG', 'AI', 'AL', 'AM', 'AO', 'AR', 'AT', 'AU', 'AW', 'AX', 'BA', 'BB', 'BD', 'BE',
  'BF', 'BG', 'BH', 'BI', 'BJ', 'BL', 'BM', 'BO', 'BQ', 'BR', 'BS', 'BW', 'BY', 'BZ', 'CA',
  'CC', 'CD', 'CF', 'CG', 'CH', 'CI', 'CK', 'CL', 'CM', 'CN', 'CO', 'CR', 'CU', 'CV', 'CW',
  'CX', 'CY', 'CZ', 'DE', 'DJ', 'DK', 'DM', 'DO', 'DZ', 'EC', 'EE', 'EG', 'ER', 'ES', 'ET',
  'FI', 'FK', 'FM', 'FO', 'FR', 'GA', 'GB', 'GD', 'GE', 'GF', 'GG', 'GH', 'GI', 'GL', 'GM',
  'GN', 'GP', 'GQ', 'GR', 'GT', 'GW', 'GY', 'HK', 'HN', 'HR', 'HT', 'HU', 'ID', 'IE', 'IM',
  'IQ', 'IS', 'IT', 'JE', 'JM', 'JP', 'KE', 'KH', 'KI', 'KM', 'KN', 'KR', 'KY', 'KZ', 'LC',
  'LI', 'LR', 'LS', 'LT', 'LU', 'LV', 'LY', 'MA', 'MC', 'MD', 'ME', 'MF', 'MG', 'MH', 'MK',
  'ML', 'MN', 'MP', 'MQ', 'MR', 'MS', 'MT', 'MW', 'MX', 'MZ', 'NA', 'NC', 'NE', 'NF', 'NG',
  'NI', 'NL', 'NO', 'NR', 'NU', 'NZ', 'PA', 'PE', 'PF', 'PG', 'PH', 'PL', 'PM', 'PN', 'PR',
  'PT', 'PW', 'PY', 'RO', 'RS', 'RU', 'RW', 'SB', 'SC', 'SD', 'SE', 'SG', 'SH', 'SI', 'SJ',
  'SK', 'SL', 'SM', 'SN', 'SO', 'SR', 'SS', 'ST', 'SV', 'SX', 'SY', 'SZ', 'TC', 'TD', 'TG',
  'TK', 'TN', 'TO', 'TR', 'TT', 'TV', 'TZ', 'UA', 'UG', 'US', 'UY', 'VA', 'VC', 'VE', 'VG',
  'VI', 'VN', 'VU', 'WF', 'WS', 'YE', 'ZA', 'ZM', 'ZW'
];

const PINNED = ['ID', 'KR', 'JP', 'SG'];

const regionNames = typeof Intl !== 'undefined' && Intl.DisplayNames
  ? new Intl.DisplayNames(['en'], { type: 'region' })
  : null;

function nameFor(code) {
  try {
    return regionNames?.of(code) ?? code;
  } catch {
    return code;
  }
}

function flagFor(code) {
  return String.fromCodePoint(...[...code].map((letter) => 0x1f1e6 + letter.charCodeAt(0) - 65));
}

const byName = CODES
  .filter((code) => !PINNED.includes(code))
  .map((code) => ({ code, name: nameFor(code), flag: flagFor(code) }))
  .sort((a, b) => a.name.localeCompare(b.name));

export const COUNTRIES = [
  ...PINNED.map((code) => ({ code, name: nameFor(code), flag: flagFor(code) })),
  ...byName
];

export function countryFor(code) {
  if (!code) {
    return null;
  }
  const upper = code.toUpperCase();
  return COUNTRIES.find((country) => country.code === upper)
    ?? { code: upper, name: nameFor(upper), flag: flagFor(upper) };
}
