package me.minseok.velocitydiscordlogger;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class LinkCommand implements SimpleCommand {

    private static class LinkCode {
        final UUID uuid;
        final long timestamp;

        LinkCode(UUID uuid, long timestamp) {
            this.uuid = uuid;
            this.timestamp = timestamp;
        }
    }

    private final Map<String, LinkCode> linkCodes = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public LinkCommand() {
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        if (!(source instanceof Player)) {
            source.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return;
        }

        Player player = (Player) source;
        UUID uuid = player.getUniqueId();

        if (cooldowns.containsKey(uuid) && System.currentTimeMillis() < cooldowns.get(uuid)) {
            source.sendMessage(Component.text("Please wait before generating another code.", NamedTextColor.RED));
            return;
        }

        String code = generateCode();
        linkCodes.put(code, new LinkCode(uuid, System.currentTimeMillis()));
        cooldowns.put(uuid, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(60)); // 60s cooldown

        source.sendMessage(Component.text("Your linking code is: ", NamedTextColor.GREEN)
                .append(Component.text(code, NamedTextColor.YELLOW,
                        net.kyori.adventure.text.format.TextDecoration.BOLD)));
        source.sendMessage(Component.text("Type ", NamedTextColor.GREEN)
                .append(Component.text("/link " + code, NamedTextColor.AQUA))
                .append(Component.text(" in the Discord server to complete the linking.", NamedTextColor.GREEN)));
        source.sendMessage(Component.text("This code will expire in 5 minutes.", NamedTextColor.GRAY));
    }

    private String generateCode() {
        return String.format("%04d", random.nextInt(10000));
    }

    public UUID getPlayerByCode(String code) {
        LinkCode linkCode = linkCodes.remove(code);
        if (linkCode == null) {
            return null;
        }
        // Check expiration (5 minutes)
        if (System.currentTimeMillis() - linkCode.timestamp > TimeUnit.MINUTES.toMillis(5)) {
            return null;
        }
        return linkCode.uuid;
    }

    public void cleanup() {
        long now = System.currentTimeMillis();
        linkCodes.entrySet().removeIf(entry -> now - entry.getValue().timestamp > TimeUnit.MINUTES.toMillis(5));
    }
}
