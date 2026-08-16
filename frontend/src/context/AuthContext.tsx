import { useCallback, useEffect, useState, type ReactNode } from 'react';
import { queryClient } from '../config/queryClient';
import {
  clearSession,
  getStoredEmail,
  getToken,
  setSession,
  SESSION_EXPIRED_EVENT,
} from '../services/auth/session';
import { AuthContext } from './auth-context';

export function AuthProvider({ children }: { children: ReactNode }) {
  const [email, setEmail] = useState<string | null>(() => (getToken() ? getStoredEmail() : null));

  const logout = useCallback(() => {
    clearSession();
    setEmail(null);
    // Estoque/concessionárias em cache pertencem à sessão que acabou de sair.
    queryClient.clear();
  }, []);

  const login = useCallback((token: string, userEmail: string, remember: boolean) => {
    setSession(token, userEmail, remember);
    setEmail(userEmail);
  }, []);

  useEffect(() => {
    window.addEventListener(SESSION_EXPIRED_EVENT, logout);
    return () => window.removeEventListener(SESSION_EXPIRED_EVENT, logout);
  }, [logout]);

  return (
    <AuthContext.Provider value={{ isAuthenticated: email !== null, email, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
