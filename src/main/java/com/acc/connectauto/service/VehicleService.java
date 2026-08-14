package com.acc.connectauto.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acc.connectauto.dto.request.VehicleDealerRequestDTO;
import com.acc.connectauto.dto.request.VehicleRequestDTO;
import com.acc.connectauto.dto.response.VehicleResponseDTO;
import com.acc.connectauto.entity.Dealer;
import com.acc.connectauto.entity.Vehicle;
import com.acc.connectauto.exception.ResourceNotFoundException;
import com.acc.connectauto.mapper.VehicleMapper;
import com.acc.connectauto.repository.DealerRepository;
import com.acc.connectauto.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DealerRepository dealerRepository;
    private final VehicleMapper vehicleMapper;

    @Transactional
    public VehicleResponseDTO criar(VehicleRequestDTO vehicleRequestDTO) {
        Vehicle vehicle = vehicleMapper.toEntity(vehicleRequestDTO);
        vehicle.setDealer(buscarDealerOpcional(vehicleRequestDTO.dealerId()));
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.toDTO(savedVehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> listarTodos() {
        return vehicleRepository.findAll().stream()
                .map(vehicleMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehicleResponseDTO buscarPorId(Long vehicleId) {
        return vehicleMapper.toDTO(buscarEntidadePorId(vehicleId));
    }

    @Transactional
    public VehicleResponseDTO atualizar(Long vehicleId, VehicleRequestDTO vehicleRequestDTO) {
        Vehicle vehicle = buscarEntidadePorId(vehicleId);
        vehicleMapper.updateEntityFromDto(vehicleRequestDTO, vehicle);
        vehicle.setDealer(buscarDealerOpcional(vehicleRequestDTO.dealerId()));
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.toDTO(updatedVehicle);
    }

    @Transactional
    public void excluir(Long vehicleId) {
        Vehicle vehicle = buscarEntidadePorId(vehicleId);
        vehicleRepository.delete(vehicle);
    }
.
    @Transactional
    public VehicleResponseDTO associarDealer(Long vehicleId, VehicleDealerRequestDTO vehicleDealerRequestDTO) {
        Vehicle vehicle = buscarEntidadePorId(vehicleId);
        vehicle.setDealer(buscarDealerOpcional(vehicleDealerRequestDTO.dealerId()));
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.toDTO(updatedVehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> listarPorDealer(Long dealerId) {
        if (!dealerRepository.existsById(dealerId)) {
            throw new ResourceNotFoundException("Concessionária não encontrada com id " + dealerId);
        }
        return vehicleRepository.findByDealer_Id(dealerId).stream()
                .map(vehicleMapper::toDTO)
                .toList();
    }

    private Vehicle buscarEntidadePorId(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com id " + vehicleId));
    }

    private Dealer buscarDealerOpcional(Long dealerId) {
        if (dealerId == null) {
            return null;
        }
        return dealerRepository.findById(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException("Concessionária não encontrada com id " + dealerId));
    }
}
