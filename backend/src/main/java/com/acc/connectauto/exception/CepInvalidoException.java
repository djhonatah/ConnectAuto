package com.acc.connectauto.exception;

/**
 * Lançada quando o CEP informado não existe (retorno {@code erro: true} do ViaCEP)
 * ou quando a consulta ao ViaCEP falha (rede indisponível, CEP mal formatado, etc).
 */
public class CepInvalidoException extends RuntimeException {

    public CepInvalidoException(String message) {
        super(message);
    }
}
