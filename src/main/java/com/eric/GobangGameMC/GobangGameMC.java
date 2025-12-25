package com.eric.GobangGameMC;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class GobangGameMC extends JavaPlugin implements CommandExecutor, TabCompleter {

    private Map<UUID, GobangGameManager> playerGames;
    private GameInvitationManager invitationManager;
    private static GobangGameMC instance;

    @Override
    public void onLoad() {
        instance = this;
        saveDefaultConfig();
        getLogger().info("GobangGameMC is loading...");
    }

    @Override
    public void onEnable() {
        getLogger().info("═══════════════════════════════════════");
        getLogger().info("GobangGameMC v" + getDescription().getVersion());
        getLogger().info("Minecraft: " + Bukkit.getVersion());
        getLogger().info("═══════════════════════════════════════");

        try {
            // Initialize managers
            this.playerGames = new HashMap<>();
            this.invitationManager = new GameInvitationManager(this);

            // ========== COMMAND REGISTRATION ==========
            // Register main command
            Objects.requireNonNull(getCommand("gobanggame")).setExecutor(this);
            Objects.requireNonNull(getCommand("gobanggame")).setTabCompleter(this);

            // Register events
            Bukkit.getPluginManager().registerEvents(new GobangEventListener(this), this);

            // Save default config if not exists
            saveDefaultConfig();
            reloadConfig();

            // Schedule periodic cleanup
            new BukkitRunnable() {
                @Override
                public void run() {
                    invitationManager.cleanupExpiredInvitations();
                }
            }.runTaskTimer(this, 20L * 60, 20L * 60); // Every minute

            // Log success
            getLogger().info("✓ Commands registered successfully");
            getLogger().info("✓ Event listener registered");
            getLogger().info("✓ Configuration loaded");
            getLogger().info("✓ Ready for players! Type /gobanggame to play");
            getLogger().info("═══════════════════════════════════════");

        } catch (Exception e) {
            getLogger().severe("✗ Failed to enable GobangGameMC!");
            getLogger().severe("Error: " + e.getMessage());
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling GobangGameMC...");

        if (playerGames != null) {
            int gameCount = playerGames.size() / 2; // Each game has 2 players
            for (GobangGameManager game : playerGames.values()) {
                try {
                    if (game != null) {
                        game.endGame();
                    }
                } catch (Exception e) {
                    // Ignore errors during shutdown
                }
            }
            playerGames.clear();
            getLogger().info("Ended " + gameCount + " active game(s)");
        }

        getLogger().info("GobangGameMC disabled successfully");
    }

    public void refreshPlayerGUI(Player player) {
        GobangGameManager game = getPlayerGame(player);
        if (game != null) {
            // The game manager will handle GUI updates
            Player opponent = game.getPlayer1().getUniqueId().equals(player.getUniqueId())
                    ? game.getPlayer2()
                    : game.getPlayer1();

            if (opponent != null) {
                // Trigger GUI update through game manager
                Bukkit.getScheduler().runTask(this, () -> {
                    // This will update without closing the inventory
                    player.updateInventory();
                    opponent.updateInventory();
                });
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmdName = command.getName().toLowerCase();

        // Handle main command and all its aliases
        if (cmdName.equals("gobanggame") || isAlias(cmdName)) {
            return handleGobangCommand(sender, label, args);
        }

        return false;
    }

    private boolean isAlias(String cmdName) {
        List<String> aliases = Arrays.asList("gg", "gobang", "fiveinarow");
        return aliases.contains(cmdName);
    }

    private boolean handleGobangCommand(CommandSender sender, String label, String[] args) {
        // Check if sender is player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        // Check permission
        if (!player.hasPermission("gobanggame.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        // No arguments - open main menu
        if (args.length == 0) {
            openMainMenu(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "invite":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /" + label + " invite <player>");
                    return true;
                }
                if (!player.hasPermission("gobanggame.invite")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to invite players!");
                    return true;
                }
                handleInvite(player, args[1]);
                break;

            case "accept":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /" + label + " accept <player>");
                    return true;
                }
                if (!player.hasPermission("gobanggame.accept")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to accept invitations!");
                    return true;
                }
                handleAccept(player, args[1]);
                break;

            case "deny":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /" + label + " deny <player>");
                    return true;
                }
                handleDeny(player, args[1]);
                break;

            case "quit":
                handleQuit(player);
                break;

            case "help":
                sendHelp(player, label);
                break;

            case "menu":
                openMainMenu(player);
                break;

            case "info":
                sendGameInfo(player);
                break;

            default:
                player.sendMessage(ChatColor.RED + "Unknown command. Use /" + label + " help for help.");
        }

        return true;
    }

    private void openMainMenu(Player player) {
        try {
            new GobangMainMenu(this).open(player);
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Failed to open menu. Please try again.");
            getLogger().warning("Error opening menu for " + player.getName() + ": " + e.getMessage());
        }
    }

    private void handleInvite(Player sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player '" + targetName + "' is not online!");
            return;
        }

        if (target.equals(sender)) {
            sender.sendMessage(ChatColor.RED + "You can't invite yourself!");
            return;
        }

        // Check if either player is already in a game
        if (playerGames.containsKey(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "You are already in a game! Use /gobanggame quit first.");
            return;
        }

        if (playerGames.containsKey(target.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + target.getName() + " is already in a game!");
            return;
        }

        invitationManager.sendInvitation(sender, target);
    }

    private void handleAccept(Player accepter, String senderName) {
        Player sender = Bukkit.getPlayer(senderName);
        if (sender == null) {
            accepter.sendMessage(ChatColor.RED + "Player '" + senderName + "' is not online!");
            return;
        }

        // Check if either player is already in a game
        if (playerGames.containsKey(accepter.getUniqueId())) {
            accepter.sendMessage(ChatColor.RED + "You are already in a game! Use /gobanggame quit first.");
            return;
        }

        if (playerGames.containsKey(sender.getUniqueId())) {
            accepter.sendMessage(ChatColor.RED + sender.getName() + " is already in a game!");
            return;
        }

        boolean success = invitationManager.acceptInvitation(accepter, senderName);
        if (!success) {
            accepter.sendMessage(ChatColor.RED + "No pending invitation from '" + senderName + "'!");
        }
    }

    private void handleDeny(Player denier, String senderName) {
        boolean success = invitationManager.denyInvitation(denier, senderName);
        if (!success) {
            denier.sendMessage(ChatColor.RED + "No pending invitation from '" + senderName + "'!");
        }
    }

    private void handleQuit(Player player) {
        GobangGameManager game = playerGames.get(player.getUniqueId());
        if (game != null) {
            Player opponent = game.getPlayer1().getUniqueId().equals(player.getUniqueId())
                    ? game.getPlayer2()
                    : game.getPlayer1();

            game.endGame();
            player.sendMessage(ChatColor.YELLOW + "You have left the game.");

            if (opponent != null && opponent.isOnline()) {
                opponent.sendMessage(ChatColor.YELLOW + player.getName() + " has left the game.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "You are not in a game!");
        }
    }

    private void sendHelp(Player player, String label) {
        player.sendMessage(ChatColor.GOLD + "╔══════════════════════════════════════╗");
        player.sendMessage(ChatColor.GOLD + "║         " + ChatColor.YELLOW + "Gobang Game Help" + ChatColor.GOLD + "         ║");
        player.sendMessage(ChatColor.GOLD + "╠══════════════════════════════════════╣");
        player.sendMessage(ChatColor.YELLOW + "  /" + label + ChatColor.GRAY + " - Open game menu");
        player.sendMessage(ChatColor.YELLOW + "  /" + label + " invite <player>" + ChatColor.GRAY + " - Invite player");
        player.sendMessage(ChatColor.YELLOW + "  /" + label + " accept <player>" + ChatColor.GRAY + " - Accept invitation");
        player.sendMessage(ChatColor.YELLOW + "  /" + label + " deny <player>" + ChatColor.GRAY + " - Deny invitation");
        player.sendMessage(ChatColor.YELLOW + "  /" + label + " quit" + ChatColor.GRAY + " - Leave current game");
        player.sendMessage(ChatColor.YELLOW + "  /" + label + " help" + ChatColor.GRAY + " - Show this help");
        player.sendMessage(ChatColor.YELLOW + "  /" + label + " info" + ChatColor.GRAY + " - Show game info");
        player.sendMessage(ChatColor.GOLD + "╠══════════════════════════════════════╣");
        player.sendMessage(ChatColor.GRAY + "  Game Rules:");
        player.sendMessage(ChatColor.GRAY + "  • Get 5 pieces in a row to win");
        player.sendMessage(ChatColor.GRAY + "  • Rows can be horizontal, vertical, or diagonal");
        player.sendMessage(ChatColor.GRAY + "  • Black goes first, White goes second");
        player.sendMessage(ChatColor.GRAY + "  • You have 60 seconds per move");
        player.sendMessage(ChatColor.GOLD + "╚══════════════════════════════════════╝");
    }

    private void sendGameInfo(Player player) {
        GobangGameManager game = playerGames.get(player.getUniqueId());

        player.sendMessage(ChatColor.GOLD + "╔══════════════════════════════════════╗");
        player.sendMessage(ChatColor.GOLD + "║        " + ChatColor.YELLOW + "Gobang Game Info" + ChatColor.GOLD + "         ║");
        player.sendMessage(ChatColor.GOLD + "╠══════════════════════════════════════╣");

        if (game != null) {
            Player p1 = game.getPlayer1();
            Player p2 = game.getPlayer2();
            boolean isPlayer1 = player.getUniqueId().equals(game.getPlayer1UUID());

            player.sendMessage(ChatColor.YELLOW + "  Status: " + ChatColor.GREEN + "In Game");
            player.sendMessage(ChatColor.YELLOW + "  You are: " +
                    (isPlayer1 ? ChatColor.BLACK + "⚫ Black" : ChatColor.WHITE + "⚪ White"));
            player.sendMessage(ChatColor.YELLOW + "  Opponent: " + ChatColor.WHITE +
                    (isPlayer1 ? p2.getName() : p1.getName()));
            player.sendMessage(ChatColor.YELLOW + "  Turn: " +
                    (game.isPlayer1Turn() ? ChatColor.BLACK + "Black's turn" : ChatColor.WHITE + "White's turn"));

            if ((game.isPlayer1Turn() && isPlayer1) || (!game.isPlayer1Turn() && !isPlayer1)) {
                player.sendMessage(ChatColor.GREEN + "  ✓ It's your turn!");
            } else {
                player.sendMessage(ChatColor.YELLOW + "  ⏳ Waiting for opponent...");
            }
        } else {
            player.sendMessage(ChatColor.YELLOW + "  Status: " + ChatColor.GRAY + "Not in a game");
            player.sendMessage(ChatColor.GRAY + "  Use /gobanggame to start playing!");

            // Show pending invitations
            int pendingCount = invitationManager.getPendingInvitationCount(player);
            if (pendingCount > 0) {
                player.sendMessage(ChatColor.YELLOW + "  You have " + pendingCount + " pending invitation(s)");
                player.sendMessage(ChatColor.GRAY + "  Use /gobanggame menu to view them");
            }
        }

        player.sendMessage(ChatColor.GOLD + "╚══════════════════════════════════════╝");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        String cmdName = command.getName().toLowerCase();

        // Only handle our commands
        if (!cmdName.equals("gobanggame") && !isAlias(cmdName)) {
            return completions;
        }

        // First argument suggestions
        if (args.length == 1) {
            completions.add("invite");
            completions.add("accept");
            completions.add("deny");
            completions.add("quit");
            completions.add("help");
            completions.add("menu");
            completions.add("info");

            // Filter based on what they've typed
            return filterCompletions(completions, args[0]);
        }

        // Second argument suggestions for specific commands
        else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("invite") || subCommand.equals("accept") || subCommand.equals("deny")) {
                // Suggest online players
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (sender instanceof Player && player.equals(sender)) {
                        continue; // Don't suggest self for invite
                    }

                    // For "accept", only suggest players who have invited the sender
                    if (subCommand.equals("accept") && sender instanceof Player) {
                        Player s = (Player) sender;
                        if (!invitationManager.hasInvitationFrom(player, s)) {
                            continue;
                        }
                    }

                    completions.add(player.getName());
                }

                return filterCompletions(completions, args[1]);
            }
        }

        return completions;
    }

    private List<String> filterCompletions(List<String> completions, String input) {
        if (input == null || input.isEmpty()) {
            return completions;
        }

        List<String> filtered = new ArrayList<>();
        String inputLower = input.toLowerCase();

        for (String completion : completions) {
            if (completion.toLowerCase().startsWith(inputLower)) {
                filtered.add(completion);
            }
        }

        Collections.sort(filtered);
        return filtered;
    }

    // ========== PUBLIC API METHODS ==========

    public Map<UUID, GobangGameManager> getPlayerGames() {
        return playerGames;
    }

    public GameInvitationManager getInvitationManager() {
        return invitationManager;
    }

    public boolean isPlayerInGame(Player player) {
        return playerGames.containsKey(player.getUniqueId());
    }

    public GobangGameManager getPlayerGame(Player player) {
        return playerGames.get(player.getUniqueId());
    }

    public void addGame(GobangGameManager game) {
        playerGames.put(game.getPlayer1UUID(), game);
        playerGames.put(game.getPlayer2UUID(), game);
    }

    public void removeGame(GobangGameManager game) {
        playerGames.remove(game.getPlayer1UUID());
        playerGames.remove(game.getPlayer2UUID());
    }

    public void removePlayerFromGame(Player player) {
        playerGames.remove(player.getUniqueId());
    }

    public static GobangGameMC getInstance() {
        return instance;
    }

    // ========== UTILITY METHODS ==========

    public void broadcastToGamePlayers(Player player, String message) {
        GobangGameManager game = getPlayerGame(player);
        if (game != null) {
            Player p1 = game.getPlayer1();
            Player p2 = game.getPlayer2();

            if (p1 != null && p1.isOnline()) {
                p1.sendMessage(message);
            }
            if (p2 != null && p2.isOnline()) {
                p2.sendMessage(message);
            }
        }
    }

    public String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return minutes + "m " + remainingSeconds + "s";
    }
}