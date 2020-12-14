package net.minecraftforge.mapsy.repository.mapping;

import net.minecraftforge.mapsy.dao.MinecraftVersion;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Created by covers1624 on 14/12/20.
 */
public interface MinecraftVersionRepo extends CrudRepository<MinecraftVersion, Long> {

    Optional<MinecraftVersion> findByName(String name);


}
