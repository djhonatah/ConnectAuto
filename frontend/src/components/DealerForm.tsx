import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useCepLookup } from '../hooks/useCepLookup';
import './DealerForm.css';

const UFS = [
  'AC',
  'AL',
  'AP',
  'AM',
  'BA',
  'CE',
  'DF',
  'ES',
  'GO',
  'MA',
  'MT',
  'MS',
  'MG',
  'PA',
  'PB',
  'PR',
  'PE',
  'PI',
  'RJ',
  'RN',
  'RS',
  'RO',
  'RR',
  'SC',
  'SP',
  'SE',
  'TO',
];

function isValidCnpj(value: string): boolean {
  const digits = value.replace(/\D/g, '');
  if (digits.length !== 14 || /^(\d)\1{13}$/.test(digits)) return false;

  const calcDigit = (base: string, weights: number[]) => {
    const sum = base.split('').reduce((acc, digit, i) => acc + Number(digit) * weights[i], 0);
    const rest = sum % 11;
    return rest < 2 ? 0 : 11 - rest;
  };

  const base = digits.slice(0, 12);
  const d1 = calcDigit(base, [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]);
  const d2 = calcDigit(base + d1, [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]);
  return digits === base + d1 + d2;
}

const dealerFormSchema = z.object({
  razaoSocial: z.string().trim().min(1, 'Razão social é obrigatória'),
  cnpj: z
    .string()
    .transform((value) => value.replace(/\D/g, ''))
    .refine((value) => value.length === 14, 'CNPJ deve ter 14 dígitos')
    .refine(isValidCnpj, 'CNPJ inválido'),
  endereco: z.object({
    cep: z
      .string()
      .transform((value) => value.replace(/\D/g, ''))
      .refine((value) => value.length === 8, 'CEP deve ter 8 dígitos'),
    logradouro: z.string().trim().min(1, 'Logradouro é obrigatório'),
    bairro: z.string().trim().min(1, 'Bairro é obrigatório'),
    cidade: z.string().trim().min(1, 'Cidade é obrigatória'),
    estado: z.string().refine((value) => UFS.includes(value), 'Selecione um estado'),
  }),
});

export type DealerFormValues = z.infer<typeof dealerFormSchema>;

interface DealerFormProps {
  defaultValues?: Partial<DealerFormValues>;
  onSubmit: (values: DealerFormValues) => void | Promise<void>;
  submitLabel?: string;
}

export function DealerForm({ defaultValues, onSubmit, submitLabel = 'Salvar' }: DealerFormProps) {
  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<DealerFormValues>({
    resolver: zodResolver(dealerFormSchema),
    defaultValues: {
      razaoSocial: '',
      cnpj: '',
      endereco: { cep: '', logradouro: '', bairro: '', cidade: '', estado: '' },
      ...defaultValues,
    },
  });

  const cepDigits = watch('endereco.cep').replace(/\D/g, '');
  const cepLookup = useCepLookup(cepDigits);

  useEffect(() => {
    if (!cepLookup.data || cepLookup.data.erro) return;
    setValue('endereco.logradouro', cepLookup.data.logradouro, { shouldValidate: true });
    setValue('endereco.bairro', cepLookup.data.bairro, { shouldValidate: true });
    setValue('endereco.cidade', cepLookup.data.localidade, { shouldValidate: true });
    setValue('endereco.estado', cepLookup.data.uf, { shouldValidate: true });
  }, [cepLookup.data, setValue]);

  return (
    <form className="dealer-form" onSubmit={handleSubmit(onSubmit)} noValidate>
      <div className="dealer-form__grid">
        <div className="dealer-form__field dealer-form__field--full">
          <label htmlFor="razaoSocial">Razão social *</label>
          <input
            id="razaoSocial"
            type="text"
            {...register('razaoSocial')}
            aria-invalid={!!errors.razaoSocial}
          />
          {errors.razaoSocial && (
            <span className="dealer-form__error">{errors.razaoSocial.message}</span>
          )}
        </div>

        <div className="dealer-form__field">
          <label htmlFor="cnpj">CNPJ *</label>
          <input
            id="cnpj"
            type="text"
            placeholder="00.000.000/0000-00"
            maxLength={18}
            {...register('cnpj')}
            aria-invalid={!!errors.cnpj}
          />
          {errors.cnpj && <span className="dealer-form__error">{errors.cnpj.message}</span>}
        </div>
      </div>

      <h2 className="dealer-form__section-title">Endereço</h2>

      <div className="dealer-form__grid">
        <div className="dealer-form__field">
          <label htmlFor="cep">CEP *</label>
          <input
            id="cep"
            type="text"
            placeholder="00000-000"
            maxLength={9}
            {...register('endereco.cep')}
            aria-invalid={!!errors.endereco?.cep}
          />
          {errors.endereco?.cep ? (
            <span className="dealer-form__error">{errors.endereco.cep.message}</span>
          ) : cepLookup.isFetching ? (
            <span className="dealer-form__hint">Buscando endereço…</span>
          ) : cepLookup.data?.erro ? (
            <span className="dealer-form__error">CEP não encontrado</span>
          ) : null}
        </div>

        <div className="dealer-form__field dealer-form__field--full">
          <label htmlFor="logradouro">Logradouro *</label>
          <input
            id="logradouro"
            type="text"
            {...register('endereco.logradouro')}
            aria-invalid={!!errors.endereco?.logradouro}
          />
          {errors.endereco?.logradouro && (
            <span className="dealer-form__error">{errors.endereco.logradouro.message}</span>
          )}
        </div>

        <div className="dealer-form__field">
          <label htmlFor="bairro">Bairro *</label>
          <input
            id="bairro"
            type="text"
            {...register('endereco.bairro')}
            aria-invalid={!!errors.endereco?.bairro}
          />
          {errors.endereco?.bairro && (
            <span className="dealer-form__error">{errors.endereco.bairro.message}</span>
          )}
        </div>

        <div className="dealer-form__field">
          <label htmlFor="cidade">Cidade *</label>
          <input
            id="cidade"
            type="text"
            {...register('endereco.cidade')}
            aria-invalid={!!errors.endereco?.cidade}
          />
          {errors.endereco?.cidade && (
            <span className="dealer-form__error">{errors.endereco.cidade.message}</span>
          )}
        </div>

        <div className="dealer-form__field">
          <label htmlFor="estado">Estado *</label>
          <select
            id="estado"
            {...register('endereco.estado')}
            aria-invalid={!!errors.endereco?.estado}
          >
            <option value="">Selecione…</option>
            {UFS.map((uf) => (
              <option key={uf} value={uf}>
                {uf}
              </option>
            ))}
          </select>
          {errors.endereco?.estado && (
            <span className="dealer-form__error">{errors.endereco.estado.message}</span>
          )}
        </div>
      </div>

      <div className="dealer-form__actions">
        <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
          {isSubmitting ? 'Salvando…' : submitLabel}
        </button>
      </div>
    </form>
  );
}
