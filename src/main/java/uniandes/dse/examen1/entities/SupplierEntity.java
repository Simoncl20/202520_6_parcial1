package uniandes.dse.examen1.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.Data;
import uk.co.jemos.podam.common.PodamExclude;
import java.util.ArrayList;
import java.util.List;
@Data
@Entity
public class SupplierEntity {

    @PodamExclude
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique code for the supplier
     */
    private String supplierCode;

    /**
     * The full name for the supplier
     */
    private String name;

    /**
     * The maximum number of contracts that the supplier can handle. If the number
     * is -1, it means that there is no limit.
     */
    private Integer capacity;

    @PodamExclude
    @ManyToMany(mappedBy = "providers", fetch = FetchType.LAZY)
    private List<FactoryEntity> clients = new ArrayList<>();

    /**
     * A list of all contracts that this supplier has with factories.
     */
    @PodamExclude
    @OneToMany(mappedBy = "provider", fetch = FetchType.LAZY)
    private List<ContractEntity> contracts = new ArrayList<>();
}
