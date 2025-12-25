# GobangGame *MC*
[![Build](https://github.com/QIU2014/GobangGameMC/actions/workflows/mvn.yml/badge.svg?branch=main)](https://github.com/QIU2014/GobangGameMC/actions/workflows/mvn.yml)
![Release](https://img.shields.io/github/v/release/QIU2014/GobangGameMC)
![MIT Licence](https://img.shields.io/badge/License-MIT-yellow.svg)

__This is a _Minecraft Bukkit Server_ <u>plugin</u> version of GobangGame__
## Description

A Minecraft plugin that brings the classic game of __Gobang__ (Five in a Row) into Minecraft with beautiful GUI interfaces and multiplayer support.

## Installation

### For server owners
1. Download the latest `GobangGameMC.jar` from [Github Release](https://github.com/QIU2014/GobangGameMC/releases) page
2. Place the JAR file in your server's `plugins/` folder
3. Restart your server
4. Players can now use `/gobanggame` to play!

### For developers
```bash
# Clone the repository
git clone https://github.com/QIU2014/GobangGameMC.git
cd GobangGameMC

# Build with Maven
mvn clean package

# The compiled plugin will be in target/GobangGameMC-1.0.0.jar
```

## 🎮 Commands

|  Command | Description                                | Permission          |
|----------|--------------------------------------------|---------------------|
|`/gobanggame`| Open main menu                             | `gobanggame.use`    |
|`/gg`| Alias for /gobanggame                      | `gobanggame.use`    |
|`/gobanggame invite <player>`| Invite a player to play| `gobanggame.invite` |
|`/gobanggame accept <player>`|Accept an invitation|`gobanggame.accept`|
|`/gobanggame deny <player>`|Deny an invitation|`gobanggame.use`|
|`/gobanggame quit`|Leave current game|`gobanggame.use`|
|`/gobanggame help`| Show help menu|`gobanggame.use`|

## 📖 How to Play

### Starting a game:
1. Type `/gobanggame` to open the main menu
2. Click "Create New Game" to invite a player
3. Select a player from the online list
4. They'll receive an invitation notification
5. Once they accept, the game begins!

### Game rules:
- __Black__ goes first, __White__ goes second
- Players take turns placing their pieces
- Connect __5 pieces in a row__ to win (horizontal, vertical, or diagonal)
- You have __60 seconds__ per move
- If the board fills completely, it's a draw

### Game interface:
- __Top 5 rows__: 15x15 game board (showing first 45 slots)
- __Bottom row__: Game information and controls
- __Turn Indicator__: Shows whose turn it is (black/white dye)
- __Quit Button__: Red barrier to leave the game

## ⚙️ Configuration
After first run, a `config.yml` file will be created in `plugins/GobangGameMC/`:
```yaml
# Gobang Game Configuration
version: 1.0

# Game Settings
game:
  turn-time: 60          # Seconds per turn
  board-size: 15         # Size of the board (15x15)
  max-games: 10          # Maximum concurrent games
  
# Invitations
invitations:
  timeout: 300           # 5 minutes in seconds
  max-pending: 5         # Max pending invites per player
  
# Performance
performance:
  autosave-interval: 300 # Save games every 5 minutes
  cleanup-interval: 600  # Clean up old data every 10 minutes
```

## 🧩 Permissions
| Permission      |Description|Default|
|-----------------|-----------|-------|
| `gobanggame.use` |Use main commands|`true`|
|`gobanggame.invite`|Invite players|`true`|
|`gobanggame.accept`|Accept invitations|`true`|
|`gobanggame.admin`|Admin commands|`op`|

## 🛠️ Development

### Prerequisites:
- Java 21 or higher
- Maven 3.6+
- Minecraft Plugin Server (Bukkit/Spigot/Paper 1.21+)

### Project structure:

```text
GobangGameMC/
├── src/main/java/com/eric/GobangGameMC/
│   ├── GobangGameMC.java          # Main plugin class
│   ├── GobangGameManager.java     # Game logic manager
│   ├── GobangBoard.java           # Board state and win detection
│   ├── GameInvitationManager.java # Invitation system
│   ├── GobangEventListener.java   # Event handlers
│   ├── GobangMainMenu.java        # Main menu GUI
│   ├── GobangGameGUI.java         # Game interface GUI
│   ├── PlayerSelectorGUI.java     # Player selection GUI
│   └── InvitationGUI.java         # Invitation management GUI
├── src/main/resources/
│   ├── plugin.yml                  # Plugin metadata
│   └── config.yml                  # Default configuration
├── pom.xml                         # Maven configuration
└── README.md                       # This file
```

### Building:
```bash
# Clean build
mvn clean package

# Skip tests
mvn clean package -DskipTests

# Install to local Maven repository
mvn clean install
```

## 📁 Files
- `plugin.yml` - Plugin metadata, commands, and permissions
- `config.yml` - Configuration file (auto-generated)
- `pom.xml` - Maven dependencies and build configuration

## 🔧 Troubleshooting

### Common Issues:
1. Plugin won't enable:
- Ensure you're using Java 21 or higher
- Check server logs for errors
- Verify plugin.yml is correctly formatted

2. Commands not working:
- Type /reload confirm to reload plugins
- Check that you have the required permissions
- Ensure you're using Minecraft 1.21.11+

3. GUI not opening:
- Make sure you're a player (not console)
- Check for inventory conflicts with other plugins

4. Win detection not working:
- Verify you have 5 pieces in a straight line
- Check server logs for win detection errors

### Debugging:
```bash
# Check server logs
tail -f logs/latest.log | grep -i "gobang"

# Test commands in-game
/gobanggame debug players
```

## 🤝 Contributing
Contributions are welcome! Here's how you can help:

1. Fork the repository
2. Create a feature branch: git checkout -b feature/amazing-feature
3. Commit your changes: git commit -m 'Add amazing feature'
4. Push to the branch: git push origin feature/amazing-feature
5. Open a Pull Request

## Development Guidelines:
- Follow Java naming conventions
- Add comments for complex logic
- Update documentation when changing features
- Test thoroughly before submitting PR

## 📊 Statistics
- Lines of Code: ~2,000
- Classes: 9
- GUI Screens: 4
- Supported Minecraft Versions: 1.21.10+

## 🎯 Roadmap
- AI opponent (Easy/Medium/Hard difficulties)
- Tournament mode with brackets
- Statistics tracking (wins/losses/win rate)
- Spectator mode
- Custom board sizes (13x13, 19x19)
- Sound effects
- Particle effects for wins
- Database support for persistent stats
- Web interface for remote viewing

## 📜 License
This project is licensed under the MIT License - see the [LICENSE](https://github.com/QIU2014/GobangGameMC?tab=MIT-1-ov-file) file for details.
```text
MIT License

Copyright (c) 2025 Eric

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

## 🙏 Acknowledgments
- Mojang for Minecraft
- SpigotMC for the plugin API
- PaperMC for server optimizations

## 📞 Support
- Issues: [GitHub Issues](https://github.com/QIU2014/GobangGameMC/issues)
- Email: [hanruericqiu@gmail.com](mailto:hanruericqiu@gmail.com)

## 📈 Downloads
[![count](https://img.shields.io/github/downloads/QIU2014/GobangGameMC/latest/total)](https://github.com/QIU2014/GobangGameMC/releases/latest)

### Made with ❤️ for the Minecraft community
Star this repo if you find it useful! ⭐

## 🎮 Game Preview
```text

[Player1] invited you to play Gobang!
Use /gobanggame accept Player1 to accept

══════════════════════════════════════
Player1 defeated Player2
in a game of Gobang!
══════════════════════════════════════
```

__Enjoy playing Gobang in Minecraft! 🎮⚫⚪__