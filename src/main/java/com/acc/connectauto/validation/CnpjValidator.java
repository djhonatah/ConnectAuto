package com.acc.connectauto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CnpjValidator implements ConstraintValidator<CNPJ, String> {

    private static final int[] PESOS_PRIMEIRO_DIGITO = { 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };
    private static final int[] PESOS_SEGUNDO_DIGITO = { 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };

    @Override
    public boolean isValid(String cnpj, ConstraintValidatorContext constraintValidatorContext) {
        if (cnpj == null || cnpj.isBlank()) {
            return true;
        }

        if (!cnpj.matches("\\d{14}") || todosDigitosIguais(cnpj)) {
            return false;
        }

        int[] digitos = cnpj.chars().map(digito -> digito - '0').toArray();

        int primeiroDigitoVerificador = calcularDigitoVerificador(digitos, PESOS_PRIMEIRO_DIGITO);
        if (primeiroDigitoVerificador != digitos[12]) {
            return false;
        }

        int segundoDigitoVerificador = calcularDigitoVerificador(digitos, PESOS_SEGUNDO_DIGITO);
        return segundoDigitoVerificador == digitos[13];
    }

    private int calcularDigitoVerificador(int[] digitos, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += digitos[i] * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private boolean todosDigitosIguais(String cnpj) {
        return cnpj.chars().distinct().count() == 1;
    }
}
