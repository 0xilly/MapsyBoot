package net.minecraftforge.mapsy.service.discord.command.mapping;

import net.dv8tion.jda.api.EmbedBuilder;
import net.minecraftforge.mapsy.dao.MethodChange;
import net.minecraftforge.mapsy.repository.mapping.MethodNameRepo;
import net.minecraftforge.mapsy.repository.mapping.MinecraftVersionRepo;
import net.minecraftforge.mapsy.service.discord.DiscordService;
import net.minecraftforge.mapsy.service.discord.command.BaseCommand;
import net.minecraftforge.mapsy.service.discord.command.CommandSource;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MethodCommands extends BaseCommand {

    public MethodCommands(DiscordService discord, MethodNameRepo methodNameRepo, MethodChange change, MinecraftVersionRepo versionRepo) {
        discord.registerCommand(literal("gm")
                .requires(CommandSource::notBanned)
                .then(arguments("method", discordUserArgument()).executes(ctx -> {
                    String method = ctx.getArgument("method", String.class);
                    versionRepo.findAll().forEach(v-> {
                        if (v.isLatest()) {
                            methodNameRepo.findBySrgAndMinecraftVersion(method, v).ifPresentOrElse(m -> {
                                EmbedBuilder builder = new EmbedBuilder();
                                builder.setColor(0x0099ff);
                                builder.setTitle("Method");
                                builder.addField("OBF", m.getObf(), false);
                                builder.addField("SRG", m.getSrg(), false);
                                builder.addField("Descriptor", m.getDescriptor(), false);
                                builder.addField("Comment", m.getDescription(), false);
                                ctx.getSource().getChannel().sendMessage(builder.build()).complete();
                            }, () -> {
                                ctx.getSource().getChannel().sendMessage("No method found").complete();
                            });
                        }
                    });
                    return 0;
                })));
    }

}
