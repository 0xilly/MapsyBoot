package net.minecraftforge.mapsy.service.discord.command.admin;

import net.minecraftforge.mapsy.service.discord.DiscordService;
import net.minecraftforge.mapsy.service.discord.command.AbstractCommand;
import net.minecraftforge.mapsy.service.discord.command.CommandSource;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShutdownCommand extends AbstractCommand {

    public ShutdownCommand(DiscordService discord) {
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
