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

import com.acc.connectauto.dto.request.DealerRequestDTO;
import com.acc.connectauto.dto.response.DealerResponseDTO;
import com.acc.connectauto.dto.response.VehicleResponseDTO;
import com.acc.connectauto.service.DealerService;
import com.acc.connectauto.service.VehicleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
// Rota no singular ("/dealer") conforme especificado na issue #12 —
// inconsistente com o
// plural usado em VehicleController ("/vehicles"). Mantido assim de propósito;
// avaliar
// padronização para "/dealers" se a API crescer.
@RequestMapping("/dealer")
@RequiredArgsConstructor
public class DealerController {

    private final DealerService dealerService;

    // Injetado só para o endpoint aninhado /dealer/{dealerId}/vehicles (issue #15) — o
    // resto do controller continua falando exclusivamente com DealerService.
    private final VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<List<DealerResponseDTO>> listarTodos() {
        return ResponseEntity.ok(dealerService.listarTodos());
    }

    @GetMapping("/{dealerId}")
    public ResponseEntity<DealerResponseDTO> buscarPorId(@PathVariable Long dealerId) {
        return ResponseEntity.ok(dealerService.buscarPorId(dealerId));
    }

    @PostMapping
    public ResponseEntity<DealerResponseDTO> criar(@Valid @RequestBody DealerRequestDTO dealerRequestDTO) {
        DealerResponseDTO dealerResponseDTO = dealerService.criar(dealerRequestDTO);

        URI dealerUri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{dealerId}")
                .buildAndExpand(dealerResponseDTO.id())
                .toUri();

        return ResponseEntity.created(dealerUri).body(dealerResponseDTO);
    }

    @PutMapping("/{dealerId}")
    public ResponseEntity<DealerResponseDTO> atualizar(
            @PathVariable Long dealerId,
            @Valid @RequestBody DealerRequestDTO dealerRequestDTO) {
        return ResponseEntity.ok(dealerService.atualizar(dealerId, dealerRequestDTO));
    }

    @DeleteMapping("/{dealerId}")
    public ResponseEntity<Void> excluir(@PathVariable Long dealerId) {
        dealerService.excluir(dealerId);
        return ResponseEntity.noContent().build();
    }

    // Rota aninhada: lista os veículos da concessionária dealerId. 404 se o dealerId não
    // existir (VehicleService.listarPorDealer valida antes de consultar os veículos).
    @GetMapping("/{dealerId}/vehicles")
    public ResponseEntity<List<VehicleResponseDTO>> listarVeiculos(@PathVariable Long dealerId) {
        return ResponseEntity.ok(vehicleService.listarPorDealer(dealerId));
    }
}
