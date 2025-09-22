package uniandes.dse.examen1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import uniandes.dse.examen1.entities.ContractEntity;
import uniandes.dse.examen1.entities.FactoryEntity;
import uniandes.dse.examen1.entities.SupplierEntity;
import uniandes.dse.examen1.exceptions.InvalidContractException;
import uniandes.dse.examen1.repositories.SupplierRepository;
import uniandes.dse.examen1.repositories.FactoryRepository;
import uniandes.dse.examen1.repositories.ContractRepository;
import java.util.Optional;
import java.util.ArrayList;

@Slf4j
@Service
public class ContractService {

    @Autowired
    FactoryRepository factoryRepository;

    @Autowired
    SupplierRepository supplierRepository;

    @Autowired
    ContractRepository contractRepository;

    public ContractEntity createContract(String factoryName, String supplierCode, Double contractValue)
            throws InvalidContractException {

        if (contractValue <= 0) {
            throw new InvalidContractException("Contract value must be positive");
        }

        Optional<FactoryEntity> factoryOpt = factoryRepository.findByName(factoryName);
        if (factoryOpt.isEmpty()) {
            throw new InvalidContractException("Factory with name '" + factoryName + "' does not exist");
        }

        Optional<SupplierEntity> supplierOpt = supplierRepository.findBySupplierCode(supplierCode);
        if (supplierOpt.isEmpty()) {
            throw new InvalidContractException("Supplier with code '" + supplierCode + "' does not exist");
        }

        FactoryEntity factory = factoryOpt.get();
        SupplierEntity supplier = supplierOpt.get();

        long activeContracts = supplier.getContracts().stream()
            .filter(c -> Boolean.TRUE.equals(c.getActive()))
            .count();

        if (supplier.getCapacity() != -1 && activeContracts >= supplier.getCapacity()) {
            throw new InvalidContractException("Supplier has reached maximum capacity of active contracts");
        }

        ContractEntity contract = new ContractEntity();
        contract.setFactory(factory);
        contract.setProvider(supplier);
        contract.setContractValue(contractValue);
        contract.setActive(true);
        contract.setSatisfaction(0);

        ContractEntity savedContract = contractRepository.save(contract);

        if (!factory.getProviders().contains(supplier)) {
            factory.getProviders().add(supplier);
        }

        if (!supplier.getClients().contains(factory)) {
            supplier.getClients().add(factory);
        }

        if (!factory.getContracts().contains(savedContract)) {
            factory.getContracts().add(savedContract);
        }

        if (!supplier.getContracts().contains(savedContract)) {
            supplier.getContracts().add(savedContract);
        }

        factoryRepository.save(factory);
        supplierRepository.save(supplier);

        return savedContract;
    }
}
