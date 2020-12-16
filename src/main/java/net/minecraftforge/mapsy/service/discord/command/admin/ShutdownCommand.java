package net.minecraftforge.mapsy.service.discord.command.admin;

import net.minecraftforge.mapsy.service.discord.DiscordService;
import net.minecraftforge.mapsy.service.discord.command.AbstractCommand;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShutdownCommand extends AbstractCommand {

    public ShutdownCommand(DiscordService discord) {
        discord.registerCommand(literal("shutdown")
                .executes(src -> {
                    src.getSource().getChannel().sendMessage("Shutting down").complete();
                    discord.getDiscordConfig().shutdown();
                    return 0;
                })
        );
    }
}
