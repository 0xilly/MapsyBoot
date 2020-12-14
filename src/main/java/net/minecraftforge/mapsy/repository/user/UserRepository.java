package net.minecraftforge.mapsy.repository.user;

import net.minecraftforge.mapsy.dao.User;
import org.springframework.data.jpa.repository.JpaRepository;

//@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
