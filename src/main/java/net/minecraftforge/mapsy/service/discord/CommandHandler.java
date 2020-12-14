package net.minecraftforge.mapsy.service.discord;

import net.minecraftforge.mapsy.service.discord.command.CMDMeta;
import net.minecraftforge.mapsy.service.discord.command.CommandCtx;
import net.minecraftforge.mapsy.service.discord.command.ICommand;
import net.minecraftforge.mapsy.service.discord.command.user.Ping;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.HashMap;

//TODO
/**
 * I could probably make this an @Service and then use a lambada something along the lines of
 *
 * @AutoWired
 * DiscordConfiguratiog
 */
public class CommandHandler implements MessageCreateListener {

    private HashMap<String, ICommand> commandRegistry;


    public CommandHandler() {
        commandRegistry = new HashMap<>();
        try {
            registerCommand(new Ping());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onMessageCreate(MessageCreateEvent evt) {
        var msg = evt.getMessageContent();
        var channel = evt.getChannel().asTextChannel();
        if (msg.startsWith("!") && !evt.getMessageAuthor().isBotUser()) {
            var firstWord = msg.split(" ");
            var cmdName= firstWord[0].replace("!", "");
            var ctx = new CommandCtx(evt.getApi(), evt.getServer().get(),evt.getMessageAuthor().asUser().get(), evt.getMessage(), evt.getChannel(), msg.split(" "));
            execute(cmdName, ctx);
        }
    }

    private void execute(String name, CommandCtx ctx) {
        if (commandRegistry.containsKey(name)) {
            var cmd = commandRegistry.get(name);
            var author = ctx.getAuthor();
            //Grab the commands permission level
            var cmdPermission =  cmdPermResolver(cmd);
            cmd.command(ctx);
        }
//        if ((cmdPermission == Permission.MOD && userPermResolver(ctx) == Permission.MOD) ||
//                (cmdPermission == Permission.MOD && userPermResolver(ctx) == Permission.ADMIN)) {
//            cmd.command(ctx);
//        } else {
//            ctx.getMessage().delete();
//            ctx.getChannel().asTextChannel().ifPresent(s-> s.sendMessage(author.getNicknameMentionTag() + " You don't have the" +
//                    "correct persimmon level to execute that command!"));
//        }
//
//        if (cmdPermission == Permission.ADMIN && userPermResolver(ctx) == Permission.ADMIN) {
//            cmd.command(ctx);
//        } else {
//            ctx.getMessage().delete();
//            ctx.getChannel().asTextChannel().ifPresent(s-> s.sendMessage(author.getNicknameMentionTag() + " You don't have the" +
//                    "correct persimmon level to execute that command!"));
//        }
//
//        if (cmdPermission == Permission.USER) {
//            cmd.command(ctx);
//        }
    }

    private void registerCommand(ICommand command) throws Exception {
        if (command.getClass().isAnnotationPresent(CMDMeta.class)) {
            var cmdName = command.getClass().getAnnotation(CMDMeta.class).name();
            commandRegistry.put(cmdName, command);
        } else {
            throw new Exception("No @CMDMeta annotation found on " + command.getClass().getName());
        }
    }

    private CMDMeta.Permission cmdPermResolver(ICommand cmd) {
        return cmd.getClass().getAnnotation(CMDMeta.class).perm();
    }
}
