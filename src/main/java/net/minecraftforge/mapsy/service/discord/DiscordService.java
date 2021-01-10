package net.minecraftforge.mapsy.service.discord;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.minecraftforge.mapsy.configuration.DiscordConfiguration;
import net.minecraftforge.mapsy.service.UserService;
import net.minecraftforge.mapsy.service.discord.command.CommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.Objects;

@Service
public class DiscordService {

    private final static Logger logger = LogManager.getLogger();

    private final UserService userService;
    private final DiscordConfiguration discordConfig;
    private final CommandDispatcher<CommandSource> commandDispatcher = new CommandDispatcher<>();

    private JDA jda;

    public DiscordService(UserService userService, DiscordConfiguration discordConfig) {
        this.userService = userService;
        this.discordConfig = discordConfig;

        String token = discordConfig.getToken();
        if (StringUtils.hasText(token) && !Objects.equals(token, "token")) {
            try {
                jda = JDABuilder.createDefault(token).build();
                jda.setEventManager(new AnnotatedEventManager());
                jda.addEventListener(this);
                start();
            } catch (LoginException e) {
                logger.error("Failed to initialize Discord bot: ", e);
            }
        }
    }

    public void registerCommand(LiteralArgumentBuilder<CommandSource> literal) {
        commandDispatcher.register(literal);
    }

    public void start() {
        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            logger.error("Failed to start Discord bot: ", e);
        }
    }

    public void stop() {
        jda.shutdown();
    }

    public void emergencyStop() {
        jda.shutdownNow();
    }

    public void shutdown() {
        jda.shutdownNow();
        System.exit(2);
    }

    public void restart() {
        stop();
        start();
    }

    public JDA getJda() {
        return jda;
    }

    @SubscribeEvent
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent evt) {
        if (evt.getAuthor().isBot()) {
            return;
        }
        handleMessage(evt.getMessage(),evt.getAuthor(), evt.getMessage().getAttachments(), evt.getChannel());
    }

    @SubscribeEvent
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent evt) {
        if (evt.getAuthor().isBot()) {
            return;
        }
        handleMessage(evt.getMessage(),evt.getAuthor(), evt.getMessage().getAttachments(), evt.getChannel());
    }

    private void handleMessage(Message message, User user, List<Message.Attachment> attachments, MessageChannel channel) {
        var source = new CommandSource()
                .fromDiscordUser(user)
                .fromUser(userService.getUserFromDiscord(user))
                .in(channel)
                .insertAttachments(attachments);
        if (message.getContentRaw().isEmpty()) return;
        var reader = new StringReader(message.getContentRaw());
        logger.info(reader.getString());
        if (reader.canRead()) {
            if (reader.peek() == discordConfig.getCmdOp()) {
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
            logger.error(e);
        } catch (Throwable t) {
            channel.sendMessage("Error executing command see log file").complete();
            logger.error(t);
        }
    }

    public DiscordConfiguration getDiscordConfig() {
        return discordConfig;
    }

    public CommandDispatcher<CommandSource> getCommandDispatcher() {
        return commandDispatcher;
    }
}
