import { useEffect, useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { Sidebar } from './components/Sidebar';
import { Topbar } from './components/Topbar';
import type { AppOutletContext } from './hooks/useAppSearch';
import './App.css';

function App() {
  const [query, setQuery] = useState('');
  const location = useLocation();

  useEffect(() => {

    setQuery('');
  }, [location.pathname]);

  const outletContext: AppOutletContext = { query };

  return (
    <div className="app-shell">
      <Sidebar />
      <main className="app-main">
        <Topbar query={query} onQueryChange={setQuery} hideSearch={location.pathname === '/'} />
        <div className="app-content">
          <Outlet context={outletContext} />
        </div>
      </main>
    </div>
  );
}

export default App;
