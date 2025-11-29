# VelocityDiscordLogger

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Velocity](https://img.shields.io/badge/Velocity-3.3.0+-blue?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

**[🇰🇷 한국어 (Korean)](README.md)**

</div>

---

**VelocityDiscordLogger** is a **robust and modern Discord logging plugin** for Minecraft Velocity proxy servers.  
Monitor your entire network status perfectly without DiscordSRV, and get notified with beautiful Embed messages.

## ✨ Key Features

### 📡 Network-Wide Monitoring
- **Join/Quit Logs**: Logs when players join or leave the network with clean Embed messages.
- **Ignore Server Switching**: Option to ignore server switching logs to reduce spam.

### 🔔 Server Status Notifications (Robust!)
- **Start/Stop Alerts**: Sends notifications to the announcement channel when the proxy starts or stops.
- **🛡️ Force Shutdown Protection**: Uses **JVM Shutdown Hook** and **REST API** to ensure the final stop notification is sent even during `docker restart` or unexpected forced shutdowns. (Zero missed alerts!)

### 💻 Real-time Console Mirroring
- **Console Logs**: Stream Velocity server console logs to a Discord channel in real-time.

### 🔗 Backend Server Integration (Paper/Purpur)
- **🏆 Achievements**: Logs player advancements to Discord.
- **☠️ Death Messages**: Logs player death causes to Discord.
- *(Requires separate backend plugin installation)*

---

## 📥 Installation

1. Download the latest version from [Releases](https://github.com/minseok7891/VelocityDiscordLogger/releases).
2. Place the `VelocityDiscordLogger-1.0.0.jar` file into your Velocity `plugins` folder.
3. Restart the server to generate configuration files.
4. Edit `plugins/velocitydiscordlogger/config.toml` to complete the setup.

---

## ⚙️ Configuration Guide (`config.toml`)

```toml
# ==========================================
#        VelocityDiscordLogger Config
# ==========================================

# 1. Discord Bot Token (Required)
bot_token = "YOUR_BOT_TOKEN_HERE"

# 2. Channel IDs
[channels]
log = "123456789012345678"          # 📝 Join/Quit Logs
status = "123456789012345678"       # 📢 Server Start/Stop Alerts
console = "123456789012345678"      # 💻 Console Logs
achievements = "123456789012345678" # 🏆 Achievement Logs
deaths = "123456789012345678"       # ☠ Death Logs

# 3. Message Customization
[messages.join]
enabled = true
color = "#00ff00" # Green
format = "%username% joined the network."

[messages.quit]
enabled = true
color = "#ff0000" # Red
format = "%username% left the network."

[messages.start]
enabled = true
color = "#00ff00"
format = ":white_check_mark: **Server Started!**"

[messages.stop]
enabled = true
color = "#ff0000"
format = ":octagonal_sign: **Server Stopped.**"

# 4. Avatar Settings (Visage API)
[avatar]
base_url = "https://visage.surgeplay.com/face/96/{uuid}"
```

---

## 🧩 Backend Plugin Installation

To use **Achievement** and **Death Message** features, you must install the **[VelocityDiscordLogger-Backend](https://github.com/minseok7891/VelocityDiscordLogger-Backend)** plugin on each of your backend servers (Lobby, Survival, etc.).

---

<div align="center">
  Made with ❤️ by minseok
</div>
