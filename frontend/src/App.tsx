import { Link, NavLink, Outlet } from 'react-router-dom';
import './App.css';

function App() {
  return (
    <>
      <header className="app-header">
        <Link to="/" className="brand">
          ConnectAuto
        </Link>
        <nav>
          <NavLink to="/" end>
            Início
          </NavLink>
          <NavLink to="/sobre">Sobre</NavLink>
        </nav>
      </header>

      <main className="app-main">
        <Outlet />
      </main>
    </>
  );
}

export default App;
