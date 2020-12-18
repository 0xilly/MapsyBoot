package net.minecraftforge.mapsy.repository.mapping;

import net.minecraftforge.mapsy.dao.ClassName;
import net.minecraftforge.mapsy.dao.FieldName;
import net.minecraftforge.mapsy.dao.MinecraftVersion;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by covers1624 on 14/12/20.
 */
public interface FieldNameRepo extends CrudRepository<FieldName, Long> {

    Stream<FieldName> findAllByOwner(ClassName owner);

    Stream<FieldName> findAllByMinecraftVersion(MinecraftVersion version);

    Optional<FieldName> findByObfAndMinecraftVersion(String obf, MinecraftVersion version);

    Optional<FieldName> findByMojangAndMinecraftVersion(String mojang, MinecraftVersion version);

    Optional<FieldName> findBySrgAndMinecraftVersion(String srg, MinecraftVersion version);

    Optional<FieldName> findByMcpAndMinecraftVersion(String mcp, MinecraftVersion version);

}
