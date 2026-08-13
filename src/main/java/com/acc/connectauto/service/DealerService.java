package com.acc.connectauto.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acc.connectauto.dto.request.DealerRequestDTO;
import com.acc.connectauto.dto.response.DealerResponseDTO;
import com.acc.connectauto.entity.Dealer;
import com.acc.connectauto.exception.ResourceNotFoundException;
import com.acc.connectauto.mapper.DealerMapper;
import com.acc.connectauto.repository.DealerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DealerService {

    private final DealerRepository dealerRepository;
    private final DealerMapper dealerMapper;

    @Transactional
    public DealerResponseDTO criar(DealerRequestDTO dealerRequestDTO) {
        Dealer dealer = dealerMapper.toEntity(dealerRequestDTO);
        Dealer savedDealer = dealerRepository.save(dealer);
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

    @Transactional
    public DealerResponseDTO atualizar(Long dealerId, DealerRequestDTO dealerRequestDTO) {
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
}
