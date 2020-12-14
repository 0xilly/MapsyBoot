package net.minecraftforge.mapsy.service.discord.command.admin;

import net.minecraftforge.mapsy.service.discord.DiscordService;
import net.minecraftforge.mapsy.service.discord.command.AbstractCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShutdownCommand extends AbstractCommand {

    @Autowired
    private DiscordService discord;

    @Override
    public void init() {
        discord.registerCommand(literal("shutdown").executes(src-> {
            src.getSource().getChannel().sendMessage("Shutting down").complete();
            discord.getDiscordConfig().shutdown();
            return 0;
        }));
    }
}
