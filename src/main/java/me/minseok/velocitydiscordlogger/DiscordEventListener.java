package me.minseok.velocitydiscordlogger;

import com.velocitypowered.api.proxy.ProxyServer;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.Component;

import org.slf4j.Logger;

public class DiscordEventListener extends ListenerAdapter {

    private final ProxyServer server;
    private final PluginConfig config;
    private final Logger logger;
    private final Database database;
    private final LinkCommand linkCommand;

    public DiscordEventListener(ProxyServer server, PluginConfig config, Logger logger, Database database,
            LinkCommand linkCommand) {
        this.server = server;
        this.config = config;
        this.logger = logger;
        this.database = database;
        this.linkCommand = linkCommand;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Ignore bots (including self)
        if (event.getAuthor().isBot()) {
            return;
        }

        String message = event.getMessage().getContentDisplay();
        boolean isPrivate = event.getChannelType() == net.dv8tion.jda.api.entities.channel.ChannelType.PRIVATE;

        // Check for 4-digit linking code (Works in DM or Chat Channel)
        if (message.matches("\\d{4}")) {
            // If in chat channel, verify channel ID
            if (!isPrivate && !event.getChannel().getId().equals(config.getChatChannelId())) {
                return; // Ignore random numbers in other channels
            }

            java.util.UUID uuid = linkCommand.getPlayerByCode(message);
            if (uuid != null) {
                String discordId = event.getAuthor().getId();
                logger.info("Linking account... UUID: " + uuid + ", Discord ID: " + discordId);
                database.saveLinkedAccount(uuid, discordId);
                logger.info("Account linked successfully in database.");

                // Assign Role if configured
                String roleId = config.getLinkedRoleId();
                if (!roleId.isEmpty()) {
                    // For DMs, we need to find the mutual guild
                    net.dv8tion.jda.api.entities.Guild guild = null;
                    if (isPrivate) {
                        // Find a mutual guild where the bot has permission
                        for (net.dv8tion.jda.api.entities.Guild g : event.getJDA().getGuilds()) {
                            if (g.getMemberById(discordId) != null) {
                                guild = g;
                                break;
                            }
                        }
                    } else {
                        guild = event.getGuild();
                    }

                    if (guild != null) {
                        net.dv8tion.jda.api.entities.Role role = guild.getRoleById(roleId);
                        if (role != null) {
                            net.dv8tion.jda.api.entities.Member member = guild.getMemberById(discordId);
                            if (member != null) {
                                guild.addRoleToMember(member, role).queue();
                            }
                        }
                    }
                }

                event.getMessage().reply("✅ Account Linked Successfully!").queue();
                return; // Stop processing
            }
        }

        // Chat Bridge Logic (Only for configured chat channel, ignore DMs)
        if (isPrivate) {
            return;
        }

        String chatChannelId = config.getChatChannelId();
        if (!event.getChannel().getId().equals(chatChannelId)) {
            return;
        }

        String username = event.getAuthor().getName(); // Or getEffectiveName()

        // Handle attachments if message is empty
        if (message.isEmpty()) {
            if (!event.getMessage().getAttachments().isEmpty()) {
                message = "[Attachment]";
            } else if (!event.getMessage().getStickers().isEmpty()) {
                message = "[Sticker]";
            } else {
                logger.warn("Received empty message from " + username
                        + ". Ensure MESSAGE_CONTENT intent is enabled in Discord Developer Portal!");
                return;
            }
        }

        logger.info("Discord message received from " + username + ": " + message);

        String format = config.getDiscordToMinecraftFormat();
        String formattedMessage = format
                .replace("%username%", username)
                .replace("%message%", message);

        logger.info("Formatted message: " + formattedMessage);

        Component component = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                .deserialize(formattedMessage);
        server.sendMessage(component);
    }
}
