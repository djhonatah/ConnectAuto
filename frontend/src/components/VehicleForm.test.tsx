import type { ComponentProps } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { VehicleForm } from './VehicleForm';
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

function renderForm(props: ComponentProps<typeof VehicleForm>) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <QueryClientProvider client={queryClient}>
      <VehicleForm {...props} />
    </QueryClientProvider>,
  );
}

async function fillRequiredFields(user: ReturnType<typeof userEvent.setup>) {
  await user.type(screen.getByLabelText('Marca *'), 'Toyota');
  await user.type(screen.getByLabelText('Modelo *'), 'Corolla');
  await user.selectOptions(screen.getByLabelText('Combustível *'), 'FLEX');
  await user.type(screen.getByLabelText('Cor *'), 'Prata');
}

describe('VehicleForm', () => {
  beforeEach(() => {
    vi.mocked(dealersApi.listar).mockReset().mockResolvedValue([autoCenterSilva]);
  });

  it('shows validation errors and blocks submit when required fields are empty', async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();
    renderForm({ onSubmit });

    await user.click(screen.getByRole('button', { name: 'Salvar' }));

    expect(await screen.findByText('Marca é obrigatória')).toBeInTheDocument();
    expect(screen.getByText('Modelo é obrigatório')).toBeInTheDocument();
    expect(screen.getByText('Selecione o combustível')).toBeInTheDocument();
    expect(screen.getByText('Cor é obrigatória')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('validates ano range', async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();
    renderForm({ onSubmit });

    await fillRequiredFields(user);
    await user.type(screen.getByLabelText('Ano'), '1800');
    await user.click(screen.getByRole('button', { name: 'Salvar' }));

    expect(await screen.findByText('Ano inválido')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('masks the valor field as currency while typing, ignoring non-digit input', async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();
    renderForm({ onSubmit });

    await user.type(screen.getByLabelText('Valor (R$)'), '-abc12000000');

    expect(screen.getByLabelText('Valor (R$)')).toHaveValue('120.000,00');
  });

  it('submits parsed values when the form is valid', async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();
    renderForm({ onSubmit });

    await fillRequiredFields(user);
    await user.type(screen.getByLabelText('Ano'), '2022');
    await user.type(screen.getByLabelText('Valor (R$)'), '12000000');
    await user.selectOptions(screen.getByLabelText('Concessionária'), '1');
    await user.click(screen.getByRole('button', { name: 'Salvar' }));

    await waitFor(() => expect(onSubmit).toHaveBeenCalledTimes(1));
    expect(onSubmit.mock.calls[0][0]).toMatchObject({
      marca: 'Toyota',
      modelo: 'Corolla',
      tipoCombustivel: 'FLEX',
      cor: 'Prata',
      ano: 2022,
      valor: 120000,
      dealerId: 1,
    });
  });

  it('pre-fills fields from defaultValues, formatting valor and matching the dealer', async () => {
    renderForm({
      defaultValues: {
        marca: 'Honda',
        modelo: 'Civic',
        tipoCombustivel: 'FLEX',
        cor: 'Preto',
        valor: 172300,
        dealerId: 1,
      },
      onSubmit: vi.fn(),
      submitLabel: 'Salvar alterações',
    });

    expect(screen.getByLabelText('Marca *')).toHaveValue('Honda');
    expect(screen.getByLabelText('Modelo *')).toHaveValue('Civic');
    expect(screen.getByLabelText('Combustível *')).toHaveValue('FLEX');
    expect(screen.getByLabelText('Cor *')).toHaveValue('Preto');
    expect(screen.getByLabelText('Valor (R$)')).toHaveValue('172.300,00');
    await waitFor(() => expect(screen.getByLabelText('Concessionária')).toHaveValue('1'));
    expect(screen.getByRole('button', { name: 'Salvar alterações' })).toBeInTheDocument();
  });

  it('disables the submit button while submitting', async () => {
    const user = userEvent.setup();
    let resolveSubmit: () => void = () => {};
    const onSubmit = vi.fn(
      () =>
        new Promise<void>((resolve) => {
          resolveSubmit = resolve;
        }),
    );
    renderForm({ onSubmit });

    await fillRequiredFields(user);
    await user.click(screen.getByRole('button', { name: 'Salvar' }));

    expect(await screen.findByRole('button', { name: 'Salvando…' })).toBeDisabled();

    resolveSubmit();
    await waitFor(() => expect(screen.getByRole('button', { name: 'Salvar' })).not.toBeDisabled());
  });
});
