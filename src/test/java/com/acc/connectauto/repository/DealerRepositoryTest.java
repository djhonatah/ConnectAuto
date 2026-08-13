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

        Dealer salvo = dealerRepository.save(dealer);
        assertThat(salvo.getId()).isNotNull();

        Optional<Dealer> encontrado = dealerRepository.findById(salvo.getId());
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getRazaoSocial()).isEqualTo("Auto Center Toyota Ltda");

        salvo.getEndereco().setCidade("Campinas");
        dealerRepository.save(salvo);
        assertThat(dealerRepository.findById(salvo.getId()).get().getEndereco().getCidade())
                .isEqualTo("Campinas");

        dealerRepository.deleteById(salvo.getId());
        assertThat(dealerRepository.findById(salvo.getId())).isEmpty();
    }

    @Test
    void deveBuscarPorCnpj() {
        dealerRepository.save(novoDealer("Honda Sul Ltda", "11222333000144", "Curitiba", "PR"));

        Optional<Dealer> encontrado = dealerRepository.findByCnpj("11222333000144");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getRazaoSocial()).isEqualTo("Honda Sul Ltda");
    }

    @Test
    void deveBuscarPorCidadeDoEndereco() {
        dealerRepository.save(novoDealer("Fiat Centro Ltda", "22333444000155", "Belo Horizonte", "MG"));
        dealerRepository.save(novoDealer("Jeep Norte Ltda", "33444555000166", "Belo Horizonte", "MG"));
        dealerRepository.save(novoDealer("Renault Sul Ltda", "44555666000177", "Porto Alegre", "RS"));

        List<Dealer> emBH = dealerRepository.findByEndereco_Cidade("Belo Horizonte");

        assertThat(emBH).hasSize(2)
                .extracting(Dealer::getRazaoSocial)
                .containsExactlyInAnyOrder("Fiat Centro Ltda", "Jeep Norte Ltda");
    }

    @Test
    void deveBuscarPorEstadoDoEndereco() {
        dealerRepository.save(novoDealer("VW Litoral Ltda", "55666777000188", "Santos", "SP"));
        dealerRepository.save(novoDealer("Chevrolet Serra Ltda", "66777888000199", "Gramado", "RS"));

        List<Dealer> emSP = dealerRepository.findByEndereco_Estado("SP");

        assertThat(emSP).hasSize(1);
        assertThat(emSP.get(0).getRazaoSocial()).isEqualTo("VW Litoral Ltda");
    }
}
