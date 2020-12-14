package net.minecraftforge.mapsy.service.discord.command;

import net.minecraftforge.mapsy.service.discord.DiscordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PingCommand extends AbstractCommand {

    @Autowired
    private DiscordService discord;

    @Override
    public void init() {

        discord.registerCommand(literal("ping").executes(src-> {
            src.getSource().getChannel().sendMessage("Pong!").complete();
            return 0;
        }));

    }
}
