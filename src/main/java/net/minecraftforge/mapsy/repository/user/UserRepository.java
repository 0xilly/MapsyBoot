package net.minecraftforge.mapsy.repository.user;

import net.minecraftforge.mapsy.dao.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByDiscordId(long discordId);
}
