package net.minecraftforge.mapsy.repository.mapping;

import net.minecraftforge.mapsy.dao.*;
import org.springframework.data.repository.CrudRepository;

import java.util.stream.Stream;

/**
 * Created by covers1624 on 14/12/20.
 */
public interface ParameterChangeRepo extends CrudRepository<ParameterChange, Long> {

    Stream<ParameterChange> findAllByUser(UserDAO user);

    Stream<ParameterChange> findAllByParameter(ParameterName parameter);

    Stream<ParameterChange> findAllByUserAndParameter(UserDAO user, ParameterName parameter);

}
