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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DealerRepository dealerRepository;
    private final VehicleMapper vehicleMapper;

    @Transactional
    public VehicleResponseDTO criar(VehicleRequestDTO vehicleRequestDTO) {
        Vehicle vehicle = vehicleMapper.toEntity(vehicleRequestDTO);
        Vehicle savedVehicle = salvarComDealer(vehicle, vehicleRequestDTO.dealerId());
        log.info("Veículo criado: id={}, chassi={}", savedVehicle.getId(), savedVehicle.getChassi());
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
        Vehicle updatedVehicle = salvarComDealer(vehicle, vehicleRequestDTO.dealerId());
        log.info("Veículo atualizado: id={}", updatedVehicle.getId());
        return vehicleMapper.toDTO(updatedVehicle);
    }

    @Transactional
    public void excluir(Long vehicleId) {
        Vehicle vehicle = buscarEntidadePorId(vehicleId);
        vehicleRepository.delete(vehicle);
        log.info("Veículo excluído: id={}", vehicleId);
    }

    @Transactional
    public VehicleResponseDTO associarDealer(Long vehicleId, VehicleDealerRequestDTO vehicleDealerRequestDTO) {
        Vehicle vehicle = buscarEntidadePorId(vehicleId);
        Vehicle updatedVehicle = salvarComDealer(vehicle, vehicleDealerRequestDTO.dealerId());
        log.info("Veículo {} associado à concessionária: dealerId={}", vehicleId, vehicleDealerRequestDTO.dealerId());
        return vehicleMapper.toDTO(updatedVehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> listarPorDealer(Long dealerId) {
        List<Vehicle> vehicles = vehicleRepository.findByDealer_Id(dealerId);
        if (vehicles.isEmpty() && !dealerRepository.existsById(dealerId)) {
            throw new ResourceNotFoundException("Concessionária não encontrada com id " + dealerId);
        }
        return vehicles.stream()
                .map(vehicleMapper::toDTO)
                .toList();
    }

    private Vehicle salvarComDealer(Vehicle vehicle, Long dealerId) {
        if (vehicle.getChassi() != null && vehicle.getChassi().isBlank()) {
            vehicle.setChassi(null);
        }
        vehicle.setDealer(buscarDealerOpcional(dealerId));
        return vehicleRepository.save(vehicle);
    }

    private Vehicle buscarEntidadePorId(Long vehicleId) {
        return EntityFinder.buscarOuLancar(vehicleRepository, vehicleId,
                () -> "Veículo não encontrado com id " + vehicleId);
    }

    private Dealer buscarDealerOpcional(Long dealerId) {
        if (dealerId == null) {
            return null;
        }
        return EntityFinder.buscarOuLancar(dealerRepository, dealerId,
                () -> "Concessionária não encontrada com id " + dealerId);
    }
}
