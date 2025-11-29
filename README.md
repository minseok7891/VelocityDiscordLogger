# VelocityDiscordLogger

[🇰🇷 한국어 (Korean)](README_KR.md)

A robust Velocity proxy plugin for logging Minecraft server events to Discord. It supports network-wide join/quit messages, server status notifications, console logging, and integrates with backend servers for achievements and death messages.

## Features

- **Network-wide Logging**:
  - **Join/Quit**: Logs when players join or leave the proxy network.
  - **Server Switching**: (Optional) Can be configured to ignore server switches to reduce spam.
- **Server Status Notifications**:
  - Sends an embed message when the proxy starts (`:white_check_mark:`) and stops (`:octagonal_sign:`).
  - **Robust Shutdown**: Includes a JVM shutdown hook and REST API fallback to ensure stop notifications are sent even during forced terminations (e.g., `docker restart`).
- **Console Logging**:
  - Streams Velocity console logs to a specific Discord channel in real-time.
- **Backend Integration**:
  - Works with `VelocityDiscordLogger-Backend` to log **Achievements** and **Death Messages** from backend servers (Paper/Purpur).
- **Customizable Messages**:
  - Fully configurable messages, colors, and formats via `config.toml`.
  - Supports player avatars in embeds.

## Installation

1. Download the latest release.
2. Place the `VelocityDiscordLogger-1.0.0.jar` file into your Velocity `plugins` folder.
3. Restart the proxy.
4. Configure `plugins/velocitydiscordlogger/config.toml` with your Discord Bot Token and Channel IDs.

## Configuration

```toml
# Discord Bot Configuration
bot_token = "YOUR_BOT_TOKEN_HERE"

[channels]
log = "CHANNEL_ID"           # Join/Quit logs
status = "CHANNEL_ID"        # Server Start/Stop notifications
console = "CHANNEL_ID"       # Console logs
achievements = "CHANNEL_ID"  # Achievement logs (requires backend plugin)
deaths = "CHANNEL_ID"        # Death logs (requires backend plugin)

[messages.join]
enabled = true
format = "%username% joined the network."

[messages.quit]
enabled = true
format = "%username% left the network."
```

## Backend Plugin

To enable **Achievement** and **Death** logging, you must install the [VelocityDiscordLogger-Backend](https://github.com/minseok7891/VelocityDiscordLogger-Backend) plugin on your backend servers (Lobby, Survival, Creative, etc.).
