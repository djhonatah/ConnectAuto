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
    public DealerResponseDTO criar(DealerRequestDTO request) {
        Dealer dealer = dealerMapper.toEntity(request);
        Dealer salvo = dealerRepository.save(dealer);
        return dealerMapper.toDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<DealerResponseDTO> listarTodos() {
        return dealerRepository.findAll().stream()
                .map(dealerMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public DealerResponseDTO buscarPorId(Long id) {
        return dealerMapper.toDTO(buscarEntidadePorId(id));
    }

    @Transactional
    public DealerResponseDTO atualizar(Long id, DealerRequestDTO request) {
        Dealer dealer = buscarEntidadePorId(id);
        dealerMapper.updateEntityFromDto(request, dealer);
        Dealer atualizado = dealerRepository.save(dealer);
        return dealerMapper.toDTO(atualizado);
    }

    @Transactional
    public void excluir(Long id) {
        Dealer dealer = buscarEntidadePorId(id);
        dealerRepository.delete(dealer);
    }

    private Dealer buscarEntidadePorId(Long id) {
        return dealerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concessionária não encontrada com id " + id));
    }
}
