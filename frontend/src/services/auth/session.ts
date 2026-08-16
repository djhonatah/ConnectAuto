const TOKEN_KEY = 'connectauto:token';
const EMAIL_KEY = 'connectauto:email';

/**
 * Sessão do usuário autenticado, persistida em localStorage para sobreviver
 * a reloads. Não há refresh token: o JWT expira (8h, configurado no backend)
 * e o usuário precisa logar de novo — ver SESSION_EXPIRED_EVENT abaixo.
 */
export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function getStoredEmail(): string | null {
  return localStorage.getItem(EMAIL_KEY);
}

export function setSession(token: string, email: string): void {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(EMAIL_KEY, email);
}

export function clearSession(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(EMAIL_KEY);
}

/**
 * Disparado pelo httpClient quando uma requisição autenticada volta 401
 * (token expirado ou inválido), para a aplicação reagir deslogando o
 * usuário sem acoplar o httpClient a React/contexto.
 */
export const SESSION_EXPIRED_EVENT = 'connectauto:session-expired';
