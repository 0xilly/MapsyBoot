package net.minecraftforge.mapsy.service.discord.command.admin;

import net.minecraftforge.mapsy.repository.mapping.FieldNameRepo;
import net.minecraftforge.mapsy.repository.mapping.MethodNameRepo;
import net.minecraftforge.mapsy.repository.mapping.MinecraftVersionRepo;
import net.minecraftforge.mapsy.service.discord.DiscordService;
import net.minecraftforge.mapsy.service.discord.command.BaseCommand;
import net.minecraftforge.mapsy.service.discord.command.CommandSource;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LockNameCommand extends BaseCommand {

    public LockNameCommand(DiscordService discord, MethodNameRepo methodRepo, FieldNameRepo fieldRepo, MinecraftVersionRepo versionRepo) {
        discord.registerCommand(literal("lock")
                .requires(CommandSource::isAdmin)
                .then(arguments("srg", discordUserArgument()).executes(ctx -> {
                    //TODO add locking
                    return 0;
                })));
    }
}
