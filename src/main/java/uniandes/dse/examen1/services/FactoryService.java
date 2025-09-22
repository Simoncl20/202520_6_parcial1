package uniandes.dse.examen1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import uniandes.dse.examen1.entities.FactoryEntity;
import uniandes.dse.examen1.exceptions.RepeatedFactoryException;
import uniandes.dse.examen1.repositories.FactoryRepository;

@Slf4j
@Service
public class FactoryService {

    @Autowired
    FactoryRepository factoryRepository;

    public FactoryEntity createFactory(FactoryEntity newFactory) throws RepeatedFactoryException {
        if (factoryRepository.findByName(newFactory.getName()).isPresent()) {
            throw new RepeatedFactoryException("Factory with name '" + newFactory.getName() + "' already exists");
        }
        return factoryRepository.save(newFactory);
    }
}
