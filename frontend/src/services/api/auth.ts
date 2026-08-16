import { httpClient } from './httpClient';

export interface LoginInput {
  email: string;
  senha: string;
}

export interface LoginResponse {
  token: string;
  email: string;
}

export const authApi = {
  login: (data: LoginInput) => httpClient.post<LoginResponse>('/auth/login', data),
};
