package com.acc.connectauto.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.acc.connectauto.dto.EnderecoDTO;
import com.acc.connectauto.dto.request.DealerRequestDTO;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

/**
 * Testa a combinação @Pattern + @CNPJ (org.hibernate.validator.constraints.br.CNPJ) usada
 * em DealerRequestDTO.cnpj, sem contexto Spring. O dígito verificador é calculado pela
 * própria Hibernate Validator; @Pattern garante que só o formato de 14 dígitos sem
 * máscara é aceito (o mesmo contrato do validador caseiro que este substitui).
 */
class CnpjFormatValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private EnderecoDTO enderecoValidoDTO() {
        return new EnderecoDTO("Av. Principal, 100", "Centro", "São Paulo", "SP", "01310100");
    }

    private Set<ConstraintViolation<DealerRequestDTO>> validarCnpj(String cnpj) {
        DealerRequestDTO dealerRequestDTO = new DealerRequestDTO("Auto Center Toyota Ltda", cnpj, enderecoValidoDTO());
        return validator.validate(dealerRequestDTO);
    }

    @Test
    void deveAceitarCnpjComDigitosVerificadoresCorretos() {
        assertThat(validarCnpj("12345678000195")).isEmpty();
        assertThat(validarCnpj("11222333000181")).isEmpty();
    }

    @Test
    void deveRejeitarCnpjComDigitoVerificadorIncorreto() {
        assertThat(validarCnpj("12345678000199")).isNotEmpty();
    }

    @Test
    void deveRejeitarCnpjComTodosOsDigitosIguais() {
        assertThat(validarCnpj("11111111111111")).isNotEmpty();
    }

    @Test
    void deveRejeitarCnpjComTamanhoDiferenteDe14() {
        assertThat(validarCnpj("123456780001")).isNotEmpty();
    }

    @Test
    void deveRejeitarCnpjComMascara() {
        // @CNPJ sozinho aceitaria; o @Pattern (\d{14}) é quem barra a máscara aqui,
        // mantendo o mesmo formato de armazenamento (só dígitos) já usado no banco.
        assertThat(validarCnpj("12.345.678/0001-95")).isNotEmpty();
    }

    @Test
    void deveRejeitarCnpjEmBranco() {
        assertThat(validarCnpj("")).isNotEmpty();
    }
}
