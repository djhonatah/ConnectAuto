import type { ReactNode } from 'react';
import './StatusMessage.css';

interface StatusMessageProps {
  kind: 'loading' | 'error';
  children: ReactNode;
  action?: ReactNode;
}

export function StatusMessage({ kind, children, action }: StatusMessageProps) {
  return (
    <div
      className={`status-message status-message--${kind}`}
      role={kind === 'error' ? 'alert' : 'status'}
    >
      {kind === 'loading' && <span className="status-message__spinner" aria-hidden="true" />}
      <p>{children}</p>
      {action}
    </div>
  );
}
