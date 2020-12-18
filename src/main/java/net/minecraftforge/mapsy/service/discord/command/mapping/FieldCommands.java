package net.minecraftforge.mapsy.service.discord.command.mapping;

import net.dv8tion.jda.api.EmbedBuilder;
import net.minecraftforge.mapsy.dao.FieldChange;
import net.minecraftforge.mapsy.repository.mapping.FieldNameRepo;
import net.minecraftforge.mapsy.repository.mapping.MinecraftVersionRepo;
import net.minecraftforge.mapsy.service.discord.DiscordService;
import net.minecraftforge.mapsy.service.discord.command.BaseCommand;
import net.minecraftforge.mapsy.service.discord.command.CommandSource;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FieldCommands extends BaseCommand {

    public FieldCommands(DiscordService discord, FieldNameRepo fieldName, FieldChange change, MinecraftVersionRepo versionRepo) {
        discord.registerCommand(literal("gf")
                .requires(CommandSource::notBanned)
                .then(arguments("field", discordUserArgument()).executes(ctx -> {
                    String field = ctx.getArgument("field", String.class);
                    versionRepo.findAll().forEach(v -> {
                        if (v.isLatest()) {
                            fieldName.findBySrgAndMinecraftVersion(field, v).ifPresentOrElse(f -> {
                                EmbedBuilder builder = new EmbedBuilder();
                                builder.setColor(0x0099ff);
                                builder.setTitle("Method");
                                builder.addField("OBF", f.getObf(), false);
                                builder.addField("SRG", f.getSrg(), false);
                                builder.addField("Comment", f.getDescription(), false);
                                ctx.getSource().getChannel().sendMessage(builder.build()).complete();
                            }, () -> {
                                ctx.getSource().getChannel().sendMessage("No field found").complete();
                            });
                        }
                    });
                    return 0;
                })));
    }
}
