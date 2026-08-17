import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { VehiclesPage } from './VehiclesPage';
import { vehiclesApi, type Vehicle } from '../services/api/vehicles';
import { dealersApi, type Dealer } from '../services/api/dealers';

vi.mock('../services/api/vehicles', () => ({
  vehiclesApi: {
    listar: vi.fn(),
    buscarPorId: vi.fn(),
    criar: vi.fn(),
    atualizar: vi.fn(),
    excluir: vi.fn(),
  },
  FUEL_LABELS: { FLEX: 'Flex' },
}));

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

const corolla: Vehicle = {
  id: 1,
  marca: 'Toyota',
  modelo: 'Corolla',
  tipoCombustivel: 'FLEX',
  cor: 'Prata',
  ano: 2022,
  chassi: '9BWZZZ377VT004251',
  valor: 120000,
  corInterna: 'Preto',
  dealerId: null,
};

const civic: Vehicle = {
  id: 2,
  marca: 'Honda',
  modelo: 'Civic',
  tipoCombustivel: 'FLEX',
  cor: 'Preto',
  ano: 2023,
  chassi: '9BWZZZ377VT004252',
  valor: 135000,
  corInterna: 'Bege',
  dealerId: null,
};

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

function renderPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/veiculos']}>
        <VehiclesPage />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('VehiclesPage', () => {
  beforeEach(() => {
    vi.mocked(vehiclesApi.listar).mockReset().mockResolvedValue([corolla, civic]);
    vi.mocked(vehiclesApi.excluir).mockReset().mockResolvedValue(undefined);
    vi.mocked(dealersApi.listar).mockReset().mockResolvedValue([autoCenterSilva]);
  });

  it('shows a loading state and then the vehicle list', async () => {
    renderPage();

    expect(screen.getByText('Carregando veículos…')).toBeInTheDocument();

    expect(await screen.findByText('Corolla')).toBeInTheDocument();
    expect(screen.getByText('Civic')).toBeInTheDocument();
    expect(screen.getByText('2 veículos cadastrados')).toBeInTheDocument();
  });

  it('shows an error state with a retry action', async () => {
    vi.mocked(vehiclesApi.listar).mockReset().mockRejectedValueOnce(new Error('Falha ao buscar'));
    renderPage();

    expect(await screen.findByText(/Não foi possível carregar os veículos/)).toBeInTheDocument();

    vi.mocked(vehiclesApi.listar).mockResolvedValueOnce([]);
    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'Tentar novamente' }));

    expect(await screen.findByText('Nenhum veículo cadastrado.')).toBeInTheDocument();
  });

  it('shows an empty state message when there are no vehicles', async () => {
    vi.mocked(vehiclesApi.listar).mockReset().mockResolvedValue([]);
    renderPage();

    expect(await screen.findByText('Nenhum veículo cadastrado.')).toBeInTheDocument();
  });

  it('keeps the vehicle when the delete confirmation is cancelled', async () => {
    const user = userEvent.setup();
    renderPage();

    await screen.findByText('Corolla');
    const row = screen.getByText('Corolla').closest('tr')!;
    await user.click(within(row).getByRole('button', { name: 'Excluir' }));

    const dialog = await screen.findByRole('dialog');
    await user.click(within(dialog).getByRole('button', { name: 'Cancelar' }));

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(vehiclesApi.excluir).not.toHaveBeenCalled();
    expect(screen.getByText('Corolla')).toBeInTheDocument();
  });

  it('deletes a vehicle and shows a success message', async () => {
    vi.mocked(vehiclesApi.listar)
      .mockReset()
      .mockResolvedValueOnce([corolla, civic])
      .mockResolvedValue([civic]);
    const user = userEvent.setup();
    renderPage();

    await screen.findByText('Corolla');
    const row = screen.getByText('Corolla').closest('tr')!;
    await user.click(within(row).getByRole('button', { name: 'Excluir' }));

    const dialog = await screen.findByRole('dialog');
    await user.click(within(dialog).getByRole('button', { name: 'Excluir' }));

    expect(await screen.findByText(/excluído com sucesso/)).toBeInTheDocument();
    expect(vehiclesApi.excluir).toHaveBeenCalledWith(1);
    await waitFor(() => expect(screen.queryByText('Corolla')).not.toBeInTheDocument());
  });

  it('shows an error message when deleting fails', async () => {
    vi.mocked(vehiclesApi.excluir)
      .mockReset()
      .mockRejectedValueOnce(new Error('Erro 409 ao excluir'));
    const user = userEvent.setup();
    renderPage();

    await screen.findByText('Corolla');
    const row = screen.getByText('Corolla').closest('tr')!;
    await user.click(within(row).getByRole('button', { name: 'Excluir' }));

    const dialog = await screen.findByRole('dialog');
    await user.click(within(dialog).getByRole('button', { name: 'Excluir' }));

    expect(await screen.findByText('Erro 409 ao excluir')).toBeInTheDocument();
    expect(screen.getByText('Corolla')).toBeInTheDocument();
  });

  it('shows the dealer as read-only text, not an editable select', async () => {
    renderPage();

    await screen.findByText('Corolla');
    const row = screen.getByText('Corolla').closest('tr')!;

    expect(within(row).queryByRole('combobox')).not.toBeInTheDocument();
    expect(within(row).getByText('sem concessionária')).toBeInTheDocument();
  });
});
