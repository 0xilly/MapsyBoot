package net.minecraftforge.mapsy.service.discord.command;


import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public final class CommandCtx {

    private final DiscordApi api;
    private final Server     guild;
    private final User       author;
    private final Message    message;
    private final Channel    channel;
    private final String[]   args;

    public CommandCtx(DiscordApi api, Server guild, User author, Message message, Channel channel, String[] args) {
        this.api = api;
        this.guild = guild;
        this.author = author;
        this.message = message;
        this.channel = channel;
        this.args = args;
    }

    public DiscordApi getApi() {
        return api;
    }

    public Server getGuild() {
        return guild;
    }

    public User getAuthor() {
        return author;
    }

    public Message getMessage() {
        return message;
    }

    public Channel getChannel() {
        return channel;
    }

    public String[] getArgs() {
        return args;
    }

}
