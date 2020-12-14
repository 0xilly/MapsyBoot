package net.minecraftforge.mapsy.service.discord.command.user;


import net.minecraftforge.mapsy.service.discord.command.CMDMeta;
import net.minecraftforge.mapsy.service.discord.command.CommandCtx;
import net.minecraftforge.mapsy.service.discord.command.ICommand;

@CMDMeta(name = "ping", description = "PONG!", usage = "type !ping", perm = CMDMeta.Permission.USER)
public class Ping implements ICommand {


    @Override
    public boolean command(CommandCtx ctx) {
        var channel = ctx.getChannel();
        channel.asTextChannel().ifPresent(s->s.sendMessage("PONG!"));
        return true;
    }
}
