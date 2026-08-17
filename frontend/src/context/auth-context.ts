import { createContext } from 'react';

export interface AuthContextValue {
  isAuthenticated: boolean;
  email: string | null;
  login: (token: string, email: string, remember: boolean) => void;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined);
