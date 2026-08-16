const TOKEN_KEY = 'connectauto:token';
const EMAIL_KEY = 'connectauto:email';

/**
 * Sessão do usuário autenticado. "Lembrar-me" decide onde ela é persistida:
 * localStorage sobrevive a fechar o navegador, sessionStorage só dura a aba
 * atual. Não há refresh token: o JWT expira (8h, configurado no backend) e o
 * usuário precisa logar de novo — ver SESSION_EXPIRED_EVENT abaixo.
 */
export function getToken(): string | null {
  return sessionStorage.getItem(TOKEN_KEY) ?? localStorage.getItem(TOKEN_KEY);
}

export function getStoredEmail(): string | null {
  return sessionStorage.getItem(EMAIL_KEY) ?? localStorage.getItem(EMAIL_KEY);
}

export function setSession(token: string, email: string, remember: boolean): void {
  const storage = remember ? localStorage : sessionStorage;
  storage.setItem(TOKEN_KEY, token);
  storage.setItem(EMAIL_KEY, email);
}

export function clearSession(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(EMAIL_KEY);
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(EMAIL_KEY);
}

/**
 * Disparado pelo httpClient quando uma requisição autenticada volta 401
 * (token expirado ou inválido), para a aplicação reagir deslogando o
 * usuário sem acoplar o httpClient a React/contexto.
 */
export const SESSION_EXPIRED_EVENT = 'connectauto:session-expired';
