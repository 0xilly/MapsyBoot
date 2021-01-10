package net.minecraftforge.mapsy.service.discord.command.mapping;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.minecraftforge.mapsy.repository.mapping.MethodChangeRepo;
import net.minecraftforge.mapsy.repository.mapping.MethodNameRepo;
import net.minecraftforge.mapsy.repository.mapping.MinecraftVersionRepo;
import net.minecraftforge.mapsy.service.MappingService;
import net.minecraftforge.mapsy.service.MappingService.UpdateResult;
import net.minecraftforge.mapsy.service.discord.DiscordService;
import net.minecraftforge.mapsy.service.discord.command.BaseCommand;
import net.minecraftforge.mapsy.service.discord.command.CommandSource;
import org.springframework.context.annotation.Configuration;

import java.awt.*;

@Configuration
public class MethodCommands extends BaseCommand {

    public MethodCommands(DiscordService discord, MethodNameRepo methodNameRepo, MethodChangeRepo change, MappingService mappingService, MinecraftVersionRepo versionRepo) {
        discord.registerCommand(literal("gm")
                .requires(CommandSource::notBanned)
                .then(argument("method", StringArgumentType.greedyString()).executes(ctx -> {
                    String method = ctx.getArgument("method", String.class);
                    versionRepo.findAll().forEach(v-> {
                        if (v.isLatest()) {
                            methodNameRepo.findBySrgAndMinecraftVersion(method, v).ifPresentOrElse(m -> {
                                EmbedBuilder builder = new EmbedBuilder();
                                builder.setColor(Color.BLUE);
                                builder.addField("Name", String.format("`%s` -> `%s` -> `%s`", m.getObf(), m.getSrg(), m.getMcp()), false);
                                builder.addField("Descriptor", "`" +m.getSrgDescriptor() + "`", false);
                                if (m.getDescription() != null) {

                                    builder.addField("Comment", m.getDescription(), false);
                                }

                                ctx.getSource().getChannel().sendMessage(builder.build()).complete();
                            }, () -> {
                                ctx.getSource().getChannel().sendMessage("No method found").complete();
                            });
                        }
                    });
                    return 0;
                })));

        discord.registerCommand(literal("sm")
                .requires(CommandSource::notBanned)
                .then(argument("method", StringArgumentType.string())
                        .then(argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String obfName = ctx.getArgument("method", String.class);
                                    String newName = ctx.getArgument("name", String.class);
                                    UpdateResult result = mappingService.updateMethodMappings(obfName, newName, null, ctx.getSource().getUser(), false);

                                    if (result.getResult() == MappingService.Result.FAILURE) {
                                        ctx.getSource().getChannel().sendMessage(result.getCause()).complete();
                                        return -1;
                                    } else if (result.getResult() == MappingService.Result.SUCCESS) {
                                        ctx.getSource().getChannel().sendMessage(result.getCause()).complete();
                                        return 0;
                                    } else {
                                        ctx.getSource().getChannel().sendMessage("wut!?").complete();
                                        return -2;
                                    }
                                })
                        )

                ));
    }

}
