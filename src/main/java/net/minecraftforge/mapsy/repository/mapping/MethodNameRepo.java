package net.minecraftforge.mapsy.repository.mapping;

import net.minecraftforge.mapsy.dao.ClassName;
import net.minecraftforge.mapsy.dao.MethodName;
import net.minecraftforge.mapsy.dao.MinecraftVersion;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by covers1624 on 14/12/20.
 */
public interface MethodNameRepo extends CrudRepository<MethodName, Long> {

    Stream<MethodName> findAllByOwnersContains(ClassName owner);

    Stream<MethodName> findAllByMinecraftVersion(MinecraftVersion version);

    Optional<MethodName> findByObfAndMinecraftVersion(String obf, MinecraftVersion version);

    Optional<MethodName> findByMojangAndMinecraftVersion(String mojang, MinecraftVersion version);

    Optional<MethodName> findBySrgAndMinecraftVersion(String srg, MinecraftVersion version);

    Optional<MethodName> findByMcpAndMinecraftVersion(String mcp, MinecraftVersion version);

    Optional<MethodName> findByMcpAndDescriptorAndMinecraftVersion(String mcp, String desc, MinecraftVersion version);

}
