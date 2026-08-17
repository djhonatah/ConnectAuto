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
  lembrar: z.boolean(),
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
    defaultValues: { email: '', senha: '', lembrar: true },
  });

  const redirectTo = (location.state as LocationState | null)?.from?.pathname ?? '/';

  if (isAuthenticated) {
    return <Navigate to={redirectTo} replace />;
  }

  async function onSubmit(values: LoginFormValues) {
    try {
      await login.mutateAsync({
        email: values.email,
        senha: values.senha,
        remember: values.lembrar,
      });
      navigate(redirectTo, { replace: true });
    } catch {
    }
  }

  return (
    <div className="login-page">
      <div className="login-page__visual">
        <div className="login-page__grid-tex" aria-hidden="true" />
        <div className="login-page__visual-copy">
          <div className="login-page__plate">
            <span className="login-page__rivet" />
            <BrandMark className="login-page__plate-mark" />
            <span className="login-page__rivet" />
          </div>
          <h2>Toda a frota e a rede de concessionárias, num só painel.</h2>
          <p>
            Cadastre veículos, associe concessionárias e acompanhe o estoque em tempo real — sem
            planilhas.
          </p>
        </div>
      </div>

      <div className="login-page__form-wrap">
        <div className="login-page__form-card">
          <div className="login-page__brand">
            <BrandMark className="login-page__mark" />
            <span>
              Connect<strong>Auto</strong>
            </span>
          </div>

          <h1 className="login-page__title">Entrar na sua conta</h1>
          <p className="login-page__subtitle">Acesse o painel de gestão comercial.</p>

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

            <label className="login-page__remember">
              <input type="checkbox" {...register('lembrar')} />
              Lembrar-me neste navegador
            </label>

            {login.isError && (
              <p className="login-page__form-error" role="alert">
                {login.error.message}
              </p>
            )}

            <button type="submit" className="login-page__submit" disabled={login.isPending}>
              {login.isPending ? 'Entrando…' : 'Entrar'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
