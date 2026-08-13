package com.acc.connectauto.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import com.acc.connectauto.client.ViaCepClient;
import com.acc.connectauto.dto.EnderecoDTO;
import com.acc.connectauto.dto.ViaCepResponseDTO;
import com.acc.connectauto.dto.request.DealerRequestDTO;
import com.acc.connectauto.dto.response.DealerResponseDTO;
import com.acc.connectauto.entity.Dealer;
import com.acc.connectauto.exception.CepInvalidoException;
import com.acc.connectauto.exception.ResourceNotFoundException;
import com.acc.connectauto.mapper.DealerMapper;
import com.acc.connectauto.repository.DealerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealerService {

    private final DealerRepository dealerRepository;
    private final DealerMapper dealerMapper;
    private final ViaCepClient viaCepClient;

    @Transactional
    public DealerResponseDTO criar(DealerRequestDTO dealerRequestDTO) {
        DealerRequestDTO dealerRequestComEnderecoOficialDTO = preencherEnderecoComViaCep(dealerRequestDTO);
        Dealer dealer = dealerMapper.toEntity(dealerRequestComEnderecoOficialDTO);
        Dealer savedDealer = dealerRepository.save(dealer);
        return dealerMapper.toDTO(savedDealer);
    }

    // readOnly = true: dispensa o dirty-checking do Hibernate nesta transação de leitura.
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

    @Transactional
    public DealerResponseDTO atualizar(Long dealerId, DealerRequestDTO dealerRequestDTO) {
        DealerRequestDTO dealerRequestComEnderecoOficialDTO = preencherEnderecoComViaCep(dealerRequestDTO);
        Dealer dealer = buscarEntidadePorId(dealerId);
        dealerMapper.updateEntityFromDto(dealerRequestComEnderecoOficialDTO, dealer);
        Dealer updatedDealer = dealerRepository.save(dealer);
        return dealerMapper.toDTO(updatedDealer);
    }

    @Transactional
    public void excluir(Long dealerId) {
        Dealer dealer = buscarEntidadePorId(dealerId);
        dealerRepository.delete(dealer);
    }

    private Dealer buscarEntidadePorId(Long dealerId) {
        return dealerRepository.findById(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException("Concessionária não encontrada com id " + dealerId));
    }

    // Substitui logradouro, bairro, cidade e estado pelo retorno oficial do ViaCEP,
    // mantendo apenas o CEP digitado pelo usuário. O ViaCEP é tratado como fonte da
    // verdade para esses campos, evitando divergência entre o CEP e o endereço salvo.
    private DealerRequestDTO preencherEnderecoComViaCep(DealerRequestDTO dealerRequestDTO) {
        String cep = dealerRequestDTO.endereco().cep();
        ViaCepResponseDTO viaCepResponseDTO = consultarViaCep(cep);

        EnderecoDTO enderecoOficialDTO = new EnderecoDTO(
                viaCepResponseDTO.logradouro(),
                viaCepResponseDTO.bairro(),
                viaCepResponseDTO.localidade(),
                viaCepResponseDTO.uf(),
                cep);

        return new DealerRequestDTO(dealerRequestDTO.razaoSocial(), dealerRequestDTO.cnpj(), enderecoOficialDTO);
    }

    // Trata os dois jeitos que uma consulta ao ViaCEP pode "dar errado": falha na chamada
    // HTTP (rede indisponível, CEP fora do formato aceito pela API) e CEP inexistente
    // (a API responde 200 OK com o corpo {"erro": true}, não com um status de erro HTTP).
    private ViaCepResponseDTO consultarViaCep(String cep) {
        ViaCepResponseDTO viaCepResponseDTO;
        try {
            viaCepResponseDTO = viaCepClient.buscarEnderecoPorCep(cep);
        } catch (RestClientException restClientException) {
            log.warn("Falha ao consultar ViaCEP para o CEP {}: {}", cep, restClientException.getMessage());
            throw new CepInvalidoException("Não foi possível validar o CEP " + cep + " junto ao ViaCEP");
        }

        if (viaCepResponseDTO == null || Boolean.TRUE.equals(viaCepResponseDTO.erro())) {
            throw new CepInvalidoException("CEP " + cep + " não encontrado");
        }

        log.info("Consulta ViaCEP para o CEP {}: {}", cep, viaCepResponseDTO);
        return viaCepResponseDTO;
    }
}
