package net.minecraftforge.mapsy.service.discord.command.admin;

import net.dv8tion.jda.api.entities.User;
import net.minecraftforge.mapsy.dao.UserDAO;
import net.minecraftforge.mapsy.service.UserService;
import net.minecraftforge.mapsy.service.discord.DiscordService;
import net.minecraftforge.mapsy.service.discord.command.BaseCommand;
import net.minecraftforge.mapsy.service.discord.command.CommandSource;
import net.minecraftforge.mapsy.util.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * Created by covers1624 on 16/12/20.
 */
@Configuration
public class AdminManagement extends BaseCommand {

    private final UserService userService;

    public AdminManagement(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void register(DiscordService discord) {
        discord.registerCommand(literal("promote")
                .requires(CommandSource::isAdmin)
                .then(argument("user", discordUserArgument())
                        .then(literal("admin")
                                .executes(ctx -> {
                                    long id = ctx.getArgument("user", Long.class);
                                    User discordUser = discord.getJda().retrieveUserById(id).complete();
                                    UserDAO user = userService.getUserFromDiscord(discordUser);
                                    if (!userService.promoteUser(user, UserRole.ADMIN)) {
                                        ctx.getSource().getChannel().sendMessage("User is already an admin.").complete();
                                        return -1;
                                    }
                                    ctx.getSource().getChannel().sendMessage("User <@" + id + "> is now an admin.").complete();
                                    return 0;
                                })
                        )
                        .then(literal("trusted")
                                .executes(ctx -> {
                                    long id = ctx.getArgument("user", Long.class);
                                    User discordUser = discord.getJda().retrieveUserById(id).complete();
                                    UserDAO user = userService.getUserFromDiscord(discordUser);
                                    if (!userService.promoteUser(user, UserRole.ADMIN)) {
                                        ctx.getSource().getChannel().sendMessage("User is already a trusted user.").complete();
                                        return -1;
                                    }
                                    ctx.getSource().getChannel().sendMessage("User <@" + id + "> is now trusted.").complete();
                                    return 0;
                                })
                        )
                )
        );
        discord.registerCommand(literal("demote")
                .requires(CommandSource::isAdmin)
                .then(argument("user", discordUserArgument())
                        .executes(ctx -> {
                            long id = ctx.getArgument("user", Long.class);
                            User discordUser = discord.getJda().retrieveUserById(id).complete();
                            UserDAO user = userService.getUserFromDiscord(discordUser);
                            if (!userService.promoteUser(user, UserRole.USER)) {
                                ctx.getSource().getChannel().sendMessage("User is already a normal user.").complete();
                                return -1;
                            }
                            ctx.getSource().getChannel().sendMessage("User <@" + id + "> is now a normal user.").complete();
                            return 0;
                        })
                )
        );

        discord.registerCommand(literal("ban")
                .requires(CommandSource::isAdmin)
                .then(argument("user", discordUserArgument())
                        .executes(ctx -> {
                            long id = ctx.getArgument("user", Long.class);
                            User discordUser = discord.getJda().retrieveUserById(id).complete();
                            UserDAO user = userService.getUserFromDiscord(discordUser);
                            if (!userService.promoteUser(user, UserRole.BANNED)) {
                                ctx.getSource().getChannel().sendMessage("User is already banned.").complete();
                                return -1;
                            }
                            ctx.getSource().getChannel().sendMessage("User <@" + id + "> is now banned from making changes.").complete();
                            return 0;
                        })
                )
        );

        discord.registerCommand(literal("pardon")
                .requires(CommandSource::isAdmin)
                .then(argument("user", discordUserArgument())
                        .executes(ctx -> {
                            long id = ctx.getArgument("user", Long.class);
                            User discordUser = discord.getJda().retrieveUserById(id).complete();
                            UserDAO user = userService.getUserFromDiscord(discordUser);
                            if (!userService.promoteUser(user, UserRole.USER)) {
                                ctx.getSource().getChannel().sendMessage("User is not banned.").complete();
                                return -1;
                            }
                            ctx.getSource().getChannel().sendMessage("User <@" + id + "> is now banned from making changes.").complete();
                            return 0;
                        })
                )
        );

        discord.registerCommand(literal("revert")
                .requires(CommandSource::isAdmin)
                .then(argument("user", discordUserArgument())
                        .executes(ctx -> {
                            long id = ctx.getArgument("user", Long.class);
                            User discordUser = discord.getJda().retrieveUserById(id).complete();
                            UserDAO user = userService.getUserFromDiscord(discordUser);
                            userService.revertUserChanges(user);//TODO, provide a report of the un-applied changes.
                            ctx.getSource().getChannel().sendMessage("User <@" + id + ">'s changes have been reverted.").complete();
                            return 0;
                        })
                )
        );
    }
}
