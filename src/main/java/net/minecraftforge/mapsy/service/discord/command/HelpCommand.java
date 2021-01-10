package net.minecraftforge.mapsy.service.discord.command;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.minecraftforge.mapsy.service.discord.DiscordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Configuration
public class HelpCommand extends BaseCommand {

    public HelpCommand() {
    }

    @Autowired
    public void register(DiscordService discord) {
        discord.registerCommand(literal("help")
                .executes(src -> {
                    var dispatcher = discord.getCommandDispatcher();
                    Map<CommandNode<CommandSource>, String> map = dispatcher.getSmartUsage(dispatcher.getRoot(), src.getSource());

                    MessageAction msg = src.getSource().getChannel().sendMessage("Help! :\n```");
                    for (String s : map.values()) {
                        msg.append("\n!").append(s);
                    }
                    msg.append("\n```");
                    msg.complete();

                    return map.size();
                })
                .then(argument("command", StringArgumentType.greedyString())
                        .executes(src -> {
                            var dispatcher = discord.getCommandDispatcher();
                            ParseResults<CommandSource> results = dispatcher.parse(StringArgumentType.getString(src, "command"), src.getSource());

                            if (results.getContext().getNodes().isEmpty()) {
                                var sender = src.getSource().getDiscordUser().getAsMention();
                                src.getSource().getChannel().sendMessage(sender + " unknown command").complete();
                                return -1;
                            }

                            var map = dispatcher.getSmartUsage(CollectionUtils.lastElement(results.getContext().getNodes()).getNode(), src.getSource());
                            MessageAction msg = src.getSource().getChannel().sendMessage("Help! :\n```");
                            for (String s : map.values()) {
                                msg.append("\n!").append(results.getReader().getString()).append(" ").append(s);
                            }
                            msg.append("\n```");
                            msg.complete();
                            return map.size();
                        })
                )
        );
    }
}
