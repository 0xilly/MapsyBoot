package net.minecraftforge.mapsy.repository.mapping;

import net.minecraftforge.mapsy.dao.FieldChange;
import net.minecraftforge.mapsy.dao.FieldName;
import net.minecraftforge.mapsy.dao.User;
import org.springframework.data.repository.CrudRepository;

import java.util.stream.Stream;

/**
 * Created by covers1624 on 14/12/20.
 */
public interface FieldChangeRepo extends CrudRepository<FieldChange, Long> {

    Stream<FieldChange> getAllByUser(User user);

    Stream<FieldChange> getAllByField(FieldName field);

    Stream<FieldChange> getAllByUserAndField(User user, FieldName field);

}
