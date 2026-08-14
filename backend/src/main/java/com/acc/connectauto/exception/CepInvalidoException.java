package com.acc.connectauto.exception;

/**
 * Lançada quando o CEP informado não existe (retorno {@code erro: true} do ViaCEP).
 * Falha de infraestrutura ao consultar o ViaCEP é {@link CepIndisponivelException},
 * não esta — os dois têm causas e status HTTP diferentes.
 */
public class CepInvalidoException extends RuntimeException {

    public CepInvalidoException(String message) {
        super(message);
    }
}
