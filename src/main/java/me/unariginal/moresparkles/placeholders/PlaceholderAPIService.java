package me.unariginal.moresparkles.placeholders;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.placeholders.interfaces.PlayerPlaceholder;
import me.unariginal.moresparkles.placeholders.interfaces.ServerPlaceholder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PlaceholderAPIService {
    private static PlaceholderResult safely(String id, Supplier<PlaceholderResult> body) {
        try {
            return body.get();
        } catch (Exception e) {
            MoreSparkles.LOGGER.error("[MoreSparkles] Placeholder '{}' failed to resolve", id, e);
            return PlaceholderResult.invalid("Error resolving placeholder");
        }
    }

    public void registerPlayer(PlayerPlaceholder placeholder) {
        placeholder.id().forEach(id -> Placeholders.register(Identifier.of(MoreSparkles.MOD_ID, id), (ctx, arg) -> safely(id, () -> {
            ServerPlayerEntity player = ctx.player();
            if (player == null) return PlaceholderResult.invalid("NO PLAYER");

            List<String> args = arg != null ? List.of(parse(arg, ParseContext.builder().player(player).build()).split(":")) : new ArrayList<>();
            GenericResult result = placeholder.handle(player, args);
            if (result.isSuccessful) {
                return PlaceholderResult.value(MoreSparkles.INSTANCE.audiences.toNative(result.asComponent()));
            } else {
                return PlaceholderResult.invalid(result.string);
            }
        })));
    }

    public void registerServer(ServerPlaceholder placeholder) {
        placeholder.id().forEach(id -> Placeholders.register(Identifier.of(MoreSparkles.MOD_ID, id), (ctx, arg) -> safely(id, () -> {
            List<String> args = arg != null ? List.of(parse(arg, ParseContext.builder().player(ctx.player()).build()).split(":")) : new ArrayList<>();
            GenericResult result = placeholder.handle(args);
            if (result.isSuccessful) {
                return PlaceholderResult.value(MoreSparkles.INSTANCE.audiences.toNative(result.asComponent()));
            } else {
                return PlaceholderResult.invalid(result.string);
            }
        })));
    }

    public String parse(String input, ParseContext parseContext) {
        PlaceholderContext context = PlaceholderContext.of(MoreSparkles.INSTANCE.server);
        if (parseContext.getPlayer() != null) context = PlaceholderContext.of(parseContext.getPlayer());
        return Placeholders.parseText(Text.literal(input), context).getString();
    }
}
