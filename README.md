# VelocityDiscordLogger

[한국어](README_KR.md) | English

A Velocity Minecraft proxy plugin that sends network-level join/quit notifications to Discord with beautiful embeds and player avatars.

## ✨ Features

- **Network-Level Detection** - Only sends messages when players join/leave the network, not when switching between backend servers
- **Rich Discord Embeds** - Beautiful colored embeds with left-side color bars (green for joins, red for quits)
- **Player Avatars** - Displays player's Minecraft head in the embed author field
- **Unified Configuration** - Manage all Discord channel IDs in one centralized config file
- **Lightweight** - No database required, minimal performance impact
- **Customizable Messages** - Supports custom message formats with placeholders

## 📸 Preview

Messages appear in Discord with:
- 🟢 Green embed for joins (#00ff00)
- 🔴 Red embed for quits (#ff0000)
- 👤 Player avatar from Visage API
- 📝 Customizable Korean/English messages

## 📥 Installation

1. Download the latest `VelocityDiscordLogger-X.X.X.jar` from [Releases](https://github.com/minseok7891/VelocityDiscordLogger/releases)
2. Place the JAR file in your `velocity/plugins/` folder
3. Restart your Velocity proxy server
4. Edit `velocity/plugins/velocitydiscordlogger/config.toml` with your bot token and channel ID
5. Restart Velocity again to apply configuration

## ⚙️ Configuration

After first startup, edit `velocity/plugins/velocitydiscordlogger/config.toml`:

```toml
# Discord Bot Token from https://discord.com/developers/applications
bot_token = "YOUR_BOT_TOKEN_HERE"

# Channel IDs (all in one place for easy management)
[channels]
log = "1234567890"           # Server log channel ID
chat = "9876543210"          # Chat channel ID (for future use)
deaths = "1111111111"        # Deaths channel ID (for future use)
achievements = "2222222222"  # Achievements channel ID (for future use)

# Join Message Settings
[messages.join]
enabled = true
color = "#00ff00"
format = "%username% joined the network"

# Quit Message Settings
[messages.quit]
enabled = true
color = "#ff0000"
format = "%username% left the network"

# Player Avatar Settings
[avatar]
base_url = "https://visage.surgeplay.com/face/96/{uuid}"
```

### Available Placeholders

- `%username%` - Player's Minecraft username
- `%uuid%` - Player's Minecraft UUID

## 🤖 Discord Bot Setup

1. Go to [Discord Developer Portal](https://discord.com/developers/applications)
2. Click "New Application" and give it a name
3. Go to "Bot" tab and click "Add Bot"
4. **Important:** Enable these Gateway Intents:
   - ✅ Server Members Intent
   - ✅ Presence Intent  
5. Copy the bot token and paste it in your `config.toml`
6. Go to "OAuth2" → "URL Generator"
   - Select scopes: `bot`
   - Select permissions: `Send Messages`, `Embed Links`
7. Copy the generated URL and open it to invite the bot to your Discord server

## 🔧 Building from Source

Requirements:
- Java 17+
- Gradle

```bash
git clone https://github.com/minseok7891/VelocityDiscordLogger.git
cd VelocityDiscordLogger
gradle shadowJar
```

Built JAR will be located at `build/libs/VelocityDiscordLogger-1.0.0.jar`

## 📋 Requirements

- Velocity 3.3.0 or higher
- Java 17 or higher
- Discord bot with Gateway Intents enabled
- Discord server with appropriate permissions

## 🆚 Comparison with Other Solutions

| Feature | VelocityDiscordLogger | DiscordSRV | HuskChat |
|---------|----------------------|------------|----------|
| Network-level detection | ✅ | ❌ (server-level) | ✅ |
| Embed + Avatar together | ✅ | ✅ | ❌ |
| No server-switch spam | ✅ | ❌ | ✅ |
| Lightweight | ✅ | ⚠️ | ✅ |

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Credits

- Built with [JDA (Java Discord API)](https://github.com/discord-jda/JDA)
- Player avatars from [Visage](https://visage.surgeplay.com/)

## 💬 Support

For issues, feature requests, or questions:
- Open an issue on [GitHub Issues](https://github.com/minseok7891/VelocityDiscordLogger/issues)
- Join our Discord: [Coming Soon]

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
