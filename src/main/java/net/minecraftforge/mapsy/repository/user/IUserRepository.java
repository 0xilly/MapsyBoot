package net.minecraftforge.mapsy.repository.user;

import net.minecraftforge.mapsy.dao.User;
import org.springframework.data.jpa.repository.JpaRepository;

//@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
}
