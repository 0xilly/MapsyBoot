package net.minecraftforge.mapsy.repository.mapping;

import net.minecraftforge.mapsy.dao.*;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by covers1624 on 14/12/20.
 */
public interface ParameterNameRepo extends CrudRepository<ParameterName, Long> {

    Stream<ParameterName> getAllByOwner(MethodName owner);

    Stream<ParameterName> getAllByMinecraftVersion(MinecraftVersion version);

    Optional<ParameterName> findBySrgAndMinecraftVersion(String srg, MinecraftVersion version);

    Optional<ParameterName> findByMcpAndMinecraftVersion(String mcp, MinecraftVersion version);

}
