package net.minecraftforge.mapsy.repository.mapping;

import net.minecraftforge.mapsy.dao.MethodChange;
import net.minecraftforge.mapsy.dao.MethodName;
import net.minecraftforge.mapsy.dao.UserDAO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by covers1624 on 14/12/20.
 */
public interface MethodChangeRepo extends CrudRepository<MethodChange, Long> {

    Stream<MethodChange> findAllByUser(UserDAO user);

    Stream<MethodChange> findAllByMethod(MethodName method);

    List<MethodChange> getAllByMethod(MethodName method);

    Stream<MethodChange> findAllByUserAndMethod(UserDAO user, MethodName method);

}
