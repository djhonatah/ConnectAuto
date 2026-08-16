import { Link, NavLink, useNavigate } from 'react-router-dom';
import { BrandMark } from './BrandMark';
import { useAuth } from '../hooks/useAuth';
import './Sidebar.css';

const NAV_ITEMS = [
  {
    to: '/',
    label: 'Início',
    end: true,
    icon: (
      <>
        <rect x="3" y="3" width="7" height="9" rx="1.5" />
        <rect x="14" y="3" width="7" height="5" rx="1.5" />
        <rect x="14" y="12" width="7" height="9" rx="1.5" />
        <rect x="3" y="16" width="7" height="5" rx="1.5" />
      </>
    ),
  },
  {
    to: '/veiculos',
    label: 'Veículos',
    end: false,
    icon: (
      <>
        <path d="M3 13l1.5-5a2 2 0 0 1 2-1.4h11a2 2 0 0 1 2 1.4L21 13" />
        <path d="M3 13h18v5a1 1 0 0 1-1 1h-1.5a1 1 0 0 1-1-1v-1h-11v1a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1z" />
        <circle cx="7.5" cy="16" r="1.4" />
        <circle cx="16.5" cy="16" r="1.4" />
      </>
    ),
  },
  {
    to: '/concessionarias',
    label: 'Concessionárias',
    end: false,
    icon: <path d="M3 21h18M5 21V7l7-4 7 4v14M9 21v-6h6v6" />,
  },
  {
    to: '/sobre',
    label: 'Sobre',
    end: false,
    icon: (
      <>
        <circle cx="12" cy="12" r="9" />
        <path d="M12 16v-4.5" />
        <path d="M12 8h.01" />
      </>
    ),
  },
];

export function Sidebar() {
  const { email, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  return (
    <aside className="sidebar">
      <Link to="/" className="sidebar__brand" aria-label="ConnectAuto, ir para o início">
        <BrandMark className="sidebar__mark" />
        <span className="sidebar__wordmark">
          Connect<strong>Auto</strong>
        </span>
      </Link>

      <nav aria-label="Navegação principal" className="sidebar__nav">
        <span className="sidebar__section-label">Geral</span>
        {NAV_ITEMS.map((item) => (
          <NavLink key={item.to} to={item.to} end={item.end} className="sidebar__nav-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              {item.icon}
            </svg>
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="sidebar__footer">
        {email && <span className="sidebar__user">{email}</span>}
        <button type="button" className="sidebar__logout" onClick={handleLogout}>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
            <path d="M16 17l5-5-5-5" />
            <path d="M21 12H9" />
          </svg>
          <span>Sair</span>
        </button>
      </div>
    </aside>
  );
}
