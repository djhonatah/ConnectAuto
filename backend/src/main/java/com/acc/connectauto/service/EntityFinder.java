package com.acc.connectauto.service;

import java.util.function.Supplier;

import org.springframework.data.repository.CrudRepository;

import com.acc.connectauto.exception.ResourceNotFoundException;

/**
 * Centraliza o padrão "buscar por id ou lançar ResourceNotFoundException", repetido
 * antes em DealerService e VehicleService com a mesma forma e mensagens equivalentes.
 */
final class EntityFinder {

    private EntityFinder() {
    }

    static <T, ID> T buscarOuLancar(CrudRepository<T, ID> repository, ID id, Supplier<String> mensagemNaoEncontrado) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(mensagemNaoEncontrado.get()));
    }
}
