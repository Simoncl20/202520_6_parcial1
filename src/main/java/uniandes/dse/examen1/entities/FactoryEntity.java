package uniandes.dse.examen1.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.Data;
import uk.co.jemos.podam.common.PodamExclude;

@Data
@Entity
public class FactoryEntity {

    @PodamExclude
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name for the factory. It should be unique.
     */
    private String name;

    /**
     * The location of the factory (e.g., "123 Main St, Springfield, USA").
     */
    private String location;

    /**
     * A list of the contracts that the factory has had with suppliers.
     */
    // TODO

    @PodamExclude
    @OneToMany(mappedBy = "factory", fetch = FetchType.LAZY)
    private List<ContractEntity> contracts = new ArrayList<>();

    /**
     * A list of all the suppliers that the factory has ever had. No supplier should
     * appear more than once in this list.
     */
    @PodamExclude
    @ManyToMany(fetch = FetchType.LAZY)
    private List<SupplierEntity> providers = new ArrayList<>();
}
