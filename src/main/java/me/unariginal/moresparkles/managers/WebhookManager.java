package me.unariginal.moresparkles.managers;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.receive.ReadonlyMessage;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.configs.MessagesConfig;
import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.placeholders.ParseContext;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

import static me.unariginal.moresparkles.configs.ConfigManager.CONFIG;
import static me.unariginal.moresparkles.configs.ConfigManager.MESSAGES;
import static me.unariginal.moresparkles.utils.TextUtils.parse;

public class WebhookManager {
    @Nullable
    public static WebhookClient webhook = null;

    public static void connectWebhook() {
        if (CONFIG.webhookSettings != null && CONFIG.webhookSettings.enabled) {
            webhook = WebhookClient.withUrl(CONFIG.webhookSettings.url);
        }
    }

    public static CompletableFuture<Long> sendWebhookEmbed(Boost boost) {
        if (webhook == null) return CompletableFuture.completedFuture(-1L);
        return webhook.send(buildWebhookEmbed(boost).build())
                .thenApply(ReadonlyMessage::getId)
                .exceptionally(e -> {
                    MoreSparkles.LOGGER.error("[MoreSparkles] Failed to send webhook", e);
                    return null;
                });
    }

    public static void deleteWebhook(long id) {
        if (webhook == null) return;
        webhook.delete(id).exceptionally(e -> {
            MoreSparkles.LOGGER.error("[MoreSparkles] Failed to delete webhook", e);
            return null;
        });
    }

    public static CompletableFuture<Long> editWebhookEmbed(long id, Boost boost) {
        if (webhook == null) return CompletableFuture.completedFuture(-1L);
        return webhook.edit(id, buildWebhookEmbed(boost).build())
                .thenApply(ReadonlyMessage::getId)
                .exceptionally(e -> {
                    MoreSparkles.LOGGER.error("[MoreSparkles] Failed to edit webhook", e);
                    return null;
                });
    }

    public static WebhookMessageBuilder buildWebhookEmbed(Boost boost) {
        if (MESSAGES.boostWebhooks == null) return new WebhookMessageBuilder();
        MessagesConfig.WebhookContentSettings webhookContentSettings = MESSAGES.boostWebhooks.get(boost.boostType);
        if (webhookContentSettings == null) return new WebhookMessageBuilder();

        ParseContext parseContext = ParseContext.builder().boost(boost).build();
        String username = parse(webhookContentSettings.username, parseContext);
        String message = parse(webhookContentSettings.message, parseContext);

        WebhookEmbedBuilder embedBuilder = new WebhookEmbedBuilder()
                .setColor(hexToRGB(webhookContentSettings.color))
                .setTitle(new WebhookEmbed.EmbedTitle(parse(webhookContentSettings.title, parseContext), null))
                .setAuthor(new WebhookEmbed.EmbedAuthor("", null, webhookContentSettings.thumbnailUrl));

        if (webhookContentSettings.fields != null) {
            for (MessagesConfig.WebhookContentSettings.EmbedFieldSettings embedFieldSettings : webhookContentSettings.fields) {
                embedBuilder.addField(new WebhookEmbed.EmbedField(embedFieldSettings.inline, parse(embedFieldSettings.name, parseContext), parse(embedFieldSettings.value, parseContext)));
            }
        }

        return new WebhookMessageBuilder()
                .setContent(message)
                .setUsername(username)
                .setAvatarUrl(webhookContentSettings.avatarUrl)
                .addEmbeds(embedBuilder.build());
    }

    private static int hexToRGB(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        int hexVal = Integer.parseInt(hex, 16);
        int r = (hexVal >> 16) & 0xFF;
        int g = (hexVal >> 8) & 0xFF;
        int b = (hexVal) & 0xFF;
        return new Color(r, g, b).getRGB();
    }
}
