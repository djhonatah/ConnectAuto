package com.acc.connectauto.exception;

/**
 * Lançada quando a consulta ao ViaCEP falha por motivo de infraestrutura (rede
 * indisponível, timeout, serviço fora do ar) — não porque o CEP em si seja inválido.
 * Distinta de {@link CepInvalidoException}: aqui a falha é do lado do ViaCEP, não do
 * cliente que fez a requisição, por isso mapeada para 503 em vez de 400.
 */
public class CepIndisponivelException extends RuntimeException {

    public CepIndisponivelException(String message) {
        super(message);
    }
}
