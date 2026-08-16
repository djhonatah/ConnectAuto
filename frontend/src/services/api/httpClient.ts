import { API_BASE_URL } from '../../config/env';
import { getToken, SESSION_EXPIRED_EVENT } from '../auth/session';

export class ApiError extends Error {
  readonly status: number;

  constructor(status: number, message: string) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

/**
 * Wrapper fino sobre fetch: resolve a URL contra API_BASE_URL, define
 * headers JSON por padrão (mais o token de sessão, se houver) e lança
 * ApiError em respostas não-2xx.
 */
async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const token = getToken();
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...init?.headers,
    },
    ...init,
  });

  if (!response.ok) {
    // 401 com token anexado = sessão expirou/invalidou no servidor (não um
    // simples login errado, que não tem token). Avisa a aplicação reagir.
    if (response.status === 401 && token) {
      window.dispatchEvent(new Event(SESSION_EXPIRED_EVENT));
    }
    throw new ApiError(response.status, await extractErrorMessage(response, path));
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

async function extractErrorMessage(response: Response, path: string): Promise<string> {
  try {
    const body = (await response.json()) as { message?: string; details?: string[] };
    if (body.details?.length) return body.details.join('; ');
    if (body.message) return body.message;
  } catch {
    /* corpo não é JSON, cai na mensagem genérica abaixo */
  }
  return `Erro ${response.status} ao chamar ${path}`;
}

export const httpClient = {
  get: <T>(path: string) => request<T>(path, { method: 'GET' }),
  post: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'POST', body: JSON.stringify(body) }),
  put: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'PUT', body: JSON.stringify(body) }),
  patch: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'PATCH', body: JSON.stringify(body) }),
  delete: <T>(path: string) => request<T>(path, { method: 'DELETE' }),
};
