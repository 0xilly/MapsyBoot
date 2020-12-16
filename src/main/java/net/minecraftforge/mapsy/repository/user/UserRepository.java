package net.minecraftforge.mapsy.repository.user;

import net.minecraftforge.mapsy.dao.UserDAO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserDAO, Long> {

    Optional<UserDAO> findByDiscordId(long discordId);
}
