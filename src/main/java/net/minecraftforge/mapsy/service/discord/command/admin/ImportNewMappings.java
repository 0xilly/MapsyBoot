package net.minecraftforge.mapsy.service.discord.command.admin;

import net.minecraftforge.mapsy.repository.mapping.ClassNameRepo;
import net.minecraftforge.mapsy.service.discord.DiscordService;
import net.minecraftforge.mapsy.service.discord.command.BaseCommand;
import net.minecraftforge.mapsy.service.discord.command.CommandSource;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImportNewMappings extends BaseCommand {

    public ImportNewMappings(DiscordService discord, ClassNameRepo repo) {
       discord.registerCommand(literal("import").requires(CommandSource::isAdmin)
               .then(arguments("force", discordUserArgument()).executes(ctx -> {
                   int size = ctx.getSource().getAttachments().size();
                   if (size == 1) {
                       String name = ctx.getSource().getAttachments().get(1).getFileName();
                       if (name.endsWith(".zip")) {
                           //TODO process force update
                       } else {
                           ctx.getSource().getChannel().sendMessage("You may only upload zip config").complete();
                           return -2;
                       }
                      //TODO update the service
                       return 0;
                   } else {
                       ctx.getSource().getChannel().sendMessage("Only zip may be used").complete();
                       return -1;
                   }
               }))
               .executes(ctx -> {
                   int size = ctx.getSource().getAttachments().size();
                   if (size == 1) {
                       String name = ctx.getSource().getAttachments().get(1).getFileName();
                       if (name.endsWith(".zip")) {
                           //TODO process force update
                       } else {
                           ctx.getSource().getChannel().sendMessage("You may only upload zips").complete();
                           return -2;
                       }
                       //TODO update the service
                       return 0;
                   } else {
                       ctx.getSource().getChannel().sendMessage("Only zip may be used").complete();
                       return -1;
                   }
               }));
    }
}
