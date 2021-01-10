package net.minecraftforge.mapsy.service.discord.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseCommand {

    private static final Pattern USER_PATTERN = Pattern.compile("<@!?(\\d+)>");
    protected static final Pattern VERSION_PATTERN = Pattern.compile("^([\\d.]*)$");
    protected static final Pattern VERSION_REVISION_PATTERN = Pattern.compile("^([\\d.]*)(?:-([\\d]+))?$");

    protected LiteralArgumentBuilder<CommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    protected <T> RequiredArgumentBuilder<CommandSource, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected Optional<Pair<String, Integer>> parseVersionRevisionPair(String str) {
        if (str == null) return Optional.empty();

        Matcher matcher = VERSION_REVISION_PATTERN.matcher(str);
        if (!matcher.find()) return Optional.empty();
        String version = matcher.group(1);
        int revision = -1;
        if (matcher.groupCount() != 1 && matcher.group(2) != null) {
            revision = Integer.parseInt(matcher.group(2));
        }

        return Optional.of(Pair.of(version, revision));
    }

    protected Optional<String> parseVersion(String str) {
        if (str == null) return Optional.empty();

        Matcher matcher = VERSION_PATTERN.matcher(str);
        if (!matcher.find()) return Optional.empty();

        return Optional.of(matcher.group(1));
    }

    /**
     * Argument type matching against an @mention or the raw discord id.
     *
     * @return The ArgumentType.
     */
    protected ArgumentType<Long> discordUserArgument() {
        return reader -> {
            final int start = reader.getCursor();
            while (reader.canRead() && isAllowedInUser(reader.peek())) {
                reader.skip();
            }
            String str = reader.getString().substring(start, reader.getCursor());
            Matcher matcher = USER_PATTERN.matcher(str);
            if (!matcher.find()) {
                try {
                    return Long.parseLong(str);
                } catch (NumberFormatException e) {
                    throw invalidUser().createWithContext(reader, str);
                }
            }
            String match = matcher.group(1);
            return Long.parseLong(match);
        };
    }

    private boolean isAllowedInUser(char c) {
        return c >= '0' && c <= '9'//
                || c == '<' || c == '>'//
                || c == '@' || c == '!';
    }

    private static DynamicCommandExceptionType invalidUser() {
        return new DynamicCommandExceptionType(v -> new LiteralMessage("Unknown user string: " + v));
    }
}
