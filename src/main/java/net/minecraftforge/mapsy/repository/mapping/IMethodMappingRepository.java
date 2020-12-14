package net.minecraftforge.mapsy.repository.mapping;

import net.minecraftforge.mapsy.dao.MethodMapping;
import org.springframework.data.jpa.repository.JpaRepository;

//@Repository
public interface IMethodMappingRepository extends JpaRepository<MethodMapping, Long> {
}
