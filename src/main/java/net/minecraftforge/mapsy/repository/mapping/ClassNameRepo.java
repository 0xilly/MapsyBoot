package net.minecraftforge.mapsy.repository.mapping;

import net.minecraftforge.mapsy.dao.ClassName;
import net.minecraftforge.mapsy.dao.MinecraftVersion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by covers1624 on 14/12/20.
 */
public interface ClassNameRepo extends CrudRepository<ClassName, Long> {

    Stream<ClassName> findAllByMinecraftVersion(MinecraftVersion version);

    @Transactional
    default Map<String, ClassName> loadAllByMinecraftVersion(MinecraftVersion version) {
        return findAllByMinecraftVersion(version)
                .collect(Collectors.toMap(ClassName::getSrg, e -> e));
    }

    Optional<ClassName> findByObfAndMinecraftVersion(String obf, MinecraftVersion version);

    Optional<ClassName> findByMojangAndMinecraftVersion(String mojang, MinecraftVersion version);

    Optional<ClassName> findBySrgAndMinecraftVersion(String srg, MinecraftVersion version);

}
