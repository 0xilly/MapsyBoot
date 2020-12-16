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
import org.springframework.stereotype.Service;

@Service
public class DiscordService extends ListenerAdapter {

    private final DiscordConfiguration discordConfig;
    private final CommandDispatcher<CommandSource> commandDispatcher = new CommandDispatcher<>();

    private final static Logger logger = LoggerFactory.getLogger(DiscordService.class);

    public DiscordService(DiscordConfiguration discordConfig) {
        this.discordConfig = discordConfig;

        if (discordConfig.available()) {
            discordConfig.registerListener(this);
            discordConfig.start();
        }
    }

    public void registerCommand(LiteralArgumentBuilder<CommandSource> literal) {
        if (discordConfig.available()) {
            commandDispatcher.register(literal);
        }
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent evt) {
        if (evt.getAuthor().isBot()) {
            return;
        }
        if (!evt.getMessage().getAttachments().isEmpty()) {
            return;
        }
        handleMessage(evt.getMessage(), evt.getAuthor(), evt.getChannel());
    }

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent evt) {
        if (evt.getAuthor().isBot()) {
            return;
        }
        if (!evt.getMessage().getAttachments().isEmpty()) {
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
            channel.sendMessage(e.getRawMessage().getString()).complete();
        } catch (Throwable t) {
            channel.sendMessage("Error executing command see log file").complete();
            logger.trace(t.getMessage());
        }
    }

    public DiscordConfiguration getDiscordConfig() {
        return discordConfig;
    }

    public CommandDispatcher<CommandSource> getCommandDispatcher() {
        return commandDispatcher;
    }
}
