package net.minecraftforge.mapsy.service.discord.command.mapping;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.minecraftforge.mapsy.repository.mapping.FieldNameRepo;
import net.minecraftforge.mapsy.repository.mapping.MinecraftVersionRepo;
import net.minecraftforge.mapsy.service.MappingService;
import net.minecraftforge.mapsy.service.MappingService.Result;
import net.minecraftforge.mapsy.service.MappingService.UpdateResult;
import net.minecraftforge.mapsy.service.discord.DiscordService;
import net.minecraftforge.mapsy.service.discord.command.BaseCommand;
import net.minecraftforge.mapsy.service.discord.command.CommandSource;
import org.springframework.context.annotation.Configuration;

import java.awt.*;

@Configuration
public class FieldCommands extends BaseCommand {

    public FieldCommands(DiscordService discord, FieldNameRepo fieldName, MinecraftVersionRepo versionRepo, MappingService mappingService) {
        discord.registerCommand(literal("gf")
                .requires(CommandSource::notBanned)
                .then(arguments("field", StringArgumentType.word()).executes(ctx -> {
                    String field = ctx.getArgument("field", String.class);
                    versionRepo.findAll().forEach(v -> {
                        if (v.isLatest()) {
                            fieldName.findBySrgAndMinecraftVersion(field, v).ifPresentOrElse(f -> {

                                EmbedBuilder builder = new EmbedBuilder();
                                builder.setColor(Color.BLUE);
                                builder.setTitle("Field Mapping");
                                builder.addField("Name", String.format("`%s` -> `%s` -> `%s`", f.getObf(), f.getSrg(), f.getMcp()), false);
                                if (f.getDescription() != null) {

                                    builder.addField("Comment", f.getDescription(), false);
                                }
                                ctx.getSource().getChannel().sendMessage(builder.build()).complete();
                            }, () -> {
                                ctx.getSource().getChannel().sendMessage("No field found").complete();
                            });
                        }
                    });
                    return 0;
                })));

        discord.registerCommand(literal("sf")
                .requires(CommandSource::notBanned)
                .then(arguments("field", StringArgumentType.string()).
                        then(arguments("name", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String obfFName = ctx.getArgument("field", String.class);
                                    String newName = ctx.getArgument("name", String.class);
                                    UpdateResult result = mappingService.updateFieldMapping(obfFName, newName, null, ctx.getSource().getUser(), false);

                                    if (result.getResult() == Result.FAILURE) {
                                        ctx.getSource().getChannel().sendMessage(result.getCause()).complete();
                                        return -1;
                                    } else if (result.getResult() == Result.SUCCESS) {
                                        ctx.getSource().getChannel().sendMessage(result.getCause()).complete();
                                        return 0;
                                    } else {
                                        ctx.getSource().getChannel().sendMessage("wut!?").complete();
                                        return -2;
                                    }
                        }))
        ));
    }
}
