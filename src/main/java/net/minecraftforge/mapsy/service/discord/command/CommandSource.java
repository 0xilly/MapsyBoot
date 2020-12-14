package net.minecraftforge.mapsy.service.discord.command;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

public class CommandSource {

    private User user;
    private MessageChannel channel;

    public CommandSource from(User user) {
        this.user = user;
        return this;
    }

    public CommandSource in(MessageChannel channel) {
        this.channel = channel;
        return this;
    }


    public User getUser() {
        return user;
    }

    public MessageChannel getChannel() {
        return channel;
    }


    public boolean isDirect() {
        return channel instanceof PrivateChannel;
    }

}
