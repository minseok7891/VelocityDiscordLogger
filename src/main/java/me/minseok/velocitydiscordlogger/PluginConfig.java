package me.minseok.velocitydiscordlogger;

import com.moandjiezana.toml.Toml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PluginConfig {

    private final Toml toml;

    public PluginConfig(Path configPath) throws IOException {
        String content = Files.readString(configPath);
        this.toml = new Toml().read(content);
    }

    public String getBotToken() {
        return toml.getString("bot_token", "");
    }

    public String getLogChannelId() {
        return toml.getString("channels.log", "");
    }

    public String getChatChannelId() {
        return toml.getString("channels.chat", "");
    }

    public String getDeathsChannelId() {
        return toml.getString("channels.deaths", "");
    }

    public String getAchievementsChannelId() {
        return toml.getString("channels.achievements", "");
    }

    public String getConsoleChannelId() {
        return toml.getString("channels.console", "");
    }

    public boolean isJoinEnabled() {
        return toml.getBoolean("messages.join.enabled", true);
    }

    public String getJoinColor() {
        return toml.getString("messages.join.color", "#00ff00");
    }

    public String getJoinFormat() {
        return toml.getString("messages.join.format", "%username% joined the network");
    }

    public boolean isQuitEnabled() {
        return toml.getBoolean("messages.quit.enabled", true);
    }

    public String getQuitColor() {
        return toml.getString("messages.quit.color", "#ff0000");
    }

    public String getQuitFormat() {
        return toml.getString("messages.quit.format", "%username% left the network");
    }

    public String getAvatarUrl() {
        return toml.getString("avatar.base_url", "https://visage.surgeplay.com/face/96/{uuid}");
    }
}
