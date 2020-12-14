package net.minecraftforge.mapsy.service.mapping;

import net.minecraftforge.mapsy.dao.MethodMapping;
import net.minecraftforge.mapsy.repository.mapping.IMethodMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Optional;

//@Service
public class MethodMappingService implements IMethodMappingService{

    @Autowired
    public IMethodMappingRepository service;

    @Override
    public Optional<MethodMapping> getById(long id) {
        return service.findById(id);
    }

    @Override
    public void add(MethodMapping add) {
        service.save(add);
    }

    @Override
    public void delete(long id) {
        service.deleteById(id);
    }

    @Override
    public Collection<MethodMapping> getAll() {
        return service.findAll();
    }
}
