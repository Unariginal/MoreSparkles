package me.unariginal.moresparkles.utils;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.ShinyChanceCalculationEvent;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import kotlin.Unit;
import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.data.SparkleArea;
import me.unariginal.moresparkles.data.ShinyBoost;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.concurrent.atomic.AtomicReference;

public class TextUtils {
    public static Text deserialize(String text) {
        return MoreSparkles.INSTANCE.getAudiences().toNative(MiniMessage.miniMessage().deserialize("<!i>" + text));
    }

    public static String parse(String text) {
        return text
                .replaceAll("%prefix%", MoreSparkles.INSTANCE.getMessages().prefix)
                .replaceAll("%base_shiny_rate%", String.valueOf(Cobblemon.config.getShinyRate()));
    }

    public static String parse(String text, ShinyBoost boost) {
        text = parse(text);
        if (boost.player_uuid != null) {
            ServerPlayerEntity player = MoreSparkles.INSTANCE.getServer().getPlayerManager().getPlayer(boost.player_uuid);
            if (player != null) {
                text = parse(text, player);
            }
        }
        return text
                .replaceAll("%multiplier%", String.valueOf(boost.multiplier))
                .replaceAll("%duration%", hms(boost.duration))
                .replaceAll("%time_remaining%", hms(boost.time_remaining / 20L));
    }

    public static String parse(String text, ServerPlayerEntity player) {
        text = parse(text);
        if (player != null) {
            AtomicReference<Float> shinyRate = new AtomicReference<>(Cobblemon.config.getShinyRate());
            CobblemonEvents.SHINY_CHANCE_CALCULATION.post(new ShinyChanceCalculationEvent[]{new ShinyChanceCalculationEvent(Cobblemon.config.getShinyRate(), new PokemonProperties().create(player))}, event -> {
                shinyRate.set(event.calculate(player));
                return Unit.INSTANCE;
            });
            return text
                    .replaceAll("%player.uuid%", player.getUuid().toString())
                    .replaceAll("%player.name%", player.getNameForScoreboard())
                    .replaceAll("%player.shiny_rate%", String.valueOf(shinyRate.get()));
        }
        return text;
    }

    public static String parse(String text, SparkleArea area) {
        text = parse(text);
        return text
                .replaceAll("%area%", area.id)
                .replaceAll("%multiplier%", String.valueOf(area.multiplier))
                .replaceAll("%area.type%", area.type);
    }

    public static String hms(long raw_time) {
        if (raw_time < 0) {
            raw_time = 0;
        }
        long days;
        long hours;
        long minutes;
        long seconds = raw_time;
        long temp;

        String output = "";

        days = seconds / 86400;
        seconds %= 86400;
        hours = seconds / 3600;
        seconds %= 3600;
        minutes = seconds / 60;
        seconds %= 60;

        if (days > 0) {
            output = output.concat(days + "d ");
        }
        if (hours > 0) {
            output = output.concat(hours + "h ");
        }
        if (minutes > 0) {
            output = output.concat(minutes + "m ");
        }
        output = output.concat(seconds + "s");

//        if (raw_time >= 86400) {
//            seconds = raw_time % 86400;
//            days = (raw_time - seconds) / 86400;
//            output = output.concat(days + "d ");
//        }
//        if (raw_time >= 3600) {
//            seconds = raw_time % 3600;
//            hours = (raw_time - seconds) / 3600;
//            output = output.concat(hours + "h ");
//        }
//        temp = seconds;
//        seconds = seconds % 60;
//        temp = temp - seconds;
//        minutes = temp / 60;
//        if (minutes > 0) {
//            output = output.concat(minutes + "m ");
//        }
//        output = output.concat(seconds + "s");

        return output;
    }
}
