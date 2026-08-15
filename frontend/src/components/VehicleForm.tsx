import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { FUEL_LABELS, type FuelType } from '../services/api/vehicles';
import './VehicleForm.css';

const CURRENT_YEAR = new Date().getFullYear();
const fuelTypes = Object.keys(FUEL_LABELS) as FuelType[];

// Espelha as validações de VehicleRequestDTO no backend: marca, modelo,
// tipoCombustivel e cor são obrigatórios; ano, chassi, valor e corInterna
// são opcionais, mas continuam validados quando preenchidos.
const vehicleFormSchema = z.object({
  marca: z.string().trim().min(1, 'Marca é obrigatória'),
  modelo: z.string().trim().min(1, 'Modelo é obrigatório'),
  tipoCombustivel: z
    .string()
    .refine((value): value is FuelType => fuelTypes.includes(value as FuelType), {
      message: 'Selecione o combustível',
    }),
  cor: z.string().trim().min(1, 'Cor é obrigatória'),
  ano: z.preprocess(
    (value) => (value === '' || value === undefined ? undefined : Number(value)),
    z
      .number()
      .int('Ano deve ser um número inteiro')
      .min(1900, 'Ano inválido')
      .max(CURRENT_YEAR + 1, 'Ano inválido')
      .optional(),
  ),
  chassi: z.string().trim().max(17, 'Chassi deve ter no máximo 17 caracteres').optional(),
  valor: z.preprocess(
    (value) => (value === '' || value === undefined ? undefined : Number(value)),
    z.number().nonnegative('Valor não pode ser negativo').optional(),
  ),
  corInterna: z.string().trim().optional(),
});

// O schema transforma a entrada crua do formulário (strings dos <input>,
// "" para campos vazios) na saída validada (número, FuelType, etc). Por isso
// usamos dois tipos: Input é o que os campos guardam enquanto o usuário
// digita, VehicleFormValues é o que o onSubmit recebe já validado/convertido.
type VehicleFormInput = z.input<typeof vehicleFormSchema>;
export type VehicleFormValues = z.output<typeof vehicleFormSchema>;

interface VehicleFormProps {
  defaultValues?: Partial<VehicleFormInput>;
  onSubmit: (values: VehicleFormValues) => void | Promise<void>;
  submitLabel?: string;
}

export function VehicleForm({ defaultValues, onSubmit, submitLabel = 'Salvar' }: VehicleFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<VehicleFormInput, unknown, VehicleFormValues>({
    resolver: zodResolver(vehicleFormSchema),
    defaultValues: { tipoCombustivel: '', ...defaultValues },
  });

  return (
    <form className="vehicle-form" onSubmit={handleSubmit(onSubmit)} noValidate>
      <div className="vehicle-form__grid">
        <div className="vehicle-form__field">
          <label htmlFor="marca">Marca *</label>
          <input id="marca" type="text" {...register('marca')} aria-invalid={!!errors.marca} />
          {errors.marca && <span className="vehicle-form__error">{errors.marca.message}</span>}
        </div>

        <div className="vehicle-form__field">
          <label htmlFor="modelo">Modelo *</label>
          <input id="modelo" type="text" {...register('modelo')} aria-invalid={!!errors.modelo} />
          {errors.modelo && <span className="vehicle-form__error">{errors.modelo.message}</span>}
        </div>

        <div className="vehicle-form__field">
          <label htmlFor="tipoCombustivel">Combustível *</label>
          <select
            id="tipoCombustivel"
            {...register('tipoCombustivel')}
            aria-invalid={!!errors.tipoCombustivel}
          >
            <option value="">Selecione…</option>
            {fuelTypes.map((fuel) => (
              <option key={fuel} value={fuel}>
                {FUEL_LABELS[fuel]}
              </option>
            ))}
          </select>
          {errors.tipoCombustivel && (
            <span className="vehicle-form__error">{errors.tipoCombustivel.message}</span>
          )}
        </div>

        <div className="vehicle-form__field">
          <label htmlFor="cor">Cor *</label>
          <input id="cor" type="text" {...register('cor')} aria-invalid={!!errors.cor} />
          {errors.cor && <span className="vehicle-form__error">{errors.cor.message}</span>}
        </div>

        <div className="vehicle-form__field">
          <label htmlFor="ano">Ano</label>
          <input id="ano" type="number" {...register('ano')} aria-invalid={!!errors.ano} />
          {errors.ano && <span className="vehicle-form__error">{errors.ano.message}</span>}
        </div>

        <div className="vehicle-form__field">
          <label htmlFor="valor">Valor (R$)</label>
          <input
            id="valor"
            type="number"
            step="0.01"
            {...register('valor')}
            aria-invalid={!!errors.valor}
          />
          {errors.valor && <span className="vehicle-form__error">{errors.valor.message}</span>}
        </div>

        <div className="vehicle-form__field">
          <label htmlFor="chassi">Chassi</label>
          <input
            id="chassi"
            type="text"
            maxLength={17}
            {...register('chassi')}
            aria-invalid={!!errors.chassi}
          />
          {errors.chassi && <span className="vehicle-form__error">{errors.chassi.message}</span>}
        </div>

        <div className="vehicle-form__field">
          <label htmlFor="corInterna">Cor interna</label>
          <input id="corInterna" type="text" {...register('corInterna')} />
        </div>
      </div>

      <div className="vehicle-form__actions">
        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Salvando…' : submitLabel}
        </button>
      </div>
    </form>
  );
}
