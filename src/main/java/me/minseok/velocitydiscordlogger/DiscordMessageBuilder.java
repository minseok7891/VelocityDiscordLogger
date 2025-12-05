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

        public MessageEmbed buildAchievementEmbed(String username, String uuid, String title, String description) {
                String avatarUrl = config.getAvatarUrl()
                                .replace("{uuid}", uuid)
                                .replace("{username}", username);

                return new EmbedBuilder()
                                .setColor(Color.decode("#ffd700")) // Gold color
                                .setAuthor(username + " 님이 " + title + " 발전 과제를 달성하셨습니다!", null, avatarUrl)
                                .setDescription(description)
                                .build();
        }

        public MessageEmbed buildDeathEmbed(String username, String uuid, String deathMessage) {
                String avatarUrl = config.getAvatarUrl()
                                .replace("{uuid}", uuid)
                                .replace("{username}", username);

                return new EmbedBuilder()
                                .setColor(Color.BLACK)
                                .setAuthor(deathMessage, null, avatarUrl)
                                .build();
        }

        public MessageEmbed buildServerStartEmbed() {
                return new EmbedBuilder()
                                .setColor(parseColor(config.getStartColor()))
                                .setTitle(config.getStartFormat())
                                .build();
        }

        public MessageEmbed buildServerStopEmbed() {
                return new EmbedBuilder()
                                .setColor(parseColor(config.getStopColor()))
                                .setTitle(config.getStopFormat())
                                .build();
        }

        private Color parseColor(String hex) {
                try {
                        return Color.decode(hex);
                } catch (Exception e) {
                        return Color.WHITE;
                }
        }

        public MessageEmbed buildChatEmbed(Player player, String message) {
                String avatarUrl = config.getAvatarUrl()
                                .replace("{uuid}", player.getUniqueId().toString())
                                .replace("{username}", player.getUsername());

                return new EmbedBuilder()
                                .setAuthor(player.getUsername(), null, avatarUrl)
                                .setDescription(message)
                                .setColor(Color.WHITE)
                                .setFooter(player.getCurrentServer().map(server -> server.getServerInfo().getName())
                                                .orElse("Unknown Server"))
                                .setTimestamp(java.time.Instant.now())
                                .build();
        }
}
