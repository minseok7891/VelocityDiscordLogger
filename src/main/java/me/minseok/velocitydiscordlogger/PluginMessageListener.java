package me.minseok.velocitydiscordlogger;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;

public class PluginMessageListener {

    private static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier
            .from("velocitydiscordlogger:main");

    private final JDA jda;
    private final PluginConfig config;
    private final DiscordMessageBuilder messageBuilder;
    private final Logger logger;

    public PluginMessageListener(JDA jda, PluginConfig config, DiscordMessageBuilder messageBuilder, Logger logger) {
        this.jda = jda;
        this.config = config;
        this.messageBuilder = messageBuilder;
        this.logger = logger;
    }

    public static MinecraftChannelIdentifier getIdentifier() {
        return IDENTIFIER;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(IDENTIFIER)) {
            return;
        }

        if (!(event.getSource() instanceof ServerConnection)) {
            return;
        }

        ServerConnection serverConnection = (ServerConnection) event.getSource();
        Player player = serverConnection.getPlayer();

        // Read the message
        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String message = in.readUTF();

        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.get("type").getAsString();

            switch (type) {
                case "achievement":
                    handleAchievement(json);
                    break;
                case "death":
                    handleDeath(json);
                    break;
                default:
                    logger.warn("Unknown plugin message type: " + type);
            }
        } catch (Exception e) {
            logger.error("Failed to handle plugin message", e);
        }

        event.setResult(PluginMessageEvent.ForwardResult.handled());
    }

    private final java.util.Map<java.util.UUID, Long> achievementCooldowns = new java.util.concurrent.ConcurrentHashMap<>();

    private void handleAchievement(JsonObject json) {
        String username = json.get("username").getAsString();
        String uuidString = json.get("uuid").getAsString();
        java.util.UUID uuid = java.util.UUID.fromString(uuidString);
        String title = config.getTranslatedTitle(json.get("title").getAsString());
        String description = config.getTranslatedDescription(json.get("description").getAsString());

        // Deduplication: Ignore if received within 1 second
        long now = System.currentTimeMillis();
        if (achievementCooldowns.containsKey(uuid)) {
            long lastTime = achievementCooldowns.get(uuid);
            if (now - lastTime < 1000) {
                return;
            }
        }
        achievementCooldowns.put(uuid, now);

        TextChannel channel = jda.getTextChannelById(config.getAchievementsChannelId());
        if (channel == null) {
            logger.warn("Achievements channel not found: " + config.getAchievementsChannelId());
            return;
        }

        channel.sendMessageEmbeds(
                messageBuilder.buildAchievementEmbed(username, uuidString, title, description)).queue(
                        success -> logger.debug("Sent achievement message for " + username),
                        error -> logger.error("Failed to send achievement message", error));
    }

    private void handleDeath(JsonObject json) {
        String username = json.get("username").getAsString();
        String uuid = json.get("uuid").getAsString();
        String deathMessage = json.get("message").getAsString();

        TextChannel channel = jda.getTextChannelById(config.getDeathsChannelId());
        if (channel == null) {
            logger.warn("Deaths channel not found: " + config.getDeathsChannelId());
            return;
        }

        channel.sendMessageEmbeds(
                messageBuilder.buildDeathEmbed(username, uuid, deathMessage)).queue(
                        success -> logger.debug("Sent death message for " + username),
                        error -> logger.error("Failed to send death message", error));
    }
}
