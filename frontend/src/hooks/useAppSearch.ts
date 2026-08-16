import { useOutletContext } from 'react-router-dom';

export interface AppOutletContext {
  query: string;
}

/** Busca digitada na topbar (App.tsx), disponível para as páginas via
 * contexto de rota. Fora do shell autenticado (ex.: testes que renderizam
 * uma página isolada) não há Outlet provendo o contexto — cai em ''. */
export function useAppSearch(): string {
  return useOutletContext<AppOutletContext | null>()?.query ?? '';
}
