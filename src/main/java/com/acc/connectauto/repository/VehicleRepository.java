package com.acc.connectauto.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acc.connectauto.entity.FuelType;
import com.acc.connectauto.entity.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByMarca(String marca);
    List<Vehicle> findByTipoCombustivel(FuelType tipoCombustivel);
    Optional<Vehicle> findByChassi(String chassi);
}
