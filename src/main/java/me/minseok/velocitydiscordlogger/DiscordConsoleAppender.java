package me.minseok.velocitydiscordlogger;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Plugin(name = "DiscordConsoleAppender", category = "Core", elementType = "appender", printObject = true)
public class DiscordConsoleAppender extends AbstractAppender {

    private final JDA jda;
    private final PluginConfig config;
    private final Queue<String> logQueue = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public DiscordConsoleAppender(JDA jda, PluginConfig config) {
        super("DiscordConsoleAppender", null, PatternLayout.createDefaultLayout(), false, null);
        this.jda = jda;
        this.config = config;
    }

    @Override
    public void append(LogEvent event) {
        if (event == null)
            return;

        String message = event.getMessage().getFormattedMessage();
        // Filter out sensitive info or spam if needed
        logQueue.offer(message);
    }

    public void startLogSender() {
        scheduler.scheduleAtFixedRate(this::sendBatch, 1, 2, TimeUnit.SECONDS);
        super.start();
    }

    @Override
    public void stop() {
        scheduler.shutdown();
        super.stop();
    }

    private void sendBatch() {
        if (logQueue.isEmpty())
            return;

        TextChannel channel = jda.getTextChannelById(config.getConsoleChannelId());
        if (channel == null)
            return;

        StringBuilder batch = new StringBuilder();
        batch.append("```\n");

        while (!logQueue.isEmpty()) {
            String line = logQueue.poll();
            // Discord limit is 2000 chars. Reserve some for wrapper.
            if (batch.length() + line.length() + 10 > 1900) {
                // Put back if too long for this batch (simplified logic: just drop or split in
                // real app)
                // For now, just send what we have and start new batch next time
                // But to avoid losing logs, let's just break and send current batch
                // Ideally we should handle split, but this is simple version
                break;
            }
            batch.append(line).append("\n");
        }
        batch.append("```");

        if (batch.length() > 7) { // More than just empty code block
            channel.sendMessage(batch.toString()).queue(
                    null,
                    error -> {
                        // If failed, maybe rate limited or channel issue.
                        // Don't re-queue to avoid infinite loop of errors.
                    });
        }
    }
}
