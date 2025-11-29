package me.minseok.velocitydiscordlogger;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(id = "velocitydiscordlogger", name = "VelocityDiscordLogger", version = "1.0.0", description = "Network-level Discord logging with embeds and player icons", authors = {
        "minseok" })
public class VelocityDiscordLogger {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private PluginConfig config;
    private JDA jda;
    private DiscordMessageBuilder messageBuilder;

    @Inject
    public VelocityDiscordLogger(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("VelocityDiscordLogger starting...");

        // Load configuration
        try {
            loadConfig();
        } catch (IOException e) {
            logger.error("Failed to load configuration!", e);
            return;
        }

        // Initialize Discord bot
        try {
            jda = JDABuilder.createDefault(config.getBotToken())
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .build();
            jda.awaitReady();
            logger.info("Discord bot connected successfully!");
        } catch (Exception e) {
            logger.error("Failed to connect to Discord!", e);
            return;
        }

        // Initialize message builder
        messageBuilder = new DiscordMessageBuilder(config);

        // Register event listener
        server.getEventManager().register(this, new PlayerEventListener(jda, config, messageBuilder, logger));

        logger.info("VelocityDiscordLogger started successfully!");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (jda != null) {
            jda.shutdown();
        }
        logger.info("VelocityDiscordLogger stopped.");
    }

    private void loadConfig() throws IOException {
        if (!Files.exists(dataDirectory)) {
            Files.createDirectories(dataDirectory);
        }

        File configFile = dataDirectory.resolve("config.toml").toFile();

        if (!configFile.exists()) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.toml")) {
                if (in != null) {
                    Files.copy(in, configFile.toPath());
                    logger.info("Default configuration created. Please configure bot token and channel IDs.");
                }
            }
        }

        config = new PluginConfig(configFile.toPath());
    }
}
