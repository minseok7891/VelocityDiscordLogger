package me.minseok.velocitydiscordlogger;

import com.velocitypowered.api.proxy.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;

public class DiscordMessageBuilder {

    private final PluginConfig config;

    public DiscordMessageBuilder(PluginConfig config) {
        this.config = config;
    }

    public MessageEmbed buildJoinEmbed(Player player) {
        String message = config.getJoinFormat()
                .replace("%username%", player.getUsername())
                .replace("%uuid%", player.getUniqueId().toString());

        String avatarUrl = config.getAvatarUrl()
                .replace("{uuid}", player.getUniqueId().toString())
                .replace("{username}", player.getUsername());

        return new EmbedBuilder()
                .setColor(parseColor(config.getJoinColor()))
                .setAuthor(message, null, avatarUrl)
                .build();
    }

    public MessageEmbed buildQuitEmbed(Player player) {
        String message = config.getQuitFormat()
                .replace("%username%", player.getUsername())
                .replace("%uuid%", player.getUniqueId().toString());

        String avatarUrl = config.getAvatarUrl()
                .replace("{uuid}", player.getUniqueId().toString())
                .replace("{username}", player.getUsername());

        return new EmbedBuilder()
                .setColor(parseColor(config.getQuitColor()))
                .setAuthor(message, null, avatarUrl)
                .build();
    }

    private Color parseColor(String hex) {
        try {
            return Color.decode(hex);
        } catch (Exception e) {
            return Color.WHITE;
        }
    }
}
