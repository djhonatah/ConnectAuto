import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { VehicleForm } from './VehicleForm';

async function fillRequiredFields(user: ReturnType<typeof userEvent.setup>) {
  await user.type(screen.getByLabelText('Marca *'), 'Toyota');
  await user.type(screen.getByLabelText('Modelo *'), 'Corolla');
  await user.selectOptions(screen.getByLabelText('Combustível *'), 'FLEX');
  await user.type(screen.getByLabelText('Cor *'), 'Prata');
}

describe('VehicleForm', () => {
  it('shows validation errors and blocks submit when required fields are empty', async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();
    render(<VehicleForm onSubmit={onSubmit} />);

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
    render(<VehicleForm onSubmit={onSubmit} />);

    await fillRequiredFields(user);
    await user.type(screen.getByLabelText('Ano'), '1800');
    await user.click(screen.getByRole('button', { name: 'Salvar' }));

    expect(await screen.findByText('Ano inválido')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('validates valor is not negative', async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();
    render(<VehicleForm onSubmit={onSubmit} />);

    await fillRequiredFields(user);
    await user.type(screen.getByLabelText('Valor (R$)'), '-5');
    await user.click(screen.getByRole('button', { name: 'Salvar' }));

    expect(await screen.findByText('Valor não pode ser negativo')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('submits parsed values when the form is valid', async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();
    render(<VehicleForm onSubmit={onSubmit} />);

    await fillRequiredFields(user);
    await user.type(screen.getByLabelText('Ano'), '2022');
    await user.type(screen.getByLabelText('Valor (R$)'), '120000');
    await user.click(screen.getByRole('button', { name: 'Salvar' }));

    await waitFor(() => expect(onSubmit).toHaveBeenCalledTimes(1));
    expect(onSubmit.mock.calls[0][0]).toMatchObject({
      marca: 'Toyota',
      modelo: 'Corolla',
      tipoCombustivel: 'FLEX',
      cor: 'Prata',
      ano: 2022,
      valor: 120000,
    });
  });

  it('pre-fills fields from defaultValues', () => {
    render(
      <VehicleForm
        defaultValues={{ marca: 'Honda', modelo: 'Civic', tipoCombustivel: 'FLEX', cor: 'Preto' }}
        onSubmit={vi.fn()}
        submitLabel="Salvar alterações"
      />,
    );

    expect(screen.getByLabelText('Marca *')).toHaveValue('Honda');
    expect(screen.getByLabelText('Modelo *')).toHaveValue('Civic');
    expect(screen.getByLabelText('Combustível *')).toHaveValue('FLEX');
    expect(screen.getByLabelText('Cor *')).toHaveValue('Preto');
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
    render(<VehicleForm onSubmit={onSubmit} />);

    await fillRequiredFields(user);
    await user.click(screen.getByRole('button', { name: 'Salvar' }));

    expect(await screen.findByRole('button', { name: 'Salvando…' })).toBeDisabled();

    resolveSubmit();
    await waitFor(() => expect(screen.getByRole('button', { name: 'Salvar' })).not.toBeDisabled());
  });
});
