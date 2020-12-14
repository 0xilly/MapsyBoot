package net.minecraftforge.mapsy.service.discord;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraftforge.mapsy.configuration.DiscordConfiguration;
import net.minecraftforge.mapsy.service.discord.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class DiscordService extends ListenerAdapter {

    @Autowired
    private DiscordConfiguration discordConfig;
    private CommandDispatcher<CommandSource> commandDispatcher;

    private final static Logger logger = LoggerFactory.getLogger(DiscordService.class);

    @PostConstruct
    private void discordServiceInit() {
       commandDispatcher = new CommandDispatcher<>();
       discordConfig.registerListener(this);
       discordConfig.start();
    }

    public void registerCommand(LiteralArgumentBuilder<CommandSource> literal) {
        commandDispatcher.register(literal);
    }


    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent evt) {
        if (evt.getAuthor().isBot()) {
            return;
        }
        handleMessage(evt.getMessage(), evt.getAuthor(), evt.getChannel());
    }

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent evt) {
        if (evt.getAuthor().isBot()) {
            return;
        }
        handleMessage(evt.getMessage(), evt.getAuthor(), evt.getChannel());
    }

    private void handleMessage(Message message, User user, MessageChannel channel) {
        var source = new CommandSource().from(user).in(channel);
        var reader = new StringReader(message.getContentRaw());
        logger.info(reader.getString());
        if (reader.canRead()) {
            if (reader.peek() == '!') {
                reader.skip();
            } else {
                if (!source.isDirect()) {
                    return;
                }
            }
        }
        try {
           commandDispatcher.execute(reader, source);
        } catch (CommandSyntaxException e) {
            channel.sendMessage(e.getRawMessage().getString() + " `" + reader.getString() +"`").complete();
        } catch (Throwable t) {
            channel.sendMessage("Error executing command see log file").complete();
            logger.trace(t.getMessage());
        }
    }

    public DiscordConfiguration getDiscordConfig() {
        return discordConfig;
    }
}
