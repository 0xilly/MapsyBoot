/*
package net.minecraftforge.mapsy.service.mapping;

import net.minecraftforge.mapsy.dao.FieldMapping;
import net.minecraftforge.mapsy.repository.mapping.IFieldMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Optional;

//@Service
public class FieldMappingsService implements IFieldMappingService {

    @Autowired
    private IFieldMappingRepository service;

    @Override
    public Optional<FieldMapping> getById(long id) {
        return service.findById(id);
    }

    @Override
    public void add(FieldMapping add) {
        service.save(add);
    }

    @Override
    public void delete(long id) {
        service.deleteById(id);
    }

    @Override
    public Collection<FieldMapping> getAll() {
        return service.findAll();
    }
}
*/
