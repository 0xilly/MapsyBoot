package net.minecraftforge.mapsy.service.mapping;

import net.minecraftforge.mapsy.dao.ClassMapping;
import net.minecraftforge.mapsy.repository.mapping.IClassMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Optional;

//@Service
public class ClassMappingService implements IClassMappingService {

    @Autowired
    private IClassMappingRepository service;

    @Override
    public Optional<ClassMapping> getById(long id) {
        return service.findById(id);
    }

    @Override
    public void add(ClassMapping add) {
        service.save(add);
    }

    @Override
    public void delete(long id) {
        service.deleteById(id);
    }

    @Override
    public Collection<ClassMapping> getAll() {
        return service.findAll();
    }
}
