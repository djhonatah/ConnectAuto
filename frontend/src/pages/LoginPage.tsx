import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { BrandMark } from '../components/BrandMark';
import { useAuth } from '../hooks/useAuth';
import { useLogin } from '../hooks/useLogin';
import './LoginPage.css';

const loginFormSchema = z.object({
  email: z.string().trim().min(1, 'E-mail é obrigatório').email('E-mail inválido'),
  senha: z.string().min(1, 'Senha é obrigatória'),
});

type LoginFormValues = z.infer<typeof loginFormSchema>;

interface LocationState {
  from?: { pathname: string };
}

export function LoginPage() {
  const { isAuthenticated } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const login = useLogin();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginFormSchema),
    defaultValues: { email: '', senha: '' },
  });

  const redirectTo = (location.state as LocationState | null)?.from?.pathname ?? '/';

  if (isAuthenticated) {
    return <Navigate to={redirectTo} replace />;
  }

  async function onSubmit(values: LoginFormValues) {
    try {
      await login.mutateAsync(values);
      navigate(redirectTo, { replace: true });
    } catch {
      // Erro já fica disponível via login.isError/error, exibido abaixo.
    }
  }

  return (
    <div className="login-page">
      <div className="login-page__card">
        <BrandMark className="login-page__mark" />
        <h1 className="login-page__title">
          Connect<strong>Auto</strong>
        </h1>
        <p className="login-page__subtitle">Entre para acessar o estoque de veículos.</p>

        <form className="login-page__form" onSubmit={handleSubmit(onSubmit)} noValidate>
          <div className="login-page__field">
            <label htmlFor="email">E-mail</label>
            <input
              id="email"
              type="email"
              autoComplete="username"
              {...register('email')}
              aria-invalid={!!errors.email}
            />
            {errors.email && <span className="login-page__error">{errors.email.message}</span>}
          </div>

          <div className="login-page__field">
            <label htmlFor="senha">Senha</label>
            <input
              id="senha"
              type="password"
              autoComplete="current-password"
              {...register('senha')}
              aria-invalid={!!errors.senha}
            />
            {errors.senha && <span className="login-page__error">{errors.senha.message}</span>}
          </div>

          {login.isError && (
            <p className="login-page__form-error" role="alert">
              {login.error.message}
            </p>
          )}

          <button
            type="submit"
            className="btn btn-primary login-page__submit"
            disabled={login.isPending}
          >
            {login.isPending ? 'Entrando…' : 'Entrar'}
          </button>
        </form>
      </div>
    </div>
  );
}
