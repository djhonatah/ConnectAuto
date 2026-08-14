package com.acc.connectauto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import com.acc.connectauto.client.ViaCepClient;
import com.acc.connectauto.dto.EnderecoDTO;
import com.acc.connectauto.dto.ViaCepResponseDTO;
import com.acc.connectauto.dto.request.DealerRequestDTO;
import com.acc.connectauto.dto.response.DealerResponseDTO;
import com.acc.connectauto.exception.CepIndisponivelException;
import com.acc.connectauto.exception.CepInvalidoException;
import com.acc.connectauto.exception.DealerComVeiculosAssociadosException;
import com.acc.connectauto.exception.ResourceNotFoundException;
import com.acc.connectauto.dto.request.VehicleRequestDTO;
import com.acc.connectauto.entity.FuelType;

@SpringBootTest
@Transactional
class DealerServiceTest {

        @Autowired
        private DealerService dealerService;

        @Autowired
        private VehicleService vehicleService;

        @MockitoBean
        private ViaCepClient viaCepClient;

        @BeforeEach
        void configurarViaCepClient() {
                when(viaCepClient.buscarEnderecoPorCep(anyString())).thenAnswer(invocation -> {
                        String cep = invocation.getArgument(0);
                        return switch (cep) {
                                case "80420100" -> new ViaCepResponseDTO("80420-100", "Rua das Flores", "Batel",
                                                "Curitiba", "PR", null);
                                case "13010000" -> new ViaCepResponseDTO("13010-000", "Av. Nova", "Jardins", "Campinas",
                                                "SP", null);
                                default -> new ViaCepResponseDTO("01310-100", "Av. Principal", "Centro", "São Paulo",
                                                "SP", null);
                        };
                });
        }

        private DealerRequestDTO dealerRequestDTOValido() {
                return new DealerRequestDTO(
                                "Auto Center Toyota Ltda",
                                "12345678000195",
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
                                "11222333000181",
                                new EnderecoDTO("Rua das Flores, 200", "Batel", "Curitiba", "PR", "80420100")));

                List<DealerResponseDTO> dealerResponseDTOs = dealerService.listarTodos();

                assertThat(dealerResponseDTOs).hasSize(2);
        }

        @Test
        void deveBuscarDealerPorId() {
                DealerResponseDTO dealerResponseDTO = dealerService.criar(dealerRequestDTOValido());

                DealerResponseDTO foundDealerResponseDTO = dealerService.buscarPorId(dealerResponseDTO.id());

                assertThat(foundDealerResponseDTO.id()).isEqualTo(dealerResponseDTO.id());
                assertThat(foundDealerResponseDTO.cnpj()).isEqualTo("12345678000195");
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
                                "12345678000195",
                                new EnderecoDTO("Av. Nova, 500", "Jardins", "Campinas", "SP", "13010000"));

                DealerResponseDTO updatedDealerResponseDTO = dealerService.atualizar(dealerResponseDTO.id(),
                                dealerAtualizacaoRequestDTO);

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
        void deveLancarExcecaoAoExcluirDealerComVeiculosAssociados() {
                DealerResponseDTO dealerResponseDTO = dealerService.criar(dealerRequestDTOValido());
                vehicleService.criar(new VehicleRequestDTO(
                                "Toyota", "Corolla", FuelType.FLEX, "Prata",
                                2024, "1HGCM82633A123456", new java.math.BigDecimal("120000.00"), null, dealerResponseDTO.id()));

                assertThatThrownBy(() -> dealerService.excluir(dealerResponseDTO.id()))
                                .isInstanceOf(DealerComVeiculosAssociadosException.class);
        }

        @Test
        void deveLancarExcecaoAoExcluirIdInexistente() {
                assertThatThrownBy(() -> dealerService.excluir(999L))
                                .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void deveConsultarViaCepAoCriarDealer() {
                dealerService.criar(dealerRequestDTOValido());

                verify(viaCepClient).buscarEnderecoPorCep("01310100");
        }

        @Test
        void deveConsultarViaCepAoAtualizarDealer() {
                DealerResponseDTO dealerResponseDTO = dealerService.criar(dealerRequestDTOValido());
                DealerRequestDTO dealerAtualizacaoRequestDTO = new DealerRequestDTO(
                                "Auto Center Toyota S.A.",
                                "12345678000195",
                                new EnderecoDTO("Av. Nova, 500", "Jardins", "Campinas", "SP", "13010000"));

                dealerService.atualizar(dealerResponseDTO.id(), dealerAtualizacaoRequestDTO);

                verify(viaCepClient).buscarEnderecoPorCep("13010000");
        }

        @Test
        void devePopularEnderecoComRetornoDoViaCep() {
                DealerRequestDTO dealerComEnderecoDigitadoDivergenteRequestDTO = new DealerRequestDTO(
                                "Auto Center Toyota Ltda",
                                "12345678000195",
                                new EnderecoDTO("Endereço qualquer", "Bairro qualquer", "Cidade qualquer", "XX",
                                                "01310100"));

                DealerResponseDTO dealerResponseDTO = dealerService
                                .criar(dealerComEnderecoDigitadoDivergenteRequestDTO);

                assertThat(dealerResponseDTO.endereco().logradouro()).isEqualTo("Av. Principal");
                assertThat(dealerResponseDTO.endereco().bairro()).isEqualTo("Centro");
                assertThat(dealerResponseDTO.endereco().cidade()).isEqualTo("São Paulo");
                assertThat(dealerResponseDTO.endereco().estado()).isEqualTo("SP");
                assertThat(dealerResponseDTO.endereco().cep()).isEqualTo("01310100");
        }

        @Test
        void deveLancarExcecaoAoCriarDealerComCepInexistente() {
                when(viaCepClient.buscarEnderecoPorCep("00000000"))
                                .thenReturn(new ViaCepResponseDTO(null, null, null, null, null, true));

                DealerRequestDTO dealerComCepInexistenteRequestDTO = new DealerRequestDTO(
                                "Auto Center Toyota Ltda",
                                "12345678000195",
                                new EnderecoDTO("Av. Principal, 100", "Centro", "São Paulo", "SP", "00000000"));

                assertThatThrownBy(() -> dealerService.criar(dealerComCepInexistenteRequestDTO))
                                .isInstanceOf(CepInvalidoException.class)
                                .hasMessageContaining("00000000");
        }

        @Test
        void deveLancarExcecaoQuandoConsultaAoViaCepFalhar() {
                when(viaCepClient.buscarEnderecoPorCep("99999999"))
                                .thenThrow(new RestClientException("Falha de rede simulada"));

                DealerRequestDTO dealerComFalhaNaConsultaRequestDTO = new DealerRequestDTO(
                                "Auto Center Toyota Ltda",
                                "12345678000195",
                                new EnderecoDTO("Av. Principal, 100", "Centro", "São Paulo", "SP", "99999999"));

                assertThatThrownBy(() -> dealerService.criar(dealerComFalhaNaConsultaRequestDTO))
                                .isInstanceOf(CepIndisponivelException.class);
        }

        @Test
        void deveLancarExcecaoAoCriarDealerComCnpjDuplicado() {
                dealerService.criar(dealerRequestDTOValido());

                DealerRequestDTO dealerComCnpjDuplicadoRequestDTO = new DealerRequestDTO(
                                "Outra Razão Social Ltda",
                                "12345678000195",
                                new EnderecoDTO("Rua das Flores, 200", "Batel", "Curitiba", "PR", "80420100"));

                assertThatThrownBy(() -> dealerService.criar(dealerComCnpjDuplicadoRequestDTO))
                                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void naoDeveConsultarViaCepAoAtualizarDealerIdInexistente() {
                assertThatThrownBy(() -> dealerService.atualizar(999L, dealerRequestDTOValido()))
                                .isInstanceOf(ResourceNotFoundException.class);

                verifyNoInteractions(viaCepClient);
        }

        @Test
        void naoDeveConsultarViaCepDeNovoQuandoCepNaoMuda() {
                DealerResponseDTO dealerResponseDTO = dealerService.criar(dealerRequestDTOValido());
                DealerRequestDTO dealerComMesmoCepRequestDTO = new DealerRequestDTO(
                                "Auto Center Toyota S.A.",
                                "12345678000195",
                                new EnderecoDTO("Av. Principal, 100", "Centro", "São Paulo", "SP", "01310100"));

                DealerResponseDTO updatedDealerResponseDTO = dealerService.atualizar(dealerResponseDTO.id(),
                                dealerComMesmoCepRequestDTO);

                assertThat(updatedDealerResponseDTO.razaoSocial()).isEqualTo("Auto Center Toyota S.A.");
                assertThat(updatedDealerResponseDTO.endereco().cidade()).isEqualTo("São Paulo");
                // A consulta ao ViaCEP só deve ter acontecido uma vez, na criação — o CEP não
                // mudou.
                verify(viaCepClient).buscarEnderecoPorCep("01310100");
        }

        @Test
        void deveManterCampoDigitadoQuandoViaCepRetornaLogradouroOuBairroNulos() {
                when(viaCepClient.buscarEnderecoPorCep("30140071"))
                                .thenReturn(new ViaCepResponseDTO("30140-071", null, null, "Belo Horizonte", "MG",
                                                null));

                DealerRequestDTO dealerComCepGenericoRequestDTO = new DealerRequestDTO(
                                "Auto Center Toyota Ltda",
                                "12345678000195",
                                new EnderecoDTO("Rua Digitada, 10", "Bairro Digitado", "Belo Horizonte", "MG",
                                                "30140071"));

                DealerResponseDTO dealerResponseDTO = dealerService.criar(dealerComCepGenericoRequestDTO);

                assertThat(dealerResponseDTO.endereco().logradouro()).isEqualTo("Rua Digitada, 10");
                assertThat(dealerResponseDTO.endereco().bairro()).isEqualTo("Bairro Digitado");
                assertThat(dealerResponseDTO.endereco().cidade()).isEqualTo("Belo Horizonte");
        }
}
