import { createContext } from 'react';

export interface AuthContextValue {
  isAuthenticated: boolean;
  email: string | null;
  login: (token: string, email: string) => void;
  logout: () => void;
}

// Objeto de contexto isolado num módulo não-componente: mantém
// AuthContext.tsx exportando só o componente AuthProvider (Fast Refresh).
export const AuthContext = createContext<AuthContextValue | undefined>(undefined);
