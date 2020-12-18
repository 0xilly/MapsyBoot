package net.minecraftforge.mapsy.service.discord.command;

import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.minecraftforge.mapsy.dao.UserDAO;
import net.minecraftforge.mapsy.util.UserRole;

import java.util.List;

public class CommandSource {

    private User discordUser;
    private UserDAO user;
    private MessageChannel channel;
    private List<Attachment> attachments;

    public CommandSource fromDiscordUser(User discordUser) {
        this.discordUser = discordUser;
        return this;
    }

    public CommandSource fromUser(UserDAO user) {
        this.user = user;
        return this;
    }

    public CommandSource in(MessageChannel channel) {
        this.channel = channel;
        return this;
    }

    public CommandSource insertAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    public User getDiscordUser() {
        return discordUser;
    }

    public UserDAO getUser() {
        return user;
    }

    public boolean isBanned() {
        return user.getRole() == UserRole.BANNED;
    }

    public boolean notBanned() {
        return user.getRole() != UserRole.BANNED;
    }

    public boolean isTrusted() {
        return user.getRole() == UserRole.TRUSTED;
    }

    public boolean isAdmin() {
        return user.getRole() == UserRole.ADMIN;
    }

    public MessageChannel getChannel() {
        return channel;
    }

    public boolean isDirect() {
        return channel instanceof PrivateChannel;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public boolean hasAttachments() {
       return attachments.isEmpty();
    }
}
