import { render, screen } from '@testing-library/react';
import { StatusMessage } from './StatusMessage';

describe('StatusMessage', () => {
  it('renders loading state with status role and spinner', () => {
    render(<StatusMessage kind="loading">Carregando…</StatusMessage>);
    expect(screen.getByRole('status')).toHaveTextContent('Carregando…');
  });

  it('renders error state with alert role', () => {
    render(<StatusMessage kind="error">Falhou</StatusMessage>);
    expect(screen.getByRole('alert')).toHaveTextContent('Falhou');
  });

  it('renders the action slot', () => {
    render(
      <StatusMessage kind="error" action={<button>Tentar novamente</button>}>
        Falhou
      </StatusMessage>,
    );
    expect(screen.getByRole('button', { name: 'Tentar novamente' })).toBeInTheDocument();
  });
});
