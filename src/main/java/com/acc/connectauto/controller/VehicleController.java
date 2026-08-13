package com.acc.connectauto.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.acc.connectauto.dto.request.VehicleRequestDTO;
import com.acc.connectauto.dto.response.VehicleResponseDTO;
import com.acc.connectauto.service.VehicleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints REST de veículos. Expõe apenas {@link VehicleService} e DTOs.
 */
@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<List<VehicleResponseDTO>> listarTodos() {
        return ResponseEntity.ok(vehicleService.listarTodos());
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<VehicleResponseDTO> buscarPorId(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(vehicleService.buscarPorId(vehicleId));
    }

    @PostMapping
    public ResponseEntity<VehicleResponseDTO> criar(@Valid @RequestBody VehicleRequestDTO vehicleRequestDTO) {
        VehicleResponseDTO vehicleResponseDTO = vehicleService.criar(vehicleRequestDTO);

        URI vehicleUri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{vehicleId}")
                .buildAndExpand(vehicleResponseDTO.id())
                .toUri();

        return ResponseEntity.created(vehicleUri).body(vehicleResponseDTO);
    }

    @PutMapping("/{vehicleId}")
    public ResponseEntity<VehicleResponseDTO> atualizar(
            @PathVariable Long vehicleId,
            @Valid @RequestBody VehicleRequestDTO vehicleRequestDTO) {
        return ResponseEntity.ok(vehicleService.atualizar(vehicleId, vehicleRequestDTO));
    }

    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<Void> excluir(@PathVariable Long vehicleId) {
        vehicleService.excluir(vehicleId);
        return ResponseEntity.noContent().build();
    }
}
