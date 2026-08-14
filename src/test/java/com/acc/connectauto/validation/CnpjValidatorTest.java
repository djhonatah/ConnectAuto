package com.acc.connectauto.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Testes unitários do algoritmo de dígito verificador, sem contexto Spring.
 */
class CnpjValidatorTest {

    private final CnpjValidator cnpjValidator = new CnpjValidator();

    @Test
    void deveAceitarCnpjComDigitosVerificadoresCorretos() {
        assertThat(cnpjValidator.isValid("12345678000195", null)).isTrue();
        assertThat(cnpjValidator.isValid("11222333000181", null)).isTrue();
    }

    @Test
    void deveRejeitarCnpjComDigitoVerificadorIncorreto() {
        assertThat(cnpjValidator.isValid("12345678000199", null)).isFalse();
    }

    @Test
    void deveRejeitarCnpjComTodosOsDigitosIguais() {
        assertThat(cnpjValidator.isValid("11111111111111", null)).isFalse();
    }

    @Test
    void deveRejeitarCnpjComTamanhoDiferenteDe14() {
        assertThat(cnpjValidator.isValid("123456780001", null)).isFalse();
    }

    @Test
    void deveRejeitarCnpjComCaracteresNaoNumericos() {
        assertThat(cnpjValidator.isValid("12.345.678/0001-95", null)).isFalse();
    }

    @Test
    void deveConsiderarNuloOuVazioComoValido() {
        // Obrigatoriedade é responsabilidade de @NotBlank, não do @CNPJ.
        assertThat(cnpjValidator.isValid(null, null)).isTrue();
        assertThat(cnpjValidator.isValid("", null)).isTrue();
    }
}
