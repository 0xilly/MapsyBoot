package net.minecraftforge.mapsy.repository.mapping;

import net.minecraftforge.mapsy.dao.ClassName;
import net.minecraftforge.mapsy.dao.MinecraftVersion;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by covers1624 on 14/12/20.
 */
public interface ClassNameRepo extends CrudRepository<ClassName, Long> {

    Stream<ClassName> getAllByMinecraftVersion(MinecraftVersion version);

    Optional<ClassName> findByObfAndMinecraftVersion(String obf, MinecraftVersion version);

    Optional<ClassName> findByMojangAndMinecraftVersion(String mojang, MinecraftVersion version);

    Optional<ClassName> findBySrgAndMinecraftVersion(String srg, MinecraftVersion version);

}
