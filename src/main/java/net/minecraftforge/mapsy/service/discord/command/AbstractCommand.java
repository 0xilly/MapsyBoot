package net.minecraftforge.mapsy.service.discord.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractCommand {

    private static final Pattern USER_PATTERN = Pattern.compile("(?<=<@)((?>\\d)*)(?=>)");

    protected LiteralArgumentBuilder<CommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    protected <T> RequiredArgumentBuilder<CommandSource, T> arguments(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected ArgumentType<Long> discordUserArgument() {
        return reader -> {
            final int start = reader.getCursor();
            while (reader.canRead() && isAllowedInUser(reader.peek())) {
                reader.skip();
            }
            String str = reader.getString().substring(start, reader.getCursor());
            Matcher matcher = USER_PATTERN.matcher(str);
            if (!matcher.find()) {
                throw invalidUser().createWithContext(reader, str);
            }
            String match = matcher.group();
            return Long.parseLong(match);
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
