package net.minecraftforge.mapsy.service.discord.command.admin;

import net.minecraftforge.mapsy.service.discord.DiscordService;
import net.minecraftforge.mapsy.service.discord.command.BaseCommand;
import net.minecraftforge.mapsy.service.discord.command.CommandSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile ("dev")
@Configuration
public class DevCommands extends BaseCommand {

    public DevCommands() {
    }

    @Autowired
    public void register(DiscordService discord) {
        discord.registerCommand(literal("shutdown")
                .requires(CommandSource::isAdmin)
                .executes(src -> {
                    src.getSource().getChannel().sendMessage("Shutting down").complete();
                    discord.shutdown();
                    return 0;
                })
        );
    }

}
