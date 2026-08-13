package com.acc.connectauto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.acc.connectauto.dto.EnderecoDTO;
import com.acc.connectauto.dto.request.DealerRequestDTO;
import com.acc.connectauto.dto.response.DealerResponseDTO;
import com.acc.connectauto.exception.ResourceNotFoundException;

/**
 * Testes de integração de {@link DealerService}, com contexto Spring e H2 reais.
 */
@SpringBootTest
@Transactional
class DealerServiceTest {

    @Autowired
    private DealerService dealerService;

    private DealerRequestDTO dealerRequestDTOValido() {
        return new DealerRequestDTO(
                "Auto Center Toyota Ltda",
                "12345678000199",
                new EnderecoDTO("Av. Principal, 100", "Centro", "São Paulo", "SP", "01310100"));
    }

    @Test
    void deveCriarDealer() {
        DealerResponseDTO dealerResponseDTO = dealerService.criar(dealerRequestDTOValido());

        assertThat(dealerResponseDTO.id()).isNotNull();
        assertThat(dealerResponseDTO.razaoSocial()).isEqualTo("Auto Center Toyota Ltda");
        assertThat(dealerResponseDTO.endereco().cidade()).isEqualTo("São Paulo");
    }

    @Test
    void deveListarTodosOsDealers() {
        dealerService.criar(dealerRequestDTOValido());
        dealerService.criar(new DealerRequestDTO(
                "Honda Sul Ltda",
                "11222333000144",
                new EnderecoDTO("Rua das Flores, 200", "Batel", "Curitiba", "PR", "80420100")));

        List<DealerResponseDTO> dealerResponseDTOs = dealerService.listarTodos();

        assertThat(dealerResponseDTOs).hasSize(2);
    }

    @Test
    void deveBuscarDealerPorId() {
        DealerResponseDTO dealerResponseDTO = dealerService.criar(dealerRequestDTOValido());

        DealerResponseDTO foundDealerResponseDTO = dealerService.buscarPorId(dealerResponseDTO.id());

        assertThat(foundDealerResponseDTO.id()).isEqualTo(dealerResponseDTO.id());
        assertThat(foundDealerResponseDTO.cnpj()).isEqualTo("12345678000199");
    }

    @Test
    void deveLancarExcecaoAoBuscarIdInexistente() {
        assertThatThrownBy(() -> dealerService.buscarPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void deveAtualizarDealerExistente() {
        DealerResponseDTO dealerResponseDTO = dealerService.criar(dealerRequestDTOValido());
        DealerRequestDTO dealerAtualizacaoRequestDTO = new DealerRequestDTO(
                "Auto Center Toyota S.A.",
                "12345678000199",
                new EnderecoDTO("Av. Nova, 500", "Jardins", "Campinas", "SP", "13010000"));

        DealerResponseDTO updatedDealerResponseDTO =
                dealerService.atualizar(dealerResponseDTO.id(), dealerAtualizacaoRequestDTO);

        assertThat(updatedDealerResponseDTO.id()).isEqualTo(dealerResponseDTO.id());
        assertThat(updatedDealerResponseDTO.razaoSocial()).isEqualTo("Auto Center Toyota S.A.");
        assertThat(updatedDealerResponseDTO.endereco().cidade()).isEqualTo("Campinas");
    }

    @Test
    void deveLancarExcecaoAoAtualizarIdInexistente() {
        assertThatThrownBy(() -> dealerService.atualizar(999L, dealerRequestDTOValido()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveExcluirDealer() {
        DealerResponseDTO dealerResponseDTO = dealerService.criar(dealerRequestDTOValido());

        dealerService.excluir(dealerResponseDTO.id());

        assertThatThrownBy(() -> dealerService.buscarPorId(dealerResponseDTO.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveLancarExcecaoAoExcluirIdInexistente() {
        assertThatThrownBy(() -> dealerService.excluir(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
