import type { ReactNode } from 'react';

interface StatusMessageProps {
  kind: 'loading' | 'error';
  children: ReactNode;
}

export function StatusMessage({ kind, children }: StatusMessageProps) {
  return <p role={kind === 'error' ? 'alert' : 'status'}>{children}</p>;
}
