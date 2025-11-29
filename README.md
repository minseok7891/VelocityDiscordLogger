# VelocityDiscordLogger

A Velocity proxy plugin that sends network-level join/quit notifications to Discord with beautiful embeds and player avatars.

## Features

✨ **Network-Level Detection** - Only sends messages when players join/leave the network, not when switching between backend servers  
🎨 **Rich Discord Embeds** - Beautiful colored embeds with left-side color bars  
👤 **Player Avatars** - Shows player's Minecraft head in the embed author field  
⚙️ **Unified Configuration** - Manage all Discord channel IDs in one place  
🔌 **Lightweight** - No database required, minimal performance impact

## Screenshots

Messages appear in Discord with:
- Green embed for joins (#00ff00)
- Red embed for quits (#ff0000)
- Player avatar from Visage API
- Customizable Korean/English messages

## Installation

1. Download the latest `VelocityDiscordLogger-X.X.X.jar` from [Releases](https://github.com/minseokk7/VelocityDiscordLogger/releases)
2. Place in `velocity/plugins/` folder
3. Restart Velocity proxy
4. Configure `velocity/plugins/velocitydiscordlogger/config.toml`

## Configuration

```toml
# Discord Bot Configuration
bot_token = "YOUR_BOT_TOKEN_HERE"

# Channel IDs
[channels]
log = "1234567890"  # Server log channel

# Message Settings
[messages.join]
enabled = true
color = "#00ff00"
format = "%username% 님이 서버에 접속하셨습니다."

[messages.quit]
enabled = true
color = "#ff0000"
format = "%username% 님이 서버에서 나가셨습니다."
```

## Discord Bot Setup

1. Go to [Discord Developer Portal](https://discord.com/developers/applications)
2. Create a new Application
3. Go to "Bot" tab and create a bot
4. **Enable these Gateway Intents:**
   - Server Members Intent ✅
   - Presence Intent ✅
5. Copy the bot token and paste in `config.toml`
6. Invite bot to your server with permissions: Send Messages, Embed Links

## Building from Source

```bash
git clone https://github.com/minseokk7/VelocityDiscordLogger.git
cd VelocityDiscordLogger
gradle shadowJar
```

Built JAR will be in `build/libs/VelocityDiscordLogger-1.0.0.jar`

## Requirements

- Velocity 3.3.0+
- Java 17+
- Discord bot with Gateway Intents enabled

## License

MIT License - see [LICENSE](LICENSE) file for details

## Credits

- Built with [JDA (Java Discord API)](https://github.com/discord-jda/JDA)
- Player avatars from [Visage](https://visage.surgeplay.com/)

## Support

For issues or feature requests, please open an issue on [GitHub](https://github.com/minseokk7/VelocityDiscordLogger/issues).
