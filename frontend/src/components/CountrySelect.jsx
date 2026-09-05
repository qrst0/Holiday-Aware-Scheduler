import { useEffect, useMemo, useRef, useState } from 'react';

import { COUNTRIES, countryFor } from '../api/countries';

const MAX_VISIBLE = 60;

export default function CountrySelect({ value, onChange, invalid }) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState('');
  const [highlight, setHighlight] = useState(0);

  const wrapRef = useRef(null);
  const listRef = useRef(null);
  const keyboardMove = useRef(false);

  const selected = countryFor(value);

  const matches = useMemo(() => {
    const needle = query.trim().toLowerCase();
    if (!needle) {
      return COUNTRIES.slice(0, MAX_VISIBLE);
    }
    return COUNTRIES
      .filter((country) => country.name.toLowerCase().includes(needle)
        || country.code.toLowerCase().includes(needle))
      .slice(0, MAX_VISIBLE);
  }, [query]);

  useEffect(() => {
    setHighlight(0);
  }, [query]);

  useEffect(() => {
    if (!open) {
      return undefined;
    }
    function onDocumentDown(event) {
      if (!wrapRef.current?.contains(event.target)) {
        setOpen(false);
        setQuery('');
      }
    }
    document.addEventListener('mousedown', onDocumentDown);
    return () => document.removeEventListener('mousedown', onDocumentDown);
  }, [open]);

  // Only the keyboard scrolls the list. Doing it on hover moves the option out from under
  // the pointer, which is what made the form jump.
  useEffect(() => {
    if (!keyboardMove.current) {
      return;
    }
    keyboardMove.current = false;
    listRef.current?.querySelector('[data-active="true"]')?.scrollIntoView({ block: 'nearest' });
  }, [highlight, open]);

  function choose(country) {
    onChange(country.code);
    setOpen(false);
    setQuery('');
  }

  function onKeyDown(event) {
    if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
      event.preventDefault();
      if (!open) {
        setOpen(true);
        return;
      }
      const step = event.key === 'ArrowDown' ? 1 : -1;
      keyboardMove.current = true;
      setHighlight((current) => (current + step + matches.length) % matches.length);
    } else if (event.key === 'Enter' && open) {
      event.preventDefault();
      if (matches[highlight]) {
        choose(matches[highlight]);
      }
    } else if (event.key === 'Escape') {
      setOpen(false);
      setQuery('');
    }
  }

  const shown = open ? query : (selected ? `${selected.flag} ${selected.name}` : '');

  return (
    <div className="country-select" ref={wrapRef}>
      <input
        className={invalid ? 'input country-input country-input-invalid' : 'input country-input'}
        type="text"
        role="combobox"
        aria-expanded={open}
        aria-autocomplete="list"
        value={shown}
        placeholder="Search a country"
        onChange={(event) => {
          setQuery(event.target.value);
          setOpen(true);
        }}
        onFocus={() => setOpen(true)}
        onKeyDown={onKeyDown}
      />

      {open && (
        <ul className="country-list" ref={listRef} role="listbox">
          {matches.length === 0 && <li className="country-empty">No country matches</li>}
          {matches.map((country, index) => (
            <li key={country.code}>
              <button
                type="button"
                className={index === highlight ? 'country-option country-option-active' : 'country-option'}
                data-active={index === highlight}
                role="option"
                aria-selected={country.code === selected?.code}
                // mousedown fires before blur, so the click is not lost to the input closing.
                onMouseDown={(event) => {
                  event.preventDefault();
                  choose(country);
                }}
                onMouseEnter={() => setHighlight(index)}
              >
                <span className="country-flag">{country.flag}</span>
                <span className="country-name">{country.name}</span>
                <span className="country-code">{country.code}</span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
