package net.minecraftforge.mapsy.service;

import net.dv8tion.jda.api.entities.User;
import net.minecraftforge.mapsy.configuration.DiscordConfiguration;
import net.minecraftforge.mapsy.dao.UserDAO;
import net.minecraftforge.mapsy.repository.user.UserRepository;
import net.minecraftforge.mapsy.util.UserRole;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * Created by covers1624 on 16/12/20.
 */
@Service
public class UserService {

    private final UserRepository userRepo;
    private final DiscordConfiguration discordConfig;

    public UserService(UserRepository userRepo, DiscordConfiguration discordConfig) {
        this.userRepo = userRepo;
        this.discordConfig = discordConfig;
    }

    public UserDAO getUserFromDiscord(User discordUser) {
        Optional<UserDAO> userOpt = userRepo.findByDiscordId(discordUser.getIdLong());
        UserDAO user = userOpt.orElseGet(() -> new UserDAO(discordUser.getIdLong()));

        if (discordConfig.getForcedAdmins().contains(discordUser.getIdLong())) {
            user.setRole(UserRole.ADMIN);
            user.setName(null);//Force re-save.
        }

        if (user.getName() == null || !Objects.equals(user.getName(), discordUser.getName())) {
            //TODO, does this change depending on where they are running commands from?
            user.setName(discordUser.getName());
            userRepo.save(user);
        }
        return user;
    }

    public boolean promoteUser(UserDAO user, UserRole to) {
        if (user.getRole().ordinal() == to.ordinal()) {
            return false;
        }
        user.setRole(to);
        userRepo.save(user);

        return true;
    }

    public void revertUserChanges(UserDAO user) {
        //TODO, Un-apply all this users changes.
    }

}
