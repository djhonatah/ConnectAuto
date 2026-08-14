/**
 * Base URL of the ConnectAuto backend API.
 * Configured via the VITE_API_URL environment variable (see .env.example).
 */
export const API_BASE_URL: string = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

if (!import.meta.env.VITE_API_URL) {
  console.warn('[config] VITE_API_URL is not set, falling back to default: ' + API_BASE_URL);
}
