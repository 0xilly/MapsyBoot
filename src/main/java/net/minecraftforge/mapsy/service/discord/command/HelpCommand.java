package net.minecraftforge.mapsy.service.discord.command;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.minecraftforge.mapsy.service.discord.DiscordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Service
public class HelpCommand extends AbstractCommand {

    @Autowired
    private DiscordService discord;

    @Override
    public void init() {
        discord.registerCommand(literal("help").executes(src -> {
            var dispatcher = discord.getCommandDispatcher();
            Map<CommandNode<CommandSource>, String> map = dispatcher.getSmartUsage(dispatcher.getRoot(), src.getSource());

            var eb = new EmbedBuilder();
            MessageAction msg = src.getSource().getChannel().sendMessage("Help! :\n```");
            for (String s : map.values()) {
                msg.append("\n!").append(s);
            }
            msg.append("\n```");
            msg.complete();

            return map.size();
        }).then(arguments("command", StringArgumentType.greedyString()).executes(src -> {
            var dispatcher = discord.getCommandDispatcher();
            ParseResults<CommandSource> results = dispatcher.parse(StringArgumentType.getString(src, "command"), src.getSource());

            if (results.getContext().getNodes().isEmpty()) {
                var sender = src.getSource().getUser().getAsMention();
                src.getSource().getChannel().sendMessage(sender + " unknown command").complete();
                return -1;
            } else {
                var map = dispatcher.getSmartUsage(CollectionUtils.lastElement(results.getContext().getNodes()).getNode(), src.getSource());
                MessageAction msg = src.getSource().getChannel().sendMessage("Help! :\n```");
                for (String s : map.values()) {
                    msg.append("\n!").append(results.getReader().getString()).append(" ").append(s);
                }
                msg.append("\n```");
                msg.complete();
                return map.size();
            }
        })));
    }
}
