import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { DealerForm, type DealerFormValues } from './DealerForm';
import { viaCepApi } from '../services/api/viaCep';
import type { ViaCepResponse } from '../services/api/viaCep';

vi.mock('../services/api/viaCep', () => ({
  viaCepApi: { buscar: vi.fn() },
}));

const buscarMock = vi.mocked(viaCepApi.buscar);

const cepResponse: ViaCepResponse = {
  cep: '01310-100',
  logradouro: 'Avenida Paulista',
  bairro: 'Bela Vista',
  localidade: 'São Paulo',
  uf: 'SP',
};

function renderForm(
  overrides: {
    defaultValues?: Partial<DealerFormValues>;
    onSubmit?: (values: DealerFormValues) => void | Promise<void>;
    submitLabel?: string;
  } = {},
) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  const onSubmit = vi.fn(overrides.onSubmit);
  render(
    <QueryClientProvider client={queryClient}>
      <DealerForm
        defaultValues={overrides.defaultValues}
        onSubmit={onSubmit}
        submitLabel={overrides.submitLabel}
      />
    </QueryClientProvider>,
  );
  return { onSubmit };
}

describe('DealerForm', () => {
  beforeEach(() => {
    buscarMock.mockReset();
    buscarMock.mockResolvedValue(cepResponse);
  });

  it('shows validation errors and blocks submit when required fields are empty', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm();

    await user.click(screen.getByRole('button', { name: 'Salvar' }));

    expect(await screen.findByText('Razão social é obrigatória')).toBeInTheDocument();
    expect(screen.getByText('CEP deve ter 8 dígitos')).toBeInTheDocument();
    expect(screen.getByText('Logradouro é obrigatório')).toBeInTheDocument();
    expect(screen.getByText('Bairro é obrigatório')).toBeInTheDocument();
    expect(screen.getByText('Cidade é obrigatória')).toBeInTheDocument();
    expect(screen.getByText('Selecione um estado')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('validates the CNPJ check digits', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm();

    await user.type(screen.getByLabelText('CNPJ *'), '11222333000180');
    await user.click(screen.getByRole('button', { name: 'Salvar' }));

    expect(await screen.findByText('CNPJ inválido')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('auto-fills endereco fields from a successful CEP lookup', async () => {
    const user = userEvent.setup();
    renderForm();

    await user.type(screen.getByLabelText('CEP *'), '01310100');

    expect(await screen.findByLabelText('Logradouro *')).toHaveValue('Avenida Paulista');
    expect(screen.getByLabelText('Bairro *')).toHaveValue('Bela Vista');
    expect(screen.getByLabelText('Cidade *')).toHaveValue('São Paulo');
    expect(screen.getByLabelText('Estado *')).toHaveValue('SP');
    expect(buscarMock).toHaveBeenCalledWith('01310100');
  });

  it('shows a loading hint while the CEP lookup is pending', async () => {
    let resolveLookup!: (value: ViaCepResponse) => void;
    buscarMock.mockReturnValueOnce(
      new Promise((resolve) => {
        resolveLookup = resolve;
      }),
    );
    const user = userEvent.setup();
    renderForm();

    await user.type(screen.getByLabelText('CEP *'), '01310100');

    expect(await screen.findByText('Buscando endereço…')).toBeInTheDocument();

    resolveLookup(cepResponse);
    await waitFor(() => expect(screen.queryByText('Buscando endereço…')).not.toBeInTheDocument());
  });

  it('shows an error hint when the CEP lookup returns erro', async () => {
    buscarMock.mockResolvedValueOnce({
      cep: '',
      logradouro: '',
      bairro: '',
      localidade: '',
      uf: '',
      erro: true,
    });
    const user = userEvent.setup();
    renderForm();

    await user.type(screen.getByLabelText('CEP *'), '00000000');

    expect(await screen.findByText('CEP não encontrado')).toBeInTheDocument();
  });

  it('submits parsed values when the form is valid', async () => {
    const user = userEvent.setup();
    const { onSubmit } = renderForm();

    await user.type(screen.getByLabelText('Razão social *'), 'Auto Center Silva Ltda');
    await user.type(screen.getByLabelText('CNPJ *'), '11222333000181');
    await user.type(screen.getByLabelText('CEP *'), '01310100');
    await screen.findByDisplayValue('Avenida Paulista');

    await user.click(screen.getByRole('button', { name: 'Salvar' }));

    await waitFor(() => expect(onSubmit).toHaveBeenCalledTimes(1));
    expect(onSubmit.mock.calls[0][0]).toMatchObject({
      razaoSocial: 'Auto Center Silva Ltda',
      cnpj: '11222333000181',
      endereco: {
        cep: '01310100',
        logradouro: 'Avenida Paulista',
        bairro: 'Bela Vista',
        cidade: 'São Paulo',
        estado: 'SP',
      },
    });
  });

  it('pre-fills fields from defaultValues', async () => {
    renderForm({
      defaultValues: {
        razaoSocial: 'Auto Center Silva Ltda',
        cnpj: '11222333000181',
        endereco: {
          cep: '01310100',
          logradouro: 'Avenida Paulista',
          bairro: 'Bela Vista',
          cidade: 'São Paulo',
          estado: 'SP',
        },
      },
      submitLabel: 'Salvar alterações',
    });

    expect(screen.getByLabelText('Razão social *')).toHaveValue('Auto Center Silva Ltda');
    expect(screen.getByLabelText('CNPJ *')).toHaveValue('11222333000181');
    expect(screen.getByLabelText('Estado *')).toHaveValue('SP');
    expect(screen.getByRole('button', { name: 'Salvar alterações' })).toBeInTheDocument();

    await waitFor(() => expect(buscarMock).toHaveBeenCalledWith('01310100'));
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

    await user.type(screen.getByLabelText('Razão social *'), 'Auto Center Silva Ltda');
    await user.type(screen.getByLabelText('CNPJ *'), '11222333000181');
    await user.type(screen.getByLabelText('CEP *'), '01310100');
    await screen.findByDisplayValue('Avenida Paulista');

    await user.click(screen.getByRole('button', { name: 'Salvar' }));

    expect(await screen.findByRole('button', { name: 'Salvando…' })).toBeDisabled();

    resolveSubmit();
    await waitFor(() => expect(screen.getByRole('button', { name: 'Salvar' })).not.toBeDisabled());
  });
});
