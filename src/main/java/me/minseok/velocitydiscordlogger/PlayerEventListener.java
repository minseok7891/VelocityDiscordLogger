package me.minseok.velocitydiscordlogger;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;

public class PlayerEventListener {

    private final JDA jda;
    private final PluginConfig config;
    private final DiscordMessageBuilder messageBuilder;
    private final Logger logger;

    public PlayerEventListener(JDA jda, PluginConfig config, DiscordMessageBuilder messageBuilder, Logger logger) {
        this.jda = jda;
        this.config = config;
        this.messageBuilder = messageBuilder;
        this.logger = logger;
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerJoin(PlayerChooseInitialServerEvent event) {
        if (!config.isJoinEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        TextChannel channel = jda.getTextChannelById(config.getLogChannelId());

        if (channel == null) {
            logger.warn("Log channel not found: " + config.getLogChannelId());
            return;
        }

        try {
            channel.sendMessageEmbeds(
                    messageBuilder.buildJoinEmbed(player)).queue(
                            success -> logger.debug("Sent join message for " + player.getUsername()),
                            error -> logger.error("Failed to send join message", error));
        } catch (Exception e) {
            logger.error("Error sending join message", e);
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerQuit(DisconnectEvent event) {
        if (!config.isQuitEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        TextChannel channel = jda.getTextChannelById(config.getLogChannelId());

        if (channel == null) {
            logger.warn("Log channel not found: " + config.getLogChannelId());
            return;
        }

        try {
            channel.sendMessageEmbeds(
                    messageBuilder.buildQuitEmbed(player)).queue(
                            success -> logger.debug("Sent quit message for " + player.getUsername()),
                            error -> logger.error("Failed to send quit message", error));
        } catch (Exception e) {
            logger.error("Error sending quit message", e);
        }
    }
}
