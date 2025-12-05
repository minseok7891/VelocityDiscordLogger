package me.minseok.velocitydiscordlogger;

import com.moandjiezana.toml.Toml;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PluginConfig {

    private Toml toml;
    private final Path configPath;

    public PluginConfig(Path configPath) throws IOException {
        this.configPath = configPath;
        reload();
    }

    public void reload() throws IOException {
        if (Files.exists(configPath)) {
            String content = Files.readString(configPath);
            this.toml = new Toml().read(content);
            loadTranslations();
        }
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

    public String getStatusChannelId() {
        return toml.getString("channels.status", "");
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

    public boolean isStartEnabled() {
        return toml.getBoolean("messages.start.enabled", true);
    }

    public String getStartColor() {
        return toml.getString("messages.start.color", "#00ff00");
    }

    public String getStartFormat() {
        return toml.getString("messages.start.format", "Server Started!");
    }

    public boolean isStopEnabled() {
        return toml.getBoolean("messages.stop.enabled", true);
    }

    public String getStopColor() {
        return toml.getString("messages.stop.color", "#ff0000");
    }

    public String getStopFormat() {
        return toml.getString("messages.stop.format", "Server Stopped!");
    }

    public String getAvatarUrl() {
        return toml.getString("avatar.base_url", "https://visage.surgeplay.com/face/96/{uuid}");
    }

    public List<String> getAllowedRoles() {
        return toml.getList("commands.allowed_roles", List.of("Developer", "Owner", "Admin"));
    }

    public String getWebhookUrl() {
        return toml.getString("linking.webhook_url", "");
    }

    public String getDatabaseHost() {
        return toml.getString("database.host", "localhost");
    }

    public int getDatabasePort() {
        return toml.getLong("database.port", 3306L).intValue();
    }

    public String getDatabaseName() {
        return toml.getString("database.database", "minecraft");
    }

    public String getDatabaseUsername() {
        return toml.getString("database.username", "root");
    }

    public String getDatabasePassword() {
        return toml.getString("database.password", "");
    }

    public String getDiscordToMinecraftFormat() {
        return toml.getString("messages.discord_to_mc_format", "<blue>[Discord] <white>%username%: %message%");
    }

    public String getLinkedRoleId() {
        return toml.getString("linking.linked_role_id", "");
    }

    private java.util.Map<String, String> translations = new java.util.HashMap<>();
    private java.util.Map<String, String> descriptionTranslations = new java.util.HashMap<>();

    private void loadTranslations() {
        try {
            Path translationFile = configPath.getParent().resolve("translations.json");
            if (!Files.exists(translationFile)) {
                try (java.io.InputStream in = getClass().getClassLoader().getResourceAsStream("translations.json")) {
                    if (in != null) {
                        Files.copy(in, translationFile);
                    }
                }
            }
            if (Files.exists(translationFile)) {
                Gson gson = new Gson();
                JsonObject json = gson.fromJson(Files.readString(translationFile), JsonObject.class);

                if (json.has("titles")) {
                    JsonObject titles = json.getAsJsonObject("titles");
                    for (String key : titles.keySet()) {
                        translations.put(key, titles.get(key).getAsString());
                    }
                }
                if (json.has("descriptions")) {
                    JsonObject descriptions = json.getAsJsonObject("descriptions");
                    for (String key : descriptions.keySet()) {
                        descriptionTranslations.put(key, descriptions.get(key).getAsString());
                    }
                }
                System.out.println("[VelocityDiscordLogger] Loaded " + translations.size() + " titles and "
                        + descriptionTranslations.size() + " descriptions from JSON.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getTranslatedTitle(String originalTitle) {
        String translated = translations.getOrDefault(originalTitle, originalTitle);
        if (!translated.equals(originalTitle)) {
            System.out.println(
                    "[VelocityDiscordLogger] Translated Title '" + originalTitle + "' to '" + translated + "'");
        } else {
            System.out.println("[VelocityDiscordLogger] No translation found for Title '" + originalTitle + "'");
        }
        return translated;
    }

    public String getTranslatedDescription(String originalDescription) {
        String translated = descriptionTranslations.getOrDefault(originalDescription, originalDescription);
        if (!translated.equals(originalDescription)) {
            System.out.println("[VelocityDiscordLogger] Translated Description '" + originalDescription + "' to '"
                    + translated + "'");
        } else {
            System.out.println(
                    "[VelocityDiscordLogger] No translation found for Description '" + originalDescription + "'");
        }
        return translated;
    }
}
