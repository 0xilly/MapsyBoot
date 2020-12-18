package net.minecraftforge.mapsy.service.discord.command.admin;

import net.minecraftforge.mapsy.service.discord.DiscordService;
import net.minecraftforge.mapsy.service.discord.command.BaseCommand;
import net.minecraftforge.mapsy.service.discord.command.CommandSource;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShutdownCommand extends BaseCommand {

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
