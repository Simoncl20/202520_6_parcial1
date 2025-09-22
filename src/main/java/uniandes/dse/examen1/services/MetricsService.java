package uniandes.dse.examen1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import uniandes.dse.examen1.entities.FactoryEntity;
import uniandes.dse.examen1.entities.SupplierEntity;
import uniandes.dse.examen1.repositories.SupplierRepository;
import uniandes.dse.examen1.repositories.FactoryRepository;
import uniandes.dse.examen1.repositories.ContractRepository;
import java.util.Optional;

@Slf4j
@Service
public class MetricsService {

    @Autowired
    FactoryRepository factoryRepository;

    @Autowired
    SupplierRepository supplierRepository;

    @Autowired
    ContractRepository contractRepository;

    public Double calculateTotalContractValue(String factoryName) {
        Optional<FactoryEntity> factoryOpt = factoryRepository.findByName(factoryName);
        if (factoryOpt.isEmpty()) {
            return 0.0;
        }

        FactoryEntity factory = factoryOpt.get();
        return factory.getContracts().stream()
            .mapToDouble(contract -> contract.getContractValue())
            .sum();
    }

    public Double calculateSupplierSatisfactionAverage(String supplierCode) {
        Optional<SupplierEntity> supplierOpt = supplierRepository.findBySupplierCode(supplierCode);
        if (supplierOpt.isEmpty()) {
            return 0.0;
        }

        SupplierEntity supplier = supplierOpt.get();
        return supplier.getContracts().stream()
            .filter(contract -> contract.getSatisfaction() > 0) // Excluir contratos sin calificar
            .mapToInt(contract -> contract.getSatisfaction())
            .average()
            .orElse(0.0);
    }

    public Double calculateContractAverageValue(String supplierCode) {
        Optional<SupplierEntity> supplierOpt = supplierRepository.findBySupplierCode(supplierCode);
        if (supplierOpt.isEmpty()) {
            return 0.0;
        }

        SupplierEntity supplier = supplierOpt.get();
        return supplier.getContracts().stream()
            .mapToDouble(contract -> contract.getContractValue())
            .average()
            .orElse(0.0);
    }

}
