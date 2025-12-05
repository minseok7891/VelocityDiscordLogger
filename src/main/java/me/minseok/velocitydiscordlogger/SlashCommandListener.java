package me.minseok.velocitydiscordlogger;

import com.velocitypowered.api.proxy.ProxyServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class SlashCommandListener extends ListenerAdapter {

    private final ProxyServer server;
    private final PluginConfig config;
    private final Logger logger;
    private final Database database;
    private final LinkCommand linkCommand;
    private final Instant startTime;

    public SlashCommandListener(ProxyServer server, PluginConfig config, Logger logger, Database database,
            LinkCommand linkCommand) {
        this.server = server;
        this.config = config;
        this.logger = logger;
        this.database = database;
        this.linkCommand = linkCommand;
        this.startTime = Instant.now();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("cmd")) {
            handleCmdCommand(event);
        } else if (event.getName().equals("status")) {
            handleStatusCommand(event);
        } else if (event.getName().equals("link")) {
            handleLinkCommand(event);
        }
    }

    private void handleCmdCommand(SlashCommandInteractionEvent event) {
        // Permission check
        List<String> allowedRoles = config.getAllowedRoles();
        boolean hasPermission = event.getMember().getRoles().stream()
                .anyMatch(role -> allowedRoles.contains(role.getName()) || allowedRoles.contains(role.getId()));

        if (!hasPermission) {
            event.replyEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("❌ Permission Denied")
                    .setDescription("You do not have permission to use this command.")
                    .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String command = event.getOption("command").getAsString();
        logger.info("Executing command from Discord (" + event.getUser().getName() + "): " + command);

        server.getCommandManager().executeImmediatelyAsync(server.getConsoleCommandSource(), command);

        event.replyEmbeds(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("✅ Command Executed")
                .setDescription("Executed: `" + command + "`")
                .build())
                .setEphemeral(true)
                .queue();
    }

    private void handleStatusCommand(SlashCommandInteractionEvent event) {
        int onlinePlayers = server.getPlayerCount();
        int maxPlayers = server.getConfiguration().getShowMaxPlayers();
        Duration uptime = Duration.between(startTime, Instant.now());

        long days = uptime.toDays();
        long hours = uptime.toHoursPart();
        long minutes = uptime.toMinutesPart();

        String uptimeString = String.format("%dd %dh %dm", days, hours, minutes);

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.CYAN)
                .setTitle("📊 Proxy Server Status")
                .addField("Online Players", onlinePlayers + " / " + maxPlayers, true)
                .addField("Uptime", uptimeString, true)
                .addField("Velocity Version", server.getVersion().getVersion(), false)
                .setFooter("Requested by " + event.getUser().getName(), event.getUser().getAvatarUrl())
                .setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();
    }

    private void handleLinkCommand(SlashCommandInteractionEvent event) {
        String code = event.getOption("code").getAsString();
        java.util.UUID uuid = linkCommand.getPlayerByCode(code);

        if (uuid == null) {
            event.replyEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("❌ Invalid Code")
                    .setDescription("The code is invalid or has expired.")
                    .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String discordId = event.getUser().getId();
        database.saveLinkedAccount(uuid, discordId);

        // Assign Role if configured
        String roleId = config.getLinkedRoleId();
        if (!roleId.isEmpty()) {
            net.dv8tion.jda.api.entities.Role role = event.getGuild().getRoleById(roleId);
            if (role != null) {
                event.getGuild().addRoleToMember(event.getUser(), role).queue();
            }
        }

        event.replyEmbeds(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("✅ Account Linked")
                .setDescription("Your Minecraft account has been successfully linked!")
                .build())
                .setEphemeral(true)
                .queue();
    }
}
