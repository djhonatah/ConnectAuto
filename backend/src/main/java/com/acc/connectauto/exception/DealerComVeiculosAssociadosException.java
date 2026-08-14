package com.acc.connectauto.exception;

/**
 * Lançada ao tentar excluir uma concessionária que ainda tem veículos associados.
 * Checada proativamente (VehicleRepository.existsByDealer_Id) antes do delete, em vez de
 * depender da constraint de FK do banco: o Hibernate faz sua própria checagem de
 * consistência do grafo de entidades no flush, ANTES de qualquer SQL ir ao banco, e lança
 * TransientPropertyValueException nesse caso — um tipo diferente do erro de integridade
 * esperado, mais frágil e confuso de tratar de forma reativa.
 */
public class DealerComVeiculosAssociadosException extends RuntimeException {

    public DealerComVeiculosAssociadosException(String message) {
        super(message);
    }
}
