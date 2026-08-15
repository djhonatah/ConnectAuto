import { QueryClient } from '@tanstack/react-query';

/**
 * Single QueryClient instance shared by the whole app.
 * Chamadas à API (services/api) devem ser feitas através de hooks do
 * TanStack Query (useQuery/useMutation) usando este client.
 */
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 60 * 1000, // 1 minuto antes de considerar os dados obsoletos
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});
