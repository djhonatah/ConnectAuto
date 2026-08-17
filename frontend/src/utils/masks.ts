export function onlyDigits(value: string): string {
  return value.replace(/\D/g, '');
}

export function maskCnpj(value: string): string {
  const digits = onlyDigits(value).slice(0, 14);
  const parts = [
    digits.slice(0, 2),
    digits.slice(2, 5),
    digits.slice(5, 8),
    digits.slice(8, 12),
    digits.slice(12, 14),
  ];
  let result = parts[0];
  if (parts[1]) result += `.${parts[1]}`;
  if (parts[2]) result += `.${parts[2]}`;
  if (parts[3]) result += `/${parts[3]}`;
  if (parts[4]) result += `-${parts[4]}`;
  return result;
}

export function maskCep(value: string): string {
  const digits = onlyDigits(value).slice(0, 8);
  const parts = [digits.slice(0, 5), digits.slice(5, 8)];
  return parts[1] ? `${parts[0]}-${parts[1]}` : parts[0];
}

export function maskCurrency(value: string): string {
  const digits = onlyDigits(value)
    .replace(/^0+(?=\d)/, '')
    .slice(0, 12);
  const cents = digits.padStart(3, '0');
  const integerPart = cents.slice(0, -2).replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  const decimalPart = cents.slice(-2);
  return `${integerPart},${decimalPart}`;
}

export function parseCurrency(value: string): number | undefined {
  const digits = onlyDigits(value);
  if (!digits) return undefined;
  return Number(digits) / 100;
}

export function formatCurrencyValue(value: number): string {
  return maskCurrency(Math.round(value * 100).toString());
}
