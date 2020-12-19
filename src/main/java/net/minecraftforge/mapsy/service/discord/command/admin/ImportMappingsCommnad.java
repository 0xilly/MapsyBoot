package net.minecraftforge.mapsy.service.discord.command.admin;

import net.dv8tion.jda.api.entities.Message;
import net.minecraftforge.mapsy.repository.mapping.MinecraftVersionRepo;
import net.minecraftforge.mapsy.service.ImportService;
import net.minecraftforge.mapsy.service.discord.DiscordService;
import net.minecraftforge.mapsy.service.discord.command.BaseCommand;
import net.minecraftforge.mapsy.service.discord.command.CommandSource;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Configuration
public class ImportMappingsCommnad extends BaseCommand {

    public ImportMappingsCommnad(DiscordService discord, ImportService importService, MinecraftVersionRepo versionRepo) {
       discord.registerCommand(literal("import").requires(CommandSource::isAdmin)
               .then(arguments("force", discordUserArgument()).executes(ctx -> {
                   int size = ctx.getSource().getAttachments().size();
                   if (size == 1) {
                       String name = ctx.getSource().getAttachments().get(0).getFileName();
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
                   List<Message.Attachment> attach = ctx.getSource().getAttachments();
                   int size = ctx.getSource().getAttachments().size();
                   if (size == 1) {
                       String name = ctx.getSource().getAttachments().get(0).getFileName();
                       if (name.endsWith(".zip")) {
                           Message.Attachment mcpConfig = attach.get(0);
                           try (var in = mcpConfig.retrieveInputStream().get()) {
                               importService.importMCPConfig(in, null);
                           } catch (InterruptedException | ExecutionException | IOException e) {
                               e.printStackTrace();
                           }
                           ctx.getSource().getChannel().sendMessage("Finished importing MCPConfig").complete();
                       } else {
                           ctx.getSource().getChannel().sendMessage("You may only upload zips").complete();
                           return -2;
                       }
                       return 0;
                   } else {
                       ctx.getSource().getChannel().sendMessage("Only zip may be used").complete();
                       return -1;
                   }
               }));

       discord.registerCommand(literal("importnames").requires(CommandSource::isAdmin)
               .executes(ctx-> {

                   List<Message.Attachment> attach = ctx.getSource().getAttachments();
                   int size = ctx.getSource().getAttachments().size();
                   if (size == 1) {
                       String name = ctx.getSource().getAttachments().get(0).getFileName();
                       if (name.endsWith(".zip")) {
                           Message.Attachment mcpConfig = attach.get(0);
                           try (var in = mcpConfig.retrieveInputStream().get()) {
                               var version = versionRepo.findByName("1.16.4");//TODO un hardcode this
                               importService.importMCPSnapshot(in, version.get());
                           } catch (InterruptedException | ExecutionException | IOException e) {
                               e.printStackTrace();
                           }
                           ctx.getSource().getChannel().sendMessage("Finished importing MCPNames").complete();
                       } else {
                           ctx.getSource().getChannel().sendMessage("You may only upload zips").complete();
                           return -2;
                       }
                       return 0;
                   } else {
                       ctx.getSource().getChannel().sendMessage("Only zip may be used").complete();
                       return -1;
                   }
               }));
    }
}
