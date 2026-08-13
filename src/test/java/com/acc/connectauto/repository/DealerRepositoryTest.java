package com.acc.connectauto.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.acc.connectauto.entity.Dealer;
import com.acc.connectauto.entity.Endereco;

@DataJpaTest
class DealerRepositoryTest {

    @Autowired
    private DealerRepository dealerRepository;

    private Dealer novoDealer(String razaoSocial, String cnpj, String cidade, String estado) {
        return Dealer.builder()
                .razaoSocial(razaoSocial)
                .cnpj(cnpj)
                .endereco(Endereco.builder()
                        .logradouro("Av. Principal, 100")
                        .bairro("Centro")
                        .cidade(cidade)
                        .estado(estado)
                        .cep("01310100")
                        .build())
                .build();
    }

    @Test
    void deveSalvarBuscarAtualizarEDeletarUmDealer() {
        Dealer dealer = novoDealer("Auto Center Toyota Ltda", "12345678000199", "São Paulo", "SP");

        Dealer savedDealer = dealerRepository.save(dealer);
        assertThat(savedDealer.getId()).isNotNull();

        Optional<Dealer> foundDealer = dealerRepository.findById(savedDealer.getId());
        assertThat(foundDealer).isPresent();
        assertThat(foundDealer.get().getRazaoSocial()).isEqualTo("Auto Center Toyota Ltda");

        savedDealer.getEndereco().setCidade("Campinas");
        dealerRepository.save(savedDealer);
        assertThat(dealerRepository.findById(savedDealer.getId()).get().getEndereco().getCidade())
                .isEqualTo("Campinas");

        dealerRepository.deleteById(savedDealer.getId());
        assertThat(dealerRepository.findById(savedDealer.getId())).isEmpty();
    }

    @Test
    void deveBuscarPorCnpj() {
        dealerRepository.save(novoDealer("Honda Sul Ltda", "11222333000144", "Curitiba", "PR"));

        Optional<Dealer> foundDealer = dealerRepository.findByCnpj("11222333000144");

        assertThat(foundDealer).isPresent();
        assertThat(foundDealer.get().getRazaoSocial()).isEqualTo("Honda Sul Ltda");
    }

    @Test
    void deveBuscarPorCidadeDoEndereco() {
        dealerRepository.save(novoDealer("Fiat Centro Ltda", "22333444000155", "Belo Horizonte", "MG"));
        dealerRepository.save(novoDealer("Jeep Norte Ltda", "33444555000166", "Belo Horizonte", "MG"));
        dealerRepository.save(novoDealer("Renault Sul Ltda", "44555666000177", "Porto Alegre", "RS"));

        List<Dealer> belorizontinoDealers = dealerRepository.findByEndereco_Cidade("Belo Horizonte");

        assertThat(belorizontinoDealers).hasSize(2)
                .extracting(Dealer::getRazaoSocial)
                .containsExactlyInAnyOrder("Fiat Centro Ltda", "Jeep Norte Ltda");
    }

    @Test
    void deveBuscarPorEstadoDoEndereco() {
        dealerRepository.save(novoDealer("VW Litoral Ltda", "55666777000188", "Santos", "SP"));
        dealerRepository.save(novoDealer("Chevrolet Serra Ltda", "66777888000199", "Gramado", "RS"));

        List<Dealer> paulistaDealers = dealerRepository.findByEndereco_Estado("SP");

        assertThat(paulistaDealers).hasSize(1);
        assertThat(paulistaDealers.get(0).getRazaoSocial()).isEqualTo("VW Litoral Ltda");
    }
}
