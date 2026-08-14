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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/dealer")
@RequiredArgsConstructor
public class DealerController {

    private final DealerService dealerService;

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
        log.info("POST /dealer - criando concessionária: razaoSocial={}", dealerRequestDTO.razaoSocial());
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
        log.info("PUT /dealer/{} - atualizando concessionária", dealerId);
        return ResponseEntity.ok(dealerService.atualizar(dealerId, dealerRequestDTO));
    }

    @DeleteMapping("/{dealerId}")
    public ResponseEntity<Void> excluir(@PathVariable Long dealerId) {
        log.info("DELETE /dealer/{} - excluindo concessionária", dealerId);
        dealerService.excluir(dealerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{dealerId}/vehicles")
    public ResponseEntity<List<VehicleResponseDTO>> listarVeiculos(@PathVariable Long dealerId) {
        return ResponseEntity.ok(vehicleService.listarPorDealer(dealerId));
    }
}
