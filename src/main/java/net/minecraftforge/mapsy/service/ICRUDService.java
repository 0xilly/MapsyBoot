package net.minecraftforge.mapsy.service;

import java.util.Collection;
import java.util.Optional;

public interface ICRUDService<T> {

    Optional<T> getById(long id);

    void add(T add);

    void delete(long id);

    Collection<T> getAll();
}
