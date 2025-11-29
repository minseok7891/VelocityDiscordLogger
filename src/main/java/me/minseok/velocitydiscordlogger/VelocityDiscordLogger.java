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
import java.util.concurrent.atomic.AtomicBoolean;

@Plugin(id = "velocitydiscordlogger", name = "VelocityDiscordLogger", version = "1.0.0", description = "Network-level Discord logging with embeds and player icons", authors = {
        "minseok" })
public class VelocityDiscordLogger {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private PluginConfig config;
    private JDA jda;
    private DiscordMessageBuilder messageBuilder;
    private DiscordConsoleAppender consoleAppender;

    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);

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

        // Register plugin messaging channel
        server.getChannelRegistrar().register(PluginMessageListener.getIdentifier());
        PluginMessageListener pluginMessageListener = new PluginMessageListener(jda, config, messageBuilder, logger);
        server.getEventManager().register(this, pluginMessageListener);

        // Register event listener for join/quit
        server.getEventManager().register(this, new PlayerEventListener(jda, config, messageBuilder, logger));

        // Setup Console Logging
        setupConsoleLogging();

        // Send Server Start Message
        if (config.isStartEnabled()) {
            String channelId = config.getStatusChannelId();
            logger.info("Attempting to send start message to channel: " + channelId);

            if (!channelId.isEmpty()) {
                net.dv8tion.jda.api.entities.channel.concrete.TextChannel channel = jda.getTextChannelById(channelId);
                if (channel != null) {
                    try {
                        channel.sendMessageEmbeds(messageBuilder.buildServerStartEmbed()).complete();
                        logger.info("Server start message sent successfully!");
                    } catch (Exception e) {
                        logger.error("Failed to send server start message", e);
                    }
                } else {
                    logger.warn("Status channel NOT found! ID: " + channelId);
                }
            } else {
                logger.warn("Status channel ID is empty in config!");
            }
        }

        // Register JVM Shutdown Hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("JVM Shutdown Hook triggered!");
            handleShutdown();
        }));

        logger.info("VelocityDiscordLogger started successfully!");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("ProxyShutdownEvent received.");
        handleShutdown();
    }

    private void handleShutdown() {
        if (isShuttingDown.getAndSet(true)) {
            return;
        }

        logger.info("Handling shutdown sequence...");

        // Send Server Stop Message via REST API (More reliable during shutdown)
        if (config != null && config.isStopEnabled()) {
            String channelId = config.getStatusChannelId();
            if (!channelId.isEmpty()) {
                sendShutdownMessageViaRest(channelId);
            }
        }

        // Artificial delay to ensure network buffers flush and logs are written
        try {
            logger.info("Delaying shutdown by 3 seconds to ensure message delivery...");
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (consoleAppender != null) {
            consoleAppender.stop();
            org.apache.logging.log4j.core.Logger coreLogger = (org.apache.logging.log4j.core.Logger) org.apache.logging.log4j.LogManager
                    .getRootLogger();
            coreLogger.removeAppender(consoleAppender);
        }

        if (jda != null) {
            logger.info("Shutting down JDA...");
            jda.shutdown();
            try {
                if (!jda.awaitShutdown(java.time.Duration.ofSeconds(5))) {
                    jda.shutdownNow();
                }
            } catch (InterruptedException e) {
                jda.shutdownNow();
            }
        }
        logger.info("VelocityDiscordLogger stopped.");
    }

    private void sendShutdownMessageViaRest(String channelId) {
        try {
            java.net.URL url = new java.net.URL("https://discord.com/api/v10/channels/" + channelId + "/messages");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bot " + config.getBotToken());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "VelocityDiscordLogger/1.0");
            conn.setDoOutput(true);

            // Construct simple JSON payload for embed
            // Color #ff0000 is 16711680
            String jsonBody = "{"
                    + "\"embeds\": [{"
                    + "\"title\": \":octagonal_sign: 서버가 종료되었습니다.\","
                    + "\"color\": 16711680"
                    + "}]"
                    + "}";

            try (java.io.OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            logger.info("Sent shutdown message via REST. Response Code: " + responseCode);

            // Read response to ensure it completes
            try (java.io.InputStream is = (responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream())) {
                if (is != null)
                    is.readAllBytes();
            }

        } catch (Exception e) {
            logger.error("Failed to send shutdown message via REST", e);
        }
    }

    private void setupConsoleLogging() {
        if (config.getConsoleChannelId().isEmpty())
            return;

        try {
            org.apache.logging.log4j.core.Logger coreLogger = (org.apache.logging.log4j.core.Logger) org.apache.logging.log4j.LogManager
                    .getRootLogger();
            consoleAppender = new DiscordConsoleAppender(jda, config);
            consoleAppender.startLogSender();
            coreLogger.addAppender(consoleAppender);
            logger.info("Console logging enabled for channel: " + config.getConsoleChannelId());
        } catch (Exception e) {
            logger.error("Failed to setup console logging", e);
        }
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
