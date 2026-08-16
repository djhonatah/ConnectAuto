import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, createMemoryRouter, RouterProvider } from 'react-router-dom';
import { LoginPage } from './LoginPage';
import { AuthProvider } from '../context/AuthContext';
import { authApi } from '../services/api/auth';

vi.mock('../services/api/auth', () => ({
  authApi: {
    login: vi.fn(),
  },
}));

function renderLoginPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <MemoryRouter initialEntries={['/login']}>
          <LoginPage />
        </MemoryRouter>
      </AuthProvider>
    </QueryClientProvider>,
  );
}

describe('LoginPage', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.mocked(authApi.login).mockReset();
  });

  it('shows validation errors when submitting empty fields', async () => {
    const user = userEvent.setup();
    renderLoginPage();

    await user.click(screen.getByRole('button', { name: 'Entrar' }));

    expect(await screen.findByText('Senha é obrigatória')).toBeInTheDocument();
    expect(screen.getByLabelText('E-mail')).toHaveAttribute('aria-invalid', 'true');
    expect(authApi.login).not.toHaveBeenCalled();
  });

  it('shows an error message when credentials are invalid', async () => {
    vi.mocked(authApi.login).mockRejectedValueOnce(new Error('E-mail ou senha inválidos.'));
    const user = userEvent.setup();
    renderLoginPage();

    await user.type(screen.getByLabelText('E-mail'), 'admin@connectauto.com.br');
    await user.type(screen.getByLabelText('Senha'), 'senha-errada');
    await user.click(screen.getByRole('button', { name: 'Entrar' }));

    expect(await screen.findByText('E-mail ou senha inválidos.')).toBeInTheDocument();
  });

  it('logs in and redirects to the originally requested page', async () => {
    vi.mocked(authApi.login).mockResolvedValueOnce({
      token: 'token-fake',
      email: 'admin@connectauto.com.br',
    });
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    const router = createMemoryRouter(
      [
        { path: '/login', element: <LoginPage /> },
        { path: '/veiculos', element: <p>Página de veículos</p> },
      ],
      { initialEntries: [{ pathname: '/login', state: { from: { pathname: '/veiculos' } } }] },
    );

    const user = userEvent.setup();
    render(
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <RouterProvider router={router} />
        </AuthProvider>
      </QueryClientProvider>,
    );

    await user.type(screen.getByLabelText('E-mail'), 'admin@connectauto.com.br');
    await user.type(screen.getByLabelText('Senha'), 'connectauto123');
    await user.click(screen.getByRole('button', { name: 'Entrar' }));

    expect(await screen.findByText('Página de veículos')).toBeInTheDocument();
    expect(localStorage.getItem('connectauto:token')).toBe('token-fake');
  });
});
