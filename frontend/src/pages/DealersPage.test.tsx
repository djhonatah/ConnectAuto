import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { DealersPage } from './DealersPage';
import { dealersApi, type Dealer } from '../services/api/dealers';

vi.mock('../services/api/dealers', () => ({
  dealersApi: {
    listar: vi.fn(),
    buscarPorId: vi.fn(),
    listarVeiculos: vi.fn(),
    criar: vi.fn(),
    atualizar: vi.fn(),
    excluir: vi.fn(),
  },
}));

const autoCenterSilva: Dealer = {
  id: 1,
  razaoSocial: 'Auto Center Silva Ltda',
  cnpj: '11222333000181',
  endereco: {
    logradouro: 'Avenida Paulista',
    bairro: 'Bela Vista',
    cidade: 'São Paulo',
    estado: 'SP',
    cep: '01310100',
  },
};

const veiculosNorte: Dealer = {
  id: 2,
  razaoSocial: 'Veiculos Norte Comercio Ltda',
  cnpj: '11444777000161',
  endereco: {
    logradouro: 'Avenida Paulista',
    bairro: 'Bela Vista',
    cidade: 'São Paulo',
    estado: 'SP',
    cep: '01310100',
  },
};

function renderPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/concessionarias']}>
        <DealersPage />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('DealersPage', () => {
  beforeEach(() => {
    vi.mocked(dealersApi.listar).mockReset().mockResolvedValue([autoCenterSilva, veiculosNorte]);
    vi.mocked(dealersApi.excluir).mockReset().mockResolvedValue(undefined);
  });

  it('shows a loading state and then the dealer list', async () => {
    renderPage();

    expect(screen.getByText('Carregando concessionárias…')).toBeInTheDocument();

    expect(await screen.findByText('Auto Center Silva Ltda')).toBeInTheDocument();
    expect(screen.getByText('Veiculos Norte Comercio Ltda')).toBeInTheDocument();
    expect(screen.getByText('2 concessionárias cadastradas')).toBeInTheDocument();
    expect(screen.getByText('11.222.333/0001-81')).toBeInTheDocument();
  });

  it('shows an error state with a retry action', async () => {
    vi.mocked(dealersApi.listar).mockReset().mockRejectedValueOnce(new Error('Falha ao buscar'));
    renderPage();

    expect(
      await screen.findByText(/Não foi possível carregar as concessionárias/),
    ).toBeInTheDocument();

    vi.mocked(dealersApi.listar).mockResolvedValueOnce([]);
    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'Tentar novamente' }));

    expect(await screen.findByText('Nenhuma concessionária cadastrada.')).toBeInTheDocument();
  });

  it('shows an empty state message when there are no dealers', async () => {
    vi.mocked(dealersApi.listar).mockReset().mockResolvedValue([]);
    renderPage();

    expect(await screen.findByText('Nenhuma concessionária cadastrada.')).toBeInTheDocument();
  });

  it('links each row to its vehicles and edit pages', async () => {
    renderPage();

    const row = await screen.findByText('Auto Center Silva Ltda').then((el) => el.closest('tr')!);
    expect(within(row).getByRole('link', { name: 'Ver veículos' })).toHaveAttribute(
      'href',
      '/concessionarias/1/veiculos',
    );
    expect(within(row).getByRole('link', { name: 'Editar' })).toHaveAttribute(
      'href',
      '/concessionarias/1/editar',
    );
  });

  it('keeps the dealer when the delete confirmation is cancelled', async () => {
    const user = userEvent.setup();
    renderPage();

    await screen.findByText('Auto Center Silva Ltda');
    const row = screen.getByText('Auto Center Silva Ltda').closest('tr')!;
    await user.click(within(row).getByRole('button', { name: 'Excluir' }));

    const dialog = await screen.findByRole('dialog');
    await user.click(within(dialog).getByRole('button', { name: 'Cancelar' }));

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(dealersApi.excluir).not.toHaveBeenCalled();
    expect(screen.getByText('Auto Center Silva Ltda')).toBeInTheDocument();
  });

  it('deletes a dealer and shows a success message', async () => {
    vi.mocked(dealersApi.listar)
      .mockReset()
      .mockResolvedValueOnce([autoCenterSilva, veiculosNorte])
      .mockResolvedValue([veiculosNorte]);
    const user = userEvent.setup();
    renderPage();

    await screen.findByText('Auto Center Silva Ltda');
    const row = screen.getByText('Auto Center Silva Ltda').closest('tr')!;
    await user.click(within(row).getByRole('button', { name: 'Excluir' }));

    const dialog = await screen.findByRole('dialog');
    await user.click(within(dialog).getByRole('button', { name: 'Excluir' }));

    expect(await screen.findByText(/excluída com sucesso/)).toBeInTheDocument();
    expect(dealersApi.excluir).toHaveBeenCalledWith(1);
    await waitFor(() =>
      expect(screen.queryByText('Auto Center Silva Ltda')).not.toBeInTheDocument(),
    );
  });

  it('shows an error message when deleting fails', async () => {
    vi.mocked(dealersApi.excluir)
      .mockReset()
      .mockRejectedValueOnce(new Error('Erro 409 ao excluir'));
    const user = userEvent.setup();
    renderPage();

    await screen.findByText('Auto Center Silva Ltda');
    const row = screen.getByText('Auto Center Silva Ltda').closest('tr')!;
    await user.click(within(row).getByRole('button', { name: 'Excluir' }));

    const dialog = await screen.findByRole('dialog');
    await user.click(within(dialog).getByRole('button', { name: 'Excluir' }));

    expect(await screen.findByText('Erro 409 ao excluir')).toBeInTheDocument();
    expect(screen.getByText('Auto Center Silva Ltda')).toBeInTheDocument();
  });
});
