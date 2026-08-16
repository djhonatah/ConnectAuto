import { render, screen } from '@testing-library/react';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { RequireAuth } from './RequireAuth';
import { AuthProvider } from '../context/AuthContext';
import { setSession, clearSession } from '../services/auth/session';

function renderWithRouter() {
  const router = createMemoryRouter(
    [
      {
        path: '/',
        element: (
          <RequireAuth>
            <p>Conteúdo protegido</p>
          </RequireAuth>
        ),
      },
      { path: '/login', element: <p>Tela de login</p> },
    ],
    { initialEntries: ['/'] },
  );

  render(
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>,
  );
}

describe('RequireAuth', () => {
  afterEach(() => {
    clearSession();
  });

  it('redirects to /login when there is no session', () => {
    renderWithRouter();

    expect(screen.getByText('Tela de login')).toBeInTheDocument();
    expect(screen.queryByText('Conteúdo protegido')).not.toBeInTheDocument();
  });

  it('renders the protected content when a session exists', () => {
    setSession('token-fake', 'admin@connectauto.com.br', true);

    renderWithRouter();

    expect(screen.getByText('Conteúdo protegido')).toBeInTheDocument();
  });
});
