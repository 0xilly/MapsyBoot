package net.minecraftforge.mapsy.repository.mapping;

import net.minecraftforge.mapsy.dao.MinecraftVersion;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by covers1624 on 14/12/20.
 */
public interface MinecraftVersionRepo extends CrudRepository<MinecraftVersion, Long> {

    Optional<MinecraftVersion> findByLatestIsTrue();

    Optional<MinecraftVersion> findByNameAndRevision(String name, int revision);

    List<MinecraftVersion> findByNameOrderByRevision(String name);

    List<MinecraftVersion> findAllByRevision(int revision);

    @Transactional
    default Optional<MinecraftVersion> findLatestRevisionOf(String name) {
        List<MinecraftVersion> versions = findByNameOrderByRevision(name);
        if (versions.isEmpty()) return Optional.empty();
        return Optional.of(versions.get(versions.size() - 1));
    }

    default Optional<MinecraftVersion> findByVersionRevisionPair(Optional<Pair<String, Integer>> pairOpt) {
        if (pairOpt.isEmpty()) return Optional.empty();

        Pair<String, Integer> pair = pairOpt.get();

        String version = pair.getLeft();
        if (pair.getRight() == -1) {
            return findLatestRevisionOf(version);
        } else {
            return findByNameAndRevision(version, pair.getRight());
        }
    }
}
