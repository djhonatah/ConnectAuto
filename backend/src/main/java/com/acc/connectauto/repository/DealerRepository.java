package com.acc.connectauto.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acc.connectauto.entity.Dealer;

public interface DealerRepository extends JpaRepository<Dealer, Long> {

    Optional<Dealer> findByCnpj(String cnpj);

    List<Dealer> findByEndereco_Cidade(String cidade);

    List<Dealer> findByEndereco_Estado(String estado);
}
