import { useEffect, useRef } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useDealers } from '../hooks/useDealers';
import { FUEL_LABELS, type FuelType } from '../services/api/vehicles';
import { formatCurrencyValue, maskCurrency, parseCurrency } from '../utils/masks';
import './VehicleForm.css';

const CURRENT_YEAR = new Date().getFullYear();
const fuelTypes = Object.keys(FUEL_LABELS) as FuelType[];

// Espelha as validações de VehicleRequestDTO no backend.
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
  chassi: z.preprocess(
    (value) => (typeof value === 'string' && value.trim() === '' ? undefined : value),
    z.string().trim().max(17, 'Chassi deve ter no máximo 17 caracteres').optional(),
  ),
  // O campo é uma máscara de moeda (string "1.234,56"), não um <input
  // type="number">, daí o parseCurrency em vez de Number() puro.
  valor: z.preprocess(
    (value) =>
      typeof value === 'string' && value.trim() !== '' ? parseCurrency(value) : undefined,
    z.number().nonnegative('Valor não pode ser negativo').optional(),
  ),
  corInterna: z.string().trim().optional(),
  dealerId: z.preprocess(
    (value) => (value === '' || value === undefined ? undefined : Number(value)),
    z.number().optional(),
  ),
});

type VehicleFormInput = z.input<typeof vehicleFormSchema>;
export type VehicleFormValues = z.output<typeof vehicleFormSchema>;

interface VehicleFormProps {
  defaultValues?: Partial<VehicleFormInput>;
  onSubmit: (values: VehicleFormValues) => void | Promise<void>;
  submitLabel?: string;
}

export function VehicleForm({ defaultValues, onSubmit, submitLabel = 'Salvar' }: VehicleFormProps) {
  const { data: dealers } = useDealers();

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<VehicleFormInput, unknown, VehicleFormValues>({
    resolver: zodResolver(vehicleFormSchema),
    defaultValues: {
      tipoCombustivel: '',
      ...defaultValues,
      // Máscara aplicada aqui (não em quem chama o form) pra edição já abrir
      // com o valor formatado, venha ele como número (da API) ou string.
      valor:
        typeof defaultValues?.valor === 'number'
          ? formatCurrencyValue(defaultValues.valor)
          : defaultValues?.valor,
      dealerId: defaultValues?.dealerId ?? '',
    },
  });

  // As opções da concessionária chegam de forma assíncrona (useDealers) — se
  // a lista ainda não tinha carregado quando o <select> montou, o valor
  // inicial não "gruda" em nenhuma <option> (nenhuma existia ainda). Assim
  // que a lista chega pela primeira vez, força a seleção pro dealerId que
  // veio em defaultValues. O ref evita que um refetch em segundo plano
  // sobrescreva a escolha do usuário.
  const dealerSyncedRef = useRef(false);
  useEffect(() => {
    if (dealers && defaultValues?.dealerId !== undefined && !dealerSyncedRef.current) {
      dealerSyncedRef.current = true;
      setValue('dealerId', defaultValues.dealerId);
    }
  }, [dealers, defaultValues?.dealerId, setValue]);

  const valorField = register('valor');

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
          <div className="vehicle-form__currency">
            <span>R$</span>
            <input
              id="valor"
              type="text"
              inputMode="decimal"
              placeholder="0,00"
              name={valorField.name}
              ref={valorField.ref}
              onBlur={valorField.onBlur}
              onChange={(e) => {
                e.target.value = maskCurrency(e.target.value);
                valorField.onChange(e);
              }}
              aria-invalid={!!errors.valor}
            />
          </div>
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

        <div className="vehicle-form__field">
          <label htmlFor="dealerId">Concessionária</label>
          <select id="dealerId" {...register('dealerId')} aria-invalid={!!errors.dealerId}>
            <option value="">Sem concessionária</option>
            {dealers?.map((dealer) => (
              <option key={dealer.id} value={dealer.id}>
                {dealer.razaoSocial}
              </option>
            ))}
          </select>
          {errors.dealerId && (
            <span className="vehicle-form__error">{errors.dealerId.message}</span>
          )}
        </div>
      </div>

      <div className="vehicle-form__actions">
        <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
          {isSubmitting ? 'Salvando…' : submitLabel}
        </button>
      </div>
    </form>
  );
}
