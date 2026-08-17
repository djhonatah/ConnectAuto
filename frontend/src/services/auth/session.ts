const TOKEN_KEY = 'connectauto:token';
const EMAIL_KEY = 'connectauto:email';

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

export const SESSION_EXPIRED_EVENT = 'connectauto:session-expired';
