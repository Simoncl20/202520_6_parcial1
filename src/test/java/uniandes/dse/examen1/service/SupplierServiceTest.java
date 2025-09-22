package uniandes.dse.examen1.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import uniandes.dse.examen1.entities.SupplierEntity;
import uniandes.dse.examen1.exceptions.RepeatedSupplierException;
import uniandes.dse.examen1.services.SupplierService;

@DataJpaTest
@Transactional
@Import(SupplierService.class)
public class SupplierServiceTest {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private TestEntityManager entityManager;

    private PodamFactory factory = new PodamFactoryImpl();

    @BeforeEach
    void setUp() {

    }

    @Test
    void testCreateSupplier() {
        SupplierEntity newEntity = factory.manufacturePojo(SupplierEntity.class);
        String supplierCode = newEntity.getSupplierCode();
        String name = newEntity.getName();
        Integer capacity = newEntity.getCapacity();

        try {
            SupplierEntity storedEntity = supplierService.createSupplier(newEntity);
            SupplierEntity retrieved = entityManager.find(SupplierEntity.class, storedEntity.getId());
            assertEquals(supplierCode, retrieved.getSupplierCode(), "The supplier code is incorrect");
            assertEquals(name, retrieved.getName(), "The name is incorrect");
            assertEquals(capacity, retrieved.getCapacity(), "The capacity is incorrect");
        } catch (RepeatedSupplierException e) {
            fail("No exception should be thrown: " + e.getMessage());
        }
    }

    @Test
    void testCreateRepeatedSupplier() {
        SupplierEntity firstEntity = factory.manufacturePojo(SupplierEntity.class);
        String supplierCode = firstEntity.getSupplierCode();

        SupplierEntity repeatedEntity = new SupplierEntity();
        repeatedEntity.setSupplierCode(supplierCode);
        repeatedEntity.setName("Different Name");
        repeatedEntity.setCapacity(10);

        try {
            supplierService.createSupplier(firstEntity);
            supplierService.createSupplier(repeatedEntity);
            fail("An exception must be thrown");
        } catch (Exception e) {
        }
    }
}
