package net.minecraftforge.mapsy.service.discord.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import javax.annotation.PostConstruct;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractCommand {

    @PostConstruct
    public abstract void init();

    protected LiteralArgumentBuilder<CommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    protected <T> RequiredArgumentBuilder<CommandSource, T> arguments(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected ArgumentType<Long> discordUserArgument() {
        var user_pattern = Pattern.compile("(?<=<@)((?>\\d)*)(?=>)");
        return new ArgumentType<Long>() {
            @Override
            public Long parse(StringReader reader) throws CommandSyntaxException {
                final int start = reader.getCursor();
                while (reader.canRead() && isAllowedInUser(reader.peek())) {
                    reader.skip();
                }
                String str = reader.getString().substring(start, reader.getCursor());
                Matcher matcher = user_pattern.matcher(str);
                if (!matcher.find()) {
                    throw invalidUser().createWithContext(reader, str);
                }
                String match = matcher.group();
                return Long.parseLong(match);
            }
        };
    }

    private boolean isAllowedInUser(char c) {
        return c >= '0' && c <= '9'//
                || c == '<' || c == '>'//
                || c == '@';
    }

    private static DynamicCommandExceptionType invalidUser() {
        return new DynamicCommandExceptionType(v -> new LiteralMessage("Unknown user string: " + v));
    }
}
