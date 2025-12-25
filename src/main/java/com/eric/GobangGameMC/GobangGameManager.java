package com.eric.GobangGameMC;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GobangGameManager {

    private final GobangGameMC plugin;
    private final UUID player1;
    private final UUID player2;
    private final GobangBoard board;
    private boolean isPlayer1Turn;
    private BukkitTask turnTask;
    private Map<UUID, GobangGameGUI> playerGUIs;

    public GobangGameManager(GobangGameMC plugin, Player player1, Player player2) {
        this.plugin = plugin;
        this.player1 = player1.getUniqueId();
        this.player2 = player2.getUniqueId();
        this.board = new GobangBoard();
        this.isPlayer1Turn = true;
        this.playerGUIs = new HashMap<>();

        // Add players to active games map
        plugin.addGame(this);

        // Start the game
        startGame();
    }

    private void startGame() {
        Player p1 = getPlayer1();
        Player p2 = getPlayer2();

        if (p1 != null && p2 != null) {
            p1.sendMessage("§aGame started! You are §lBLACK§a.");
            p2.sendMessage("§aGame started! You are §lWHITE§a.");
            p1.sendMessage("§eBlack goes first!");

            // Create GUIs for both players
            GobangGameGUI gui = new GobangGameGUI(this);
            playerGUIs.put(p1.getUniqueId(), gui);
            playerGUIs.put(p2.getUniqueId(), gui);

            // Open game GUI for both players
            openGameGUI(p1);
            openGameGUI(p2);

            // Start turn timer
            startTurnTimer();
        }
    }

    private void startTurnTimer() {
        if (turnTask != null) {
            turnTask.cancel();
        }

        turnTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player currentPlayer = isPlayer1Turn ? getPlayer1() : getPlayer2();
            Player opponent = isPlayer1Turn ? getPlayer2() : getPlayer1();

            if (currentPlayer != null && opponent != null) {
                // Current player loses due to timeout
                currentPlayer.sendMessage("§c⏰ Time's up! You lose!");
                opponent.sendMessage("§a✅ Your opponent ran out of time! You win!");

                // Broadcast to both
                broadcastGameResult("timeout", currentPlayer, opponent);
                endGame();
            }
        }, 20L * 60); // 60 seconds per turn
    }

    public boolean makeMove(Player player, int row, int col) {
        // Check if it's player's turn
        if ((isPlayer1Turn && !player.getUniqueId().equals(player1)) ||
                (!isPlayer1Turn && !player.getUniqueId().equals(player2))) {
            player.sendMessage("§cIt's not your turn!");
            return false;
        }

        // Get piece type (1 for black, 2 for white)
        int pieceType = isPlayer1Turn ? 1 : 2;

        // Make move on board
        if (board.makeMove(row, col, pieceType)) {
            // Update turn
            isPlayer1Turn = !isPlayer1Turn;

            // Reset turn timer
            startTurnTimer();

            // Update both players' GUIs without reopening
            updateGameGUIs();

            // Check for win
            if (board.checkWin(row, col, pieceType)) {
                // Determine winner and loser CORRECTLY
                Player winner = player; // The player who just made the winning move
                Player loser;

                // Loser is the other player
                if (winner.getUniqueId().equals(player1)) {
                    loser = getPlayer2();
                } else {
                    loser = getPlayer1();
                }

                // Debug logging
                plugin.getLogger().info("=== WIN DETECTED ===");
                plugin.getLogger().info("  Winning move by: " + winner.getName() + " (UUID: " + winner.getUniqueId() + ")");
                plugin.getLogger().info("  Player 1: " + getPlayer1().getName() + " (UUID: " + player1 + ")");
                plugin.getLogger().info("  Player 2: " + getPlayer2().getName() + " (UUID: " + player2 + ")");
                plugin.getLogger().info("  Loser determined: " + (loser != null ? loser.getName() : "null"));

                // Send individual win/lose messages
                if (winner.isOnline()) {
                    winner.sendMessage("§a§l🎉 CONGRATULATIONS! YOU WON! 🎉");
                    winner.sendMessage("§eYou got 5 in a row!");
                    plugin.getLogger().info("  Sent win message to: " + winner.getName());
                }

                if (loser != null && loser.isOnline()) {
                    loser.sendMessage("§c§l💀 YOU LOST! Better luck next time! 💀");
                    loser.sendMessage("§7Your opponent got 5 in a row.");
                    plugin.getLogger().info("  Sent lose message to: " + loser.getName());
                }

                // Broadcast game result to BOTH players
                broadcastGameResult("win", winner, loser);
                endGame();
                return true;
            }


            // Check for draw
            if (board.isBoardFull()) {
                Player p1 = getPlayer1();
                Player p2 = getPlayer2();

                if (p1 != null) {
                    p1.sendMessage("§e§l🤝 GAME ENDED IN A DRAW! 🤝");
                    p1.sendMessage("§7The board is completely filled.");
                }

                if (p2 != null) {
                    p2.sendMessage("§e§l🤝 GAME ENDED IN A DRAW! 🤝");
                    p2.sendMessage("§7The board is completely filled.");
                }

                // Broadcast draw
                broadcastGameResult("draw", p1, p2);
                endGame();
                return true;
            }

            // Send turn message to both players
            Player currentPlayer = isPlayer1Turn ? getPlayer1() : getPlayer2();
            Player opponent = isPlayer1Turn ? getPlayer2() : getPlayer1();

            if (currentPlayer != null) {
                currentPlayer.sendMessage("§a✅ It's your turn! Place your piece.");
            }
            if (opponent != null) {
                opponent.sendMessage("§e⏳ It's " + currentPlayer.getName() + "'s turn. Waiting...");
            }

            return true;
        } else {
            player.sendMessage("§cInvalid move! That position is already occupied.");
            return false;
        }
    }

    private void broadcastGameResult(String resultType, Player winner, Player loser) {
        // Get both players (guaranteed to get them correctly)
        Player p1 = getPlayer1();
        Player p2 = getPlayer2();

        if (p1 == null || p2 == null) {
            plugin.getLogger().warning("Cannot broadcast game result: one or both players are null");
            return;
        }

        // Debug logging
        plugin.getLogger().info("Broadcasting game result: " + resultType);
        plugin.getLogger().info("  Player 1: " + p1.getName() + " (UUID: " + p1.getUniqueId() + ")");
        plugin.getLogger().info("  Player 2: " + p2.getName() + " (UUID: " + p2.getUniqueId() + ")");
        plugin.getLogger().info("  Winner param: " + (winner != null ? winner.getName() : "null"));
        plugin.getLogger().info("  Loser param: " + (loser != null ? loser.getName() : "null"));

        String message = "";
        switch (resultType) {
            case "win":
                if (winner != null && loser != null) {
                    message = "§6══════════════════════════════════════\n" +
                            "§f" + winner.getName() + " §6defeated §f" + loser.getName() + "\n" +
                            "§7in a game of Gobang!\n" +
                            "§6══════════════════════════════════════";
                } else {
                    message = "§6══════════════════════════════════════\n" +
                            "§7Game ended with a win!\n" +
                            "§6══════════════════════════════════════";
                }
                break;

            case "draw":
                message = "§6══════════════════════════════════════\n" +
                        "§f" + p1.getName() + " §6and §f" + p2.getName() + "\n" +
                        "§7drew their Gobang game!\n" +
                        "§6══════════════════════════════════════";
                break;

            case "timeout":
                if (winner != null && loser != null) {
                    message = "§6══════════════════════════════════════\n" +
                            "§f" + winner.getName() + " §6won by timeout against\n" +
                            "§f" + loser.getName() + "§7!\n" +
                            "§6══════════════════════════════════════";
                }
                break;

            default:
                return;
        }

        if (!message.isEmpty()) {
            // Send to both players individually
            if (p1.isOnline()) {
                p1.sendMessage(message);
                plugin.getLogger().info("  Sent to Player 1: " + p1.getName());
            }
            if (p2.isOnline()) {
                p2.sendMessage(message);
                plugin.getLogger().info("  Sent to Player 2: " + p2.getName());
            }
        }
    }

    public void endGame() {
        if (turnTask != null) {
            turnTask.cancel();
            turnTask = null;
        }

        Player p1 = getPlayer1();
        Player p2 = getPlayer2();

        // Close inventories
        if (p1 != null && p1.isOnline()) {
            p1.closeInventory();
            p1.sendMessage("§7Game ended. Type §f/gobanggame §7to play again!");
        }

        if (p2 != null && p2.isOnline()) {
            p2.closeInventory();
            p2.sendMessage("§7Game ended. Type §f/gobanggame §7to play again!");
        }

        // Remove from plugin's game tracking
        plugin.removeGame(this);
        playerGUIs.clear();
    }

    private void openGameGUI(Player player) {
        GobangGameGUI gui = playerGUIs.get(player.getUniqueId());
        if (gui != null) {
            gui.open(player);
        }
    }

    private void updateGameGUIs() {
        Player p1 = getPlayer1();
        Player p2 = getPlayer2();

        // Get the shared GUI instance
        GobangGameGUI gui = playerGUIs.get(player1);
        if (gui != null) {
            // Update the board state
            gui.updateBoard();

            // Refresh for both players if they have the GUI open
            if (p1 != null) {
                gui.refreshForPlayer(p1);
            }
            if (p2 != null) {
                gui.refreshForPlayer(p2);
            }
        }
    }

    public Player getPlayer1() {
        return Bukkit.getPlayer(player1);
    }

    public Player getPlayer2() {
        return Bukkit.getPlayer(player2);
    }

    public UUID getPlayer1UUID() {
        return player1;
    }

    public UUID getPlayer2UUID() {
        return player2;
    }

    public GobangBoard getBoard() {
        return board;
    }

    public boolean isPlayer1Turn() {
        return isPlayer1Turn;
    }

    public boolean isPlayerInGame(Player player) {
        return player.getUniqueId().equals(player1) || player.getUniqueId().equals(player2);
    }

    public String getPlayerColor(Player player) {
        if (player.getUniqueId().equals(player1)) {
            return "§8BLACK§7 (Player 1)";
        } else if (player.getUniqueId().equals(player2)) {
            return "§fWHITE§7 (Player 2)";
        }
        return "§7Spectator";
    }
}