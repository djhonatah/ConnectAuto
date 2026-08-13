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

@SpringBootTest
@Transactional
class DealerServiceTest {

    @Autowired
    private DealerService dealerService;

    private DealerRequestDTO requestValido() {
        return new DealerRequestDTO(
                "Auto Center Toyota Ltda",
                "12345678000199",
                new EnderecoDTO("Av. Principal, 100", "Centro", "São Paulo", "SP", "01310100"));
    }

    @Test
    void deveCriarDealer() {
        DealerResponseDTO criado = dealerService.criar(requestValido());

        assertThat(criado.id()).isNotNull();
        assertThat(criado.razaoSocial()).isEqualTo("Auto Center Toyota Ltda");
        assertThat(criado.endereco().cidade()).isEqualTo("São Paulo");
    }

    @Test
    void deveListarTodosOsDealers() {
        dealerService.criar(requestValido());
        dealerService.criar(new DealerRequestDTO(
                "Honda Sul Ltda",
                "11222333000144",
                new EnderecoDTO("Rua das Flores, 200", "Batel", "Curitiba", "PR", "80420100")));

        List<DealerResponseDTO> todos = dealerService.listarTodos();

        assertThat(todos).hasSize(2);
    }

    @Test
    void deveBuscarDealerPorId() {
        DealerResponseDTO criado = dealerService.criar(requestValido());

        DealerResponseDTO encontrado = dealerService.buscarPorId(criado.id());

        assertThat(encontrado.id()).isEqualTo(criado.id());
        assertThat(encontrado.cnpj()).isEqualTo("12345678000199");
    }

    @Test
    void deveLancarExcecaoAoBuscarIdInexistente() {
        assertThatThrownBy(() -> dealerService.buscarPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void deveAtualizarDealerExistente() {
        DealerResponseDTO criado = dealerService.criar(requestValido());
        DealerRequestDTO atualizacao = new DealerRequestDTO(
                "Auto Center Toyota S.A.",
                "12345678000199",
                new EnderecoDTO("Av. Nova, 500", "Jardins", "Campinas", "SP", "13010000"));

        DealerResponseDTO atualizado = dealerService.atualizar(criado.id(), atualizacao);

        assertThat(atualizado.id()).isEqualTo(criado.id());
        assertThat(atualizado.razaoSocial()).isEqualTo("Auto Center Toyota S.A.");
        assertThat(atualizado.endereco().cidade()).isEqualTo("Campinas");
    }

    @Test
    void deveLancarExcecaoAoAtualizarIdInexistente() {
        assertThatThrownBy(() -> dealerService.atualizar(999L, requestValido()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveExcluirDealer() {
        DealerResponseDTO criado = dealerService.criar(requestValido());

        dealerService.excluir(criado.id());

        assertThatThrownBy(() -> dealerService.buscarPorId(criado.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveLancarExcecaoAoExcluirIdInexistente() {
        assertThatThrownBy(() -> dealerService.excluir(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
