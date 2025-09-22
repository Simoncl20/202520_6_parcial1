package uniandes.dse.examen1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import uniandes.dse.examen1.entities.SupplierEntity;
import uniandes.dse.examen1.exceptions.RepeatedSupplierException;
import uniandes.dse.examen1.repositories.SupplierRepository;

@Slf4j
@Service
public class SupplierService {

    @Autowired
    SupplierRepository supplierRepository;

    public SupplierEntity createSupplier(SupplierEntity newSupplier) throws RepeatedSupplierException {
        if (supplierRepository.findBySupplierCode(newSupplier.getSupplierCode()).isPresent()) {
            throw new RepeatedSupplierException("Supplier with code '" + newSupplier.getSupplierCode() + "' already exists");
        }
        return supplierRepository.save(newSupplier);
    }
}
