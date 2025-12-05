package me.minseok.velocitydiscordlogger;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;

public class ChatListener {

    private final JDA jda;
    private final PluginConfig config;

    private final Logger logger;
    private final Database database;

    private WebhookClient client;
    private String lastWebhookUrl;

    private final ProxyServer server;
    private final Object plugin;

    public ChatListener(JDA jda, PluginConfig config, Logger logger,
            Database database, ProxyServer server, Object plugin) {
        this.jda = jda;
        this.config = config;
        this.logger = logger;
        this.database = database;
        this.server = server;
        this.plugin = plugin;
        this.lastWebhookUrl = config.getWebhookUrl();
        if (!this.lastWebhookUrl.isEmpty()) {
            this.client = WebhookClient.withUrl(this.lastWebhookUrl);
        }
    }

    @SuppressWarnings("deprecation")
    @Subscribe(order = com.velocitypowered.api.event.PostOrder.LAST)
    public void onPlayerChat(PlayerChatEvent event) {
        logger.info("Chat event received: " + event.getMessage());
        logger.info("Event Result: " + event.getResult().toString());
        logger.info("Is Allowed: " + event.getResult().isAllowed());

        Player player = event.getPlayer();
        String message = event.getMessage();

        // Ignore commands
        if (message.startsWith("/")) {
            return;
        }

        server.getScheduler().buildTask(plugin, () -> {
            String webhookUrl = config.getWebhookUrl();
            if (webhookUrl.isEmpty()) {
                logger.warn("Webhook URL is empty!");
                return;
            }

            // Re-create client if URL changed
            if (!webhookUrl.equals(lastWebhookUrl)) {
                if (client != null) {
                    client.close();
                }
                client = WebhookClient.withUrl(webhookUrl);
                lastWebhookUrl = webhookUrl;
            }

            if (client == null) {
                client = WebhookClient.withUrl(webhookUrl);
                lastWebhookUrl = webhookUrl;
            }

            try {
                String discordId = database.getDiscordId(player.getUniqueId());
                String username = player.getUsername();
                String avatarUrl = config.getAvatarUrl()
                        .replace("{uuid}", player.getUniqueId().toString())
                        .replace("{username}", username);

                logger.info("Processing chat for player: " + player.getUsername() + " (UUID: " + player.getUniqueId()
                        + ")");
                logger.info("Linked Discord ID: " + discordId);

                if (discordId != null) {
                    try {
                        // Try to retrieve user (blocking is fine here as we are async)
                        User user = jda.retrieveUserById(discordId).complete();
                        if (user != null) {
                            username = user.getName();
                            avatarUrl = user.getAvatarUrl();
                            logger.info("Found Discord User: " + username);

                            // Try to get nickname from guild
                            String chatChannelId = config.getChatChannelId();
                            if (!chatChannelId.isEmpty()) {
                                net.dv8tion.jda.api.entities.channel.concrete.TextChannel channel = jda
                                        .getTextChannelById(chatChannelId);
                                if (channel != null) {
                                    net.dv8tion.jda.api.entities.Guild guild = channel.getGuild();
                                    try {
                                        net.dv8tion.jda.api.entities.Member member = guild.retrieveMember(user)
                                                .complete();
                                        if (member != null) {
                                            username = member.getEffectiveName();
                                            avatarUrl = member.getEffectiveAvatarUrl();
                                            logger.info("Found Guild Member. Using nickname: " + username);
                                        }
                                    } catch (Exception e) {
                                        logger.warn("Failed to retrieve member from guild: " + e.getMessage());
                                    }
                                } else {
                                    logger.warn("Chat channel not found: " + chatChannelId);
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to retrieve Discord user: " + e.getMessage());
                    }
                } else {
                    logger.info("No linked Discord account found.");
                }

                WebhookMessageBuilder builder = new WebhookMessageBuilder();
                builder.setUsername(username);
                builder.setAvatarUrl(avatarUrl);
                builder.setContent(message);

                client.send(builder.build()).thenAccept(readonlyMessage -> {
                    logger.info("Webhook message sent successfully. ID: " + readonlyMessage.getId());
                }).exceptionally(throwable -> {
                    logger.error("Failed to send webhook message (Async)", throwable);
                    return null;
                });
            } catch (Exception e) {
                logger.error("Failed to send webhook message", e);
            }
        }).schedule();
    }

    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
