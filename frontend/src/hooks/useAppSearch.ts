import { useOutletContext } from 'react-router-dom';

export interface AppOutletContext {
  query: string;
}

export function useAppSearch(): string {
  return useOutletContext<AppOutletContext | null>()?.query ?? '';
}
