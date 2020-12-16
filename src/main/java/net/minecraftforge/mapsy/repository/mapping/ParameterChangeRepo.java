package net.minecraftforge.mapsy.repository.mapping;

import net.minecraftforge.mapsy.dao.*;
import org.springframework.data.repository.CrudRepository;

import java.util.stream.Stream;

/**
 * Created by covers1624 on 14/12/20.
 */
public interface ParameterChangeRepo extends CrudRepository<ParameterChange, Long> {

    Stream<ParameterChange> getAllByUser(User user);

    Stream<ParameterChange> getAllByParameter(ParameterName parameter);

    Stream<ParameterChange> getAllByUserAndParameter(User user, ParameterName parameter);

}
