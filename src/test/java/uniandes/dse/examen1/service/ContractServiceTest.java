package uniandes.dse.examen1.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import jakarta.transaction.Transactional;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;
import uniandes.dse.examen1.entities.ContractEntity;
import uniandes.dse.examen1.entities.SupplierEntity;
import uniandes.dse.examen1.entities.FactoryEntity;
import uniandes.dse.examen1.exceptions.RepeatedSupplierException;
import uniandes.dse.examen1.exceptions.InvalidContractException;
import uniandes.dse.examen1.exceptions.RepeatedFactoryException;
import uniandes.dse.examen1.repositories.SupplierRepository;
import uniandes.dse.examen1.repositories.FactoryRepository;
import uniandes.dse.examen1.services.SupplierService;
import uniandes.dse.examen1.services.FactoryService;
import uniandes.dse.examen1.services.ContractService;

@DataJpaTest
@Transactional
@Import({ ContractService.class, SupplierService.class, FactoryService.class })
public class ContractServiceTest {

    @Autowired
    private ContractService contractService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private FactoryService factoryService;

    @Autowired
    FactoryRepository factoryRepository;

    @Autowired
    SupplierRepository supplierRepository;

    @Autowired
    private TestEntityManager entityManager;

    private PodamFactory factory = new PodamFactoryImpl();

    private String name;
    private String supplierCode;
    private String unlimitedSupplierCode;

    @BeforeEach
    void setUp() throws RepeatedSupplierException, RepeatedFactoryException {
        SupplierEntity newSupplier = factory.manufacturePojo(SupplierEntity.class);
        newSupplier = supplierService.createSupplier(newSupplier);
        supplierCode = newSupplier.getSupplierCode();

        SupplierEntity unlimitedSupplier = factory.manufacturePojo(SupplierEntity.class);
        unlimitedSupplier.setCapacity(-1);
        unlimitedSupplier = supplierService.createSupplier(unlimitedSupplier);
        unlimitedSupplierCode = unlimitedSupplier.getSupplierCode();

        FactoryEntity newFactory = factory.manufacturePojo(FactoryEntity.class);
        newFactory = factoryService.createFactory(newFactory);
        name = newFactory.getName();
    }

    /**
     * Tests the normal creation of a contract for a factory with an existing
     * supplier
     *
     */
    @Test
    void testCreateContract() {
        try {
            Double contractValue = 1000.0;
            ContractEntity createdContract = contractService.createContract(name, supplierCode, contractValue);

            assertNotNull(createdContract, "Contract should be created");
            assertEquals(contractValue, createdContract.getContractValue(), "Contract value is incorrect");
            assertEquals(Boolean.TRUE, createdContract.getActive(), "Contract should be active");
            assertEquals(Integer.valueOf(0), createdContract.getSatisfaction(), "Satisfaction should be 0");

            ContractEntity retrieved = entityManager.find(ContractEntity.class, createdContract.getId());
            assertNotNull(retrieved, "Contract should be stored in database");
            assertEquals(name, retrieved.getFactory().getName(), "Factory name is incorrect");
            assertEquals(supplierCode, retrieved.getProvider().getSupplierCode(), "Supplier code is incorrect");

            FactoryEntity factory = factoryRepository.findByName(name).get();
            SupplierEntity supplier = supplierRepository.findBySupplierCode(supplierCode).get();
            assertTrue(factory.getProviders().contains(supplier), "Factory should contain supplier in providers list");
            assertTrue(supplier.getClients().contains(factory), "Supplier should contain factory in clients list");
        } catch (InvalidContractException e) {
            fail("No exception should be thrown: " + e.getMessage());
        }
    }

    /**
     * Tests the normal creation of multiple contract for a factory with the same
     * existing supplier
     */
    @Test
    void testCreateMultipleContract() {
        try {
            int numberOfContracts = 3;
            for (int i = 0; i < numberOfContracts; i++) {
                Double contractValue = 1000.0 + i * 100;
                ContractEntity createdContract = contractService.createContract(name, supplierCode, contractValue);
                assertNotNull(createdContract, "Contract " + i + " should be created");
                assertEquals(contractValue, createdContract.getContractValue(), "Contract value is incorrect for contract " + i);
            }

            SupplierEntity supplier = supplierRepository.findBySupplierCode(supplierCode).get();
            long activeContracts = supplier.getContracts().stream()
                .filter(c -> Boolean.TRUE.equals(c.getActive()))
                .count();
            assertEquals(numberOfContracts, activeContracts, "Should have " + numberOfContracts + " active contracts");
        } catch (InvalidContractException e) {
            fail("No exception should be thrown: " + e.getMessage());
        }
    }

    /**
     * Tests the creation of many contracts for a factory with the same
     * existing supplier with unlimited capacity
     */
    @Test
    void testCreateContractWithUnlimitedCapacity() {
        try {
            int numberOfContracts = 50;
            for (int i = 0; i < numberOfContracts; i++) {
                contractService.createContract(name, unlimitedSupplierCode, 1000.0);
            }
        } catch (InvalidContractException e) {
            fail("No exception should be thrown: " + e.getMessage());
        }
    }

    /**
     * Tests the creation of a contract for a factory but the supplier code is wrong
     */
    @Test
    void testCreateContractMissingSupplier() {
        String nonExistentSupplierCode = "NONEXISTENT";
        Double contractValue = 1000.0;

        assertThrows(InvalidContractException.class, () -> {
            contractService.createContract(name, nonExistentSupplierCode, contractValue);
        }, "Should throw InvalidContractException for non-existent supplier");
    }

    /**
     * Tests the creation of a contract for a factory with an existing
     * supplier, but the factory name is wrong
     */
    @Test
    void testCreateContractMissingFactory() {
        String nonExistentFactoryName = "Non Existent Factory";
        Double contractValue = 1000.0;

        assertThrows(InvalidContractException.class, () -> {
            contractService.createContract(nonExistentFactoryName, supplierCode, contractValue);
        }, "Should throw InvalidContractException for non-existent factory");
    }

    /**
     * Tests the creation of a contract for a factory with an existing
     * supplier, but the contract value is wrong
     */
    @Test
    void testCreateContractWrongValue() {
        assertThrows(InvalidContractException.class, () -> {
            contractService.createContract(name, supplierCode, -100.0);
        }, "Should throw InvalidContractException for negative contract value");

        assertThrows(InvalidContractException.class, () -> {
            contractService.createContract(name, supplierCode, 0.0);
        }, "Should throw InvalidContractException for zero contract value");
    }

    /**
     * Tests the creation of a contract for a factory with an existing
     * supplier, but the supplier has too many contracts (capacity exceeded)
     */
    @Test
    void testCreateContractCapacityExceeded() {
        try {
            SupplierEntity limitedSupplier = factory.manufacturePojo(SupplierEntity.class);
            limitedSupplier.setCapacity(2);
            limitedSupplier = supplierService.createSupplier(limitedSupplier);
            String limitedSupplierCode = limitedSupplier.getSupplierCode();
            contractService.createContract(name, limitedSupplierCode, 1000.0);
            contractService.createContract(name, limitedSupplierCode, 1100.0);

            assertThrows(InvalidContractException.class, () -> {
                contractService.createContract(name, limitedSupplierCode, 1200.0);
            }, "Should throw InvalidContractException when supplier capacity is exceeded");

        } catch (RepeatedSupplierException | InvalidContractException e) {
            fail("Setup should not fail: " + e.getMessage());
        }
    }

}
