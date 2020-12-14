package net.minecraftforge.mapsy.repository.mapping;

import net.minecraftforge.mapsy.dao.ClassMapping;
import org.springframework.data.jpa.repository.JpaRepository;

//@Repository
public interface IClassMappingRepository extends JpaRepository<ClassMapping, Long> {
}
