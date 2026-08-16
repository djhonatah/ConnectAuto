import { useMutation } from '@tanstack/react-query';
import { authApi, type LoginInput } from '../services/api/auth';
import { useAuth } from './useAuth';

export function useLogin() {
  const { login } = useAuth();

  return useMutation({
    mutationFn: (data: LoginInput) => authApi.login(data),
    onSuccess: (response) => {
      login(response.token, response.email);
    },
  });
}
