package me.minseok.velocitydiscordlogger;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DiscordCommand implements SimpleCommand {

    private final PluginConfig config;
    private final Database database;

    public DiscordCommand(PluginConfig config, Database database) {
        this.config = config;
        this.database = database;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            source.sendMessage(Component.text("Usage: /discord <reload|unlink|debug>", NamedTextColor.RED));
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                // Permission check removed for testing
                // if (!source.hasPermission("velocitydiscordlogger.reload")) {
                // source.sendMessage(
                // Component.text("You do not have permission to use this command.",
                // NamedTextColor.RED));
                // return;
                // }
                try {
                    config.reload();
                    source.sendMessage(Component.text("Configuration reloaded successfully!", NamedTextColor.GREEN));
                } catch (Exception e) {
                    source.sendMessage(
                            Component.text("Failed to reload configuration: " + e.getMessage(), NamedTextColor.RED));
                    e.printStackTrace();
                }
                break;

            case "unlink":
                if (!(source instanceof Player)) {
                    source.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
                    return;
                }
                Player player = (Player) source;
                UUID uuid = player.getUniqueId();

                CompletableFuture.runAsync(() -> {
                    database.unlinkAccount(uuid);
                    source.sendMessage(Component.text("Your account has been unlinked.", NamedTextColor.GREEN));
                });
                break;

            case "debug":
                if (!(source instanceof Player)) {
                    source.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
                    return;
                }
                if (!source.hasPermission("velocitydiscordlogger.debug")) {
                    source.sendMessage(Component.text("You do not have permission.", NamedTextColor.RED));
                    return;
                }
                Player p = (Player) source;
                source.sendMessage(Component.text("=== VelocityDiscordLogger Debug ===", NamedTextColor.GOLD));

                CompletableFuture.runAsync(() -> {
                    // 1. DB Check
                    String discordId = database.getDiscordId(p.getUniqueId());
                    source.sendMessage(
                            Component.text("Linked Discord ID: " + (discordId == null ? "Not Linked" : discordId),
                                    discordId == null ? NamedTextColor.RED : NamedTextColor.GREEN));

                    // 2. Config Check
                    String chatChannelId = config.getChatChannelId();
                    source.sendMessage(
                            Component.text("Chat Channel ID: " + (chatChannelId.isEmpty() ? "Not Set" : chatChannelId),
                                    chatChannelId.isEmpty() ? NamedTextColor.RED : NamedTextColor.GREEN));

                    // 3. JDA Check
                    if (discordId != null && !chatChannelId.isEmpty()) {
                        try {
                            net.dv8tion.jda.api.JDA jda = me.minseok.velocitydiscordlogger.VelocityDiscordLogger
                                    .getInstance().getJda();
                            if (jda != null) {
                                net.dv8tion.jda.api.entities.channel.concrete.TextChannel channel = jda
                                        .getTextChannelById(chatChannelId);
                                if (channel != null) {
                                    source.sendMessage(Component.text("Chat Channel Found: " + channel.getName(),
                                            NamedTextColor.GREEN));
                                    net.dv8tion.jda.api.entities.Guild guild = channel.getGuild();
                                    net.dv8tion.jda.api.entities.Member member = guild.retrieveMemberById(discordId)
                                            .complete();
                                    if (member != null) {
                                        source.sendMessage(
                                                Component.text("Guild Member Found: " + member.getEffectiveName(),
                                                        NamedTextColor.GREEN));
                                        source.sendMessage(Component.text(
                                                "Avatar URL: " + member.getEffectiveAvatarUrl(), NamedTextColor.GREEN));
                                    } else {
                                        source.sendMessage(
                                                Component.text("Guild Member NOT Found!", NamedTextColor.RED));
                                    }
                                } else {
                                    source.sendMessage(
                                            Component.text("Chat Channel NOT Found in JDA!", NamedTextColor.RED));
                                }
                            } else {
                                source.sendMessage(Component.text("JDA instance is null!", NamedTextColor.RED));
                            }
                        } catch (Exception e) {
                            source.sendMessage(
                                    Component.text("Error during JDA check: " + e.getMessage(), NamedTextColor.RED));
                            e.printStackTrace();
                        }
                    }
                });
                break;

            default:
                source.sendMessage(
                        Component.text("Unknown subcommand. Usage: /discord <reload|unlink|debug>",
                                NamedTextColor.RED));
                break;
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 0 || args.length == 1) {
            return List.of("reload", "unlink", "debug");
        }
        return List.of();
    }
}
