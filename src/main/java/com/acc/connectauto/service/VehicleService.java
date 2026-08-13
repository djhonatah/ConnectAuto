package com.acc.connectauto.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acc.connectauto.dto.request.VehicleRequestDTO;
import com.acc.connectauto.dto.response.VehicleResponseDTO;
import com.acc.connectauto.entity.Vehicle;
import com.acc.connectauto.exception.ResourceNotFoundException;
import com.acc.connectauto.mapper.VehicleMapper;
import com.acc.connectauto.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;

    @Transactional
    public VehicleResponseDTO criar(VehicleRequestDTO request) {
        Vehicle vehicle = vehicleMapper.toEntity(request);
        Vehicle salvo = vehicleRepository.save(vehicle);
        return vehicleMapper.toDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> listarTodos() {
        return vehicleRepository.findAll().stream()
                .map(vehicleMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehicleResponseDTO buscarPorId(Long id) {
        return vehicleMapper.toDTO(buscarEntidadePorId(id));
    }

    @Transactional
    public VehicleResponseDTO atualizar(Long id, VehicleRequestDTO request) {
        Vehicle vehicle = buscarEntidadePorId(id);
        vehicleMapper.updateEntityFromDto(request, vehicle);
        Vehicle atualizado = vehicleRepository.save(vehicle);
        return vehicleMapper.toDTO(atualizado);
    }

    @Transactional
    public void excluir(Long id) {
        Vehicle vehicle = buscarEntidadePorId(id);
        vehicleRepository.delete(vehicle);
    }

    private Vehicle buscarEntidadePorId(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com id " + id));
    }
}
