import { useMutation } from '@tanstack/react-query';
import { authApi, type LoginInput } from '../services/api/auth';
import { useAuth } from './useAuth';

interface LoginArgs extends LoginInput {
  remember: boolean;
}

export function useLogin() {
  const { login } = useAuth();

  return useMutation({
    mutationFn: ({ email, senha }: LoginArgs) => authApi.login({ email, senha }),
    onSuccess: (response, variables) => {
      login(response.token, response.email, variables.remember);
    },
  });
}
