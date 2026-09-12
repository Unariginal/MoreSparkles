package me.unariginal.moresparkles.utils;

import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.data.boostareas.BoostArea;
import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.placeholders.ParseContext;
import me.unariginal.moresparkles.placeholders.interfaces.BoostAreaPlaceholder;
import me.unariginal.moresparkles.placeholders.interfaces.BoostPlaceholder;
import me.unariginal.moresparkles.placeholders.interfaces.PlayerPlaceholder;
import me.unariginal.moresparkles.placeholders.interfaces.ServerPlaceholder;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.unariginal.moresparkles.placeholders.PlaceholderManager.*;

public class TextUtils {
    private static final Pattern pattern = Pattern.compile("%([^%]+)%");

    public static Text deserialize(String text) {
        return deserialize(text, ParseContext.builder().build());
    }

    public static Text deserialize(String text, ParseContext parseContext) {
        text = parse(text, parseContext);
        return MoreSparkles.INSTANCE.audiences.toNative(MiniMessage.miniMessage().deserialize(text));
    }

    public static String parse(String text, ParseContext parseContext) {
        text = parse(text);
        if (parseContext.getBoost() != null) text = parse(text, parseContext.getBoost());
        if (parseContext.getBoostArea() != null) text = parse(text, parseContext.getBoostArea());
        if (parseContext.getPlayer() != null) text = parse(text, parseContext.getPlayer());
        return text;
    }

    public static String parse(String text) {
        return parseHelper(text, (id, args) -> {
            for (ServerPlaceholder placeholder : serverPlaceholders) {
                if (placeholder.id().contains(id)) return placeholder.handle(args).string;
            }
            return null;
        });
    }

    public static String parse(String text, ServerPlayerEntity player) {
        return parseHelper(text, (id, args) -> {
            for (PlayerPlaceholder placeholder : playerPlaceholders) {
                if (placeholder.id().contains(id)) return placeholder.handle(player, args).string;
            }
            return null;
        });
    }

    public static String parse(String text, Boost boost) {
        return parseHelper(text, (id, args) -> {
            for (BoostPlaceholder placeholder : boostPlaceholders) {
                if (placeholder.id().contains(id)) return placeholder.handle(boost, args).string;
            }
            return null;
        });
    }

    public static String parse(String text, BoostArea boostArea) {
        return parseHelper(text, (id, args) -> {
            for (BoostAreaPlaceholder placeholder : boostAreaPlaceholders) {
                if (placeholder.id().contains(id)) return placeholder.handle(boostArea, args).string;
            }
            return null;
        });
    }

    private static String parseHelper(String text, BiFunction<String, List<String>, String> resolver) {
        Matcher matcher = pattern.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String content = matcher.group(1);
            String[] parts = content.split(" ");
            String id = parts[0];
            List<String> args = Arrays.asList(parts).subList(1, parts.length);

            String replacement = resolver.apply(id, args);
            if (replacement != null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
        }

        matcher.appendTail(result);
        return result.toString();
    }

    public static String hms(long rawTime) {
        long seconds = Math.max(rawTime, 0);

        long days = seconds / 86400; seconds %= 86400;
        long hours = seconds / 3600; seconds %= 3600;
        long minutes = seconds / 60; seconds %= 60;

        if (days > 0) {
            return String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }
}
