package com.acc.connectauto.seed;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.acc.connectauto.entity.Dealer;
import com.acc.connectauto.entity.Endereco;
import com.acc.connectauto.entity.FuelType;
import com.acc.connectauto.entity.Vehicle;
import com.acc.connectauto.repository.DealerRepository;
import com.acc.connectauto.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Popula concessionárias e veículos de demonstração para o dashboard ter
 * dados reais desde o primeiro acesso — sem depender de cadastro manual.
 * Roda direto contra os repositórios (não pelos services), pra não bater no
 * ViaCEP a cada dealer. Desligado nos testes via
 * connectauto.demo-data.enabled=false (src/test/resources/application.properties),
 * já que vários testes fazem asserções de contagem exata na base.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "connectauto.demo-data", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements CommandLineRunner {

    private final DealerRepository dealerRepository;
    private final VehicleRepository vehicleRepository;

    private record DealerSeed(String razaoSocial, String cnpj, Endereco endereco) {
    }

    private record VehicleSeed(
            int dealerIndex, String marca, String modelo, FuelType tipoCombustivel,
            String cor, int ano, String valor, String corInterna) {
    }

    private static final List<DealerSeed> DEALERS = List.of(
            new DealerSeed("Auto Nexus São Paulo Ltda", "11222333000181",
                    new Endereco("Av. Brigadeiro Faria Lima, 2100", "Itaim Bibi", "São Paulo", "SP", "04538132")),
            new DealerSeed("Motor Campinas Comércio de Veículos Ltda", "11444777000242",
                    new Endereco("Av. Francisco Glicério, 850", "Centro", "Campinas", "SP", "13010141")),
            new DealerSeed("ConnectCar Curitiba Ltda", "12345678000357",
                    new Endereco("Rua XV de Novembro, 500", "Centro", "Curitiba", "PR", "80020310")),
            new DealerSeed("Litoral Motors Recife Ltda", "22334455000429",
                    new Endereco("Av. Boa Viagem, 3200", "Boa Viagem", "Recife", "PE", "51020000")),
            new DealerSeed("Bahia Auto Center Ltda", "33445566000500",
                    new Endereco("Av. Tancredo Neves, 148", "Caminho das Árvores", "Salvador", "BA", "41820000")),
            new DealerSeed("Ceará Veículos Ltda", "44556677000690",
                    new Endereco("Av. Santos Dumont, 1500", "Aldeota", "Fortaleza", "CE", "60150160")),
            new DealerSeed("Minas Motors Ltda", "55667788000771",
                    new Endereco("Av. do Contorno, 6000", "Savassi", "Belo Horizonte", "MG", "30110110")),
            new DealerSeed("Sul Auto Premium Ltda", "66778899000852",
                    new Endereco("Rua Padre Chagas, 300", "Moinhos de Vento", "Porto Alegre", "RS", "90570080")));

    private static final List<VehicleSeed> VEHICLES = List.of(
            new VehicleSeed(0, "Toyota", "Corolla Cross", FuelType.FLEX, "Prata", 2024, "148900.00", "Preto"),
            new VehicleSeed(0, "Toyota", "Corolla Hybrid", FuelType.HIBRIDO, "Branco", 2023, "172300.00", "Bege"),
            new VehicleSeed(0, "Volkswagen", "T-Cross", FuelType.FLEX, "Branco", 2023, "119900.00", null),
            new VehicleSeed(0, "Chevrolet", "Onix", FuelType.FLEX, "Vermelho", 2022, "84900.00", null),
            new VehicleSeed(0, "BYD", "Dolphin", FuelType.ELETRICO, "Vermelho", 2024, "129800.00", "Preto"),
            new VehicleSeed(0, "Honda", "HR-V", FuelType.FLEX, "Cinza", 2023, "139500.00", null),
            new VehicleSeed(0, "Jeep", "Renegade", FuelType.FLEX, "Preto", 2022, "112400.00", null),
            new VehicleSeed(0, "Hyundai", "Creta", FuelType.FLEX, "Branco", 2024, "134900.00", "Preto"),
            new VehicleSeed(1, "BYD", "Song Plus", FuelType.HIBRIDO, "Branco", 2024, "219800.00", "Bege"),
            new VehicleSeed(1, "Fiat", "Pulse", FuelType.FLEX, "Cinza", 2023, "98400.00", null),
            new VehicleSeed(1, "Nissan", "Kicks", FuelType.FLEX, "Prata", 2022, "104900.00", null),
            new VehicleSeed(1, "Renault", "Kwid", FuelType.FLEX, "Vermelho", 2021, "68900.00", null),
            new VehicleSeed(1, "Volkswagen", "Nivus", FuelType.FLEX, "Azul", 2023, "116500.00", null),
            new VehicleSeed(2, "Jeep", "Compass", FuelType.DIESEL, "Cinza Grafite", 2023, "189500.00", "Preto"),
            new VehicleSeed(2, "Toyota", "Hilux", FuelType.DIESEL, "Prata", 2022, "249900.00", "Preto"),
            new VehicleSeed(2, "Fiat", "Toro", FuelType.DIESEL, "Branco", 2023, "198700.00", null),
            new VehicleSeed(2, "GWM", "Haval H6", FuelType.HIBRIDO, "Preto", 2024, "229900.00", "Preto"),
            new VehicleSeed(3, "Toyota", "Corolla Hybrid", FuelType.HIBRIDO, "Branco", 2022, "165900.00", null),
            new VehicleSeed(3, "BYD", "Seal", FuelType.ELETRICO, "Branco", 2024, "259800.00", "Preto"),
            new VehicleSeed(3, "Hyundai", "HB20", FuelType.FLEX, "Vermelho", 2023, "82900.00", null),
            new VehicleSeed(3, "Chevrolet", "Tracker", FuelType.FLEX, "Branco", 2023, "128900.00", null),
            new VehicleSeed(3, "BYD", "Dolphin", FuelType.ELETRICO, "Azul", 2023, "124800.00", null),
            new VehicleSeed(3, "Honda", "Civic", FuelType.FLEX, "Preto", 2022, "149900.00", "Preto"),
            new VehicleSeed(4, "Fiat", "Argo", FuelType.FLEX, "Prata", 2022, "78900.00", null),
            new VehicleSeed(4, "BYD", "Dolphin Mini", FuelType.ELETRICO, "Amarelo", 2024, "99800.00", null),
            new VehicleSeed(4, "Volkswagen", "Polo", FuelType.FLEX, "Cinza", 2023, "92900.00", null),
            new VehicleSeed(5, "Fiat", "Pulse", FuelType.FLEX, "Vermelho", 2024, "101900.00", null),
            new VehicleSeed(5, "BYD", "Song Plus", FuelType.HIBRIDO, "Prata", 2023, "209800.00", null),
            new VehicleSeed(6, "Jeep", "Compass", FuelType.DIESEL, "Branco", 2024, "194900.00", "Preto"),
            new VehicleSeed(6, "Hyundai", "Creta", FuelType.FLEX, "Azul", 2022, "118900.00", null),
            new VehicleSeed(7, "GWM", "Haval H6", FuelType.HIBRIDO, "Cinza", 2023, "219900.00", null));

    @Override
    public void run(String... args) {
        if (dealerRepository.count() > 0) {
            return;
        }

        List<Dealer> dealers = DEALERS.stream()
                .map(seed -> dealerRepository.save(Dealer.builder()
                        .razaoSocial(seed.razaoSocial())
                        .cnpj(seed.cnpj())
                        .endereco(seed.endereco())
                        .build()))
                .toList();

        int seq = 1;
        for (VehicleSeed seed : VEHICLES) {
            vehicleRepository.save(Vehicle.builder()
                    .marca(seed.marca())
                    .modelo(seed.modelo())
                    .tipoCombustivel(seed.tipoCombustivel())
                    .cor(seed.cor())
                    .ano(seed.ano())
                    .chassi(chassi(seq++))
                    .valor(new BigDecimal(seed.valor()))
                    .corInterna(seed.corInterna())
                    .dealer(dealers.get(seed.dealerIndex()))
                    .build());
        }

        log.info("Dados de demonstração seedados: {} concessionárias, {} veículos.",
                dealers.size(), VEHICLES.size());
    }

    private String chassi(int seq) {
        return "9BD%06dCA%06d".formatted(seq, seq * 3 + 101);
    }
}
