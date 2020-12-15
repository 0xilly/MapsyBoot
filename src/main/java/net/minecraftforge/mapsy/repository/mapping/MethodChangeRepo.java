package net.minecraftforge.mapsy.repository.mapping;

import net.minecraftforge.mapsy.dao.MethodChange;
import net.minecraftforge.mapsy.dao.User;
import org.springframework.data.repository.CrudRepository;

import java.util.stream.Stream;

/**
 * Created by covers1624 on 14/12/20.
 */
public interface MethodChangeRepo extends CrudRepository<MethodChange, Long> {

    Stream<MethodChange> getAllByUser(User user);

    Stream<MethodChange> getAllByMethod(MethodChange method);

    Stream<MethodChange> getAllByUserAndMethod(User user, MethodChange method);

}
