import { useAuth } from '../hooks/useAuth';
import './Topbar.css';

interface TopbarProps {
  query: string;
  onQueryChange: (query: string) => void;
}

function initialsFromEmail(email: string | null): string {
  if (!email) return '?';
  const name = email.split('@')[0];
  const parts = name.split(/[._-]/).filter(Boolean);
  const chars = parts.length > 1 ? [parts[0][0], parts[1][0]] : [name[0], name[1] ?? ''];
  return chars.join('').toUpperCase();
}

export function Topbar({ query, onQueryChange }: TopbarProps) {
  const { email } = useAuth();

  return (
    <header className="topbar">
      <label className="topbar__search">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <circle cx="11" cy="11" r="7" />
          <path d="M21 21l-4.3-4.3" />
        </svg>
        <input
          type="search"
          placeholder="Buscar veículo, chassi ou concessionária…"
          value={query}
          onChange={(e) => onQueryChange(e.target.value)}
          aria-label="Buscar"
        />
      </label>

      <div className="topbar__actions">
        <div className="topbar__avatar" title={email ?? undefined}>
          {initialsFromEmail(email)}
        </div>
      </div>
    </header>
  );
}
