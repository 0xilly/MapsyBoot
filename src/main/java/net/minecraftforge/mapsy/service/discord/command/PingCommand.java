package net.minecraftforge.mapsy.service.discord.command;

import net.minecraftforge.mapsy.service.discord.DiscordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PingCommand extends BaseCommand {

    public PingCommand() {
    }

    @Autowired
    public void register(DiscordService discord) {
        discord.registerCommand(literal("ping")
                .executes(src -> {
                    src.getSource().getChannel().sendMessage("Pong!").complete();
                    return 0;
                })
        );
    }
}
