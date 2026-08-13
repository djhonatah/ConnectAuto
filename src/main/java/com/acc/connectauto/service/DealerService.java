package com.acc.connectauto.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acc.connectauto.client.ViaCepClient;
import com.acc.connectauto.dto.ViaCepResponseDTO;
import com.acc.connectauto.dto.request.DealerRequestDTO;
import com.acc.connectauto.dto.response.DealerResponseDTO;
import com.acc.connectauto.entity.Dealer;
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
        consultarViaCep(dealerRequestDTO.endereco().cep());
        Dealer dealer = dealerMapper.toEntity(dealerRequestDTO);
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
        consultarViaCep(dealerRequestDTO.endereco().cep());
        Dealer dealer = buscarEntidadePorId(dealerId);
        dealerMapper.updateEntityFromDto(dealerRequestDTO, dealer);
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

    // Passo intermediário da issue #16: só consulta e registra o retorno do ViaCEP.
    // Popular os campos do endereço com essa resposta e tratar CEP inválido/inexistente
    // (sem quebrar a aplicação) ficam para os próximos itens da mesma issue.
    private void consultarViaCep(String cep) {
        ViaCepResponseDTO viaCepResponseDTO = viaCepClient.buscarEnderecoPorCep(cep);
        log.info("Consulta ViaCEP para o CEP {}: {}", cep, viaCepResponseDTO);
    }
}
