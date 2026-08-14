package com.acc.connectauto.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import com.acc.connectauto.client.ViaCepClient;
import com.acc.connectauto.dto.EnderecoDTO;
import com.acc.connectauto.dto.ViaCepResponseDTO;
import com.acc.connectauto.dto.request.DealerRequestDTO;
import com.acc.connectauto.dto.response.DealerResponseDTO;
import com.acc.connectauto.entity.Dealer;
import com.acc.connectauto.entity.Endereco;
import com.acc.connectauto.exception.CepIndisponivelException;
import com.acc.connectauto.exception.CepInvalidoException;
import com.acc.connectauto.exception.DealerComVeiculosAssociadosException;
import com.acc.connectauto.mapper.DealerMapper;
import com.acc.connectauto.mapper.ViaCepMapper;
import com.acc.connectauto.repository.DealerRepository;
import com.acc.connectauto.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealerService {

    private final DealerRepository dealerRepository;
    private final VehicleRepository vehicleRepository;
    private final DealerMapper dealerMapper;
    private final ViaCepMapper viaCepMapper;
    private final ViaCepClient viaCepClient;

    // Sem @Transactional aqui de propósito: a consulta ao ViaCEP é uma chamada HTTP
    // síncrona que não deve segurar uma conexão do pool de banco enquanto espera resposta.
    // dealerRepository.save() já é transacional por conta própria (SimpleJpaRepository).
    public DealerResponseDTO criar(DealerRequestDTO dealerRequestDTO) {
        validarEnderecoInformado(dealerRequestDTO);
        EnderecoDTO enderecoOficialDTO = resolverEnderecoOficial(dealerRequestDTO.endereco());
        Dealer dealer = dealerMapper.toEntity(comEndereco(dealerRequestDTO, enderecoOficialDTO));
        Dealer savedDealer = dealerRepository.save(dealer);
        log.info("Concessionária criada: id={}, cnpj={}", savedDealer.getId(), savedDealer.getCnpj());
        return dealerMapper.toDTO(savedDealer);
    }

    @Transactional(readOnly = true)
    public List<DealerResponseDTO> listarTodos() {
        return dealerRepository.findAll().stream()
                .map(dealerMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public DealerResponseDTO buscarPorId(Long dealerId) {
        return dealerMapper.toDTO(buscarEntidadePorId(dealerId));
    }

    // Mesmo motivo do criar(): sem @Transactional própria, pra não segurar conexão de
    // banco durante a chamada ao ViaCEP.
    public DealerResponseDTO atualizar(Long dealerId, DealerRequestDTO dealerRequestDTO) {
        validarEnderecoInformado(dealerRequestDTO);
        // Busca o dealer ANTES de consultar o ViaCEP: se o id não existir, falha rápido
        // (404) sem gastar uma chamada HTTP externa desnecessária.
        Dealer dealer = buscarEntidadePorId(dealerId);

        EnderecoDTO enderecoOficialDTO = cepPermaneceuOMesmo(dealerRequestDTO, dealer)
                ? enderecoAtualDoDealer(dealer)
                : resolverEnderecoOficial(dealerRequestDTO.endereco());

        dealerMapper.updateEntityFromDto(comEndereco(dealerRequestDTO, enderecoOficialDTO), dealer);
        Dealer updatedDealer = dealerRepository.save(dealer);
        log.info("Concessionária atualizada: id={}", updatedDealer.getId());
        return dealerMapper.toDTO(updatedDealer);
    }

    @Transactional
    public void excluir(Long dealerId) {
        Dealer dealer = buscarEntidadePorId(dealerId);
        // Checagem proativa em vez de deixar a FK do banco rejeitar o delete: o Hibernate
        // faz sua própria validação de consistência do grafo de entidades no flush, ANTES
        // de qualquer SQL ir ao banco, e lançaria TransientPropertyValueException nesse
        // caso — um erro confuso e diferente do DataIntegrityViolationException esperado.
        if (vehicleRepository.existsByDealer_Id(dealerId)) {
            throw new DealerComVeiculosAssociadosException(
                    "Não é possível excluir a concessionária " + dealerId + ": existem veículos associados a ela.");
        }
        dealerRepository.delete(dealer);
        log.info("Concessionária excluída: id={}", dealerId);
    }

    private Dealer buscarEntidadePorId(Long dealerId) {
        return EntityFinder.buscarOuLancar(dealerRepository, dealerId,
                () -> "Concessionária não encontrada com id " + dealerId);
    }

    private void validarEnderecoInformado(DealerRequestDTO dealerRequestDTO) {
        // Defesa contra chamada indevida ao service fora do fluxo HTTP validado por @Valid
        // (ex.: outro service, job em lote, teste) — transforma um NPE opaco em uma
        // mensagem clara sobre qual contrato foi violado.
        Objects.requireNonNull(dealerRequestDTO.endereco(), "endereco não pode ser nulo");
    }

    private boolean cepPermaneceuOMesmo(DealerRequestDTO dealerRequestDTO, Dealer dealer) {
        return dealerRequestDTO.endereco().cep().equals(dealer.getEndereco().getCep());
    }

    // CEP não mudou: evita uma chamada desnecessária ao ViaCEP reaproveitando o endereço
    // oficial já resolvido e persistido na atualização/criação anterior.
    private EnderecoDTO enderecoAtualDoDealer(Dealer dealer) {
        Endereco enderecoAtual = dealer.getEndereco();
        return new EnderecoDTO(
                enderecoAtual.getLogradouro(),
                enderecoAtual.getBairro(),
                enderecoAtual.getCidade(),
                enderecoAtual.getEstado(),
                enderecoAtual.getCep());
    }

    private DealerRequestDTO comEndereco(DealerRequestDTO dealerRequestDTO, EnderecoDTO enderecoDTO) {
        return new DealerRequestDTO(dealerRequestDTO.razaoSocial(), dealerRequestDTO.cnpj(), enderecoDTO);
    }

    // Consulta o ViaCEP e monta o endereço oficial (logradouro/bairro/cidade/estado
    // vindos da API, cep mantido do que foi digitado). O ViaCEP é tratado como fonte da
    // verdade para esses campos, evitando divergência entre o CEP e o endereço salvo.
    private EnderecoDTO resolverEnderecoOficial(EnderecoDTO enderecoOriginalDTO) {
        String cep = enderecoOriginalDTO.cep();
        ViaCepResponseDTO viaCepResponseDTO = consultarViaCep(cep);
        ViaCepResponseDTO viaCepResponseComFallbackDTO = aplicarFallbackParaCamposNulos(viaCepResponseDTO, enderecoOriginalDTO);
        return viaCepMapper.toEnderecoDTO(viaCepResponseComFallbackDTO, cep);
    }

    // CEPs "genéricos" (comuns em cidades pequenas) podem vir do ViaCEP com logradouro
    // e/ou bairro nulos, mesmo sem erro=true. As colunas de Endereco são NOT NULL, então
    // mantemos o que o usuário digitou nesses dois campos quando o ViaCEP não tem o dado
    // — evita quebrar o save() com DataIntegrityViolationException por um campo nulo.
    private ViaCepResponseDTO aplicarFallbackParaCamposNulos(ViaCepResponseDTO viaCepResponseDTO, EnderecoDTO enderecoOriginalDTO) {
        String logradouro = viaCepResponseDTO.logradouro() != null
                ? viaCepResponseDTO.logradouro() : enderecoOriginalDTO.logradouro();
        String bairro = viaCepResponseDTO.bairro() != null
                ? viaCepResponseDTO.bairro() : enderecoOriginalDTO.bairro();

        return new ViaCepResponseDTO(
                viaCepResponseDTO.cep(), logradouro, bairro,
                viaCepResponseDTO.localidade(), viaCepResponseDTO.uf(), viaCepResponseDTO.erro());
    }

    // Trata os dois jeitos que uma consulta ao ViaCEP pode "dar errado": falha na chamada
    // HTTP (rede indisponível, timeout — CepIndisponivelException, 503, falha do ViaCEP)
    // e CEP inexistente (a API responde 200 OK com {"erro": true} — CepInvalidoException,
    // 400, erro do cliente). São causas diferentes e por isso exceções/status diferentes.
    private ViaCepResponseDTO consultarViaCep(String cep) {
        ViaCepResponseDTO viaCepResponseDTO;
        try {
            viaCepResponseDTO = viaCepClient.buscarEnderecoPorCep(cep);
        } catch (RestClientException restClientException) {
            log.warn("Falha ao consultar ViaCEP para o CEP {}: {}", cep, restClientException.getMessage());
            throw new CepIndisponivelException(
                    "Não foi possível validar o CEP " + cep + " no momento; tente novamente em instantes.");
        }

        if (viaCepResponseDTO == null || Boolean.TRUE.equals(viaCepResponseDTO.erro())) {
            throw new CepInvalidoException("CEP " + cep + " não encontrado");
        }

        log.info("Consulta ViaCEP para o CEP {}: {}", cep, viaCepResponseDTO);
        return viaCepResponseDTO;
    }
}
