package com.eric.GobangGameMC;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;

public class GobangEventListener implements Listener {

    private final GobangGameMC plugin;

    public GobangEventListener(GobangGameMC plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof GobangMainMenu) {
            ((GobangMainMenu) holder).handleClick(event);
        } else if (holder instanceof GobangGameGUI) {
            ((GobangGameGUI) holder).handleClick(event);
        } else if (holder instanceof PlayerSelectorGUI) {
            ((PlayerSelectorGUI) holder).handleClick(event);
        } else if (holder instanceof InvitationGUI) {
            ((InvitationGUI) holder).handleClick(event);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        GobangGameManager game = plugin.getPlayerGame(player);

        if (game != null) {
            Player opponent = game.getPlayer1().getUniqueId().equals(player.getUniqueId())
                    ? game.getPlayer2()
                    : game.getPlayer1();

            if (opponent != null && opponent.isOnline()) {
                opponent.sendMessage("§c" + player.getName() + " disconnected. Game ended.");
            }

            game.endGame();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof GobangGameGUI) {
            GobangGameManager game = plugin.getPlayerGame(player);
            if (game != null && game.isPlayerInGame(player)) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline() &&
                            plugin.isPlayerInGame(player) &&
                            player.getOpenInventory().getTopInventory().getHolder() != holder) {
                        player.sendMessage("§eYour game is still active!");
                        player.sendMessage("§7Type §f/gobanggame menu §7to reopen the game board.");
                    }
                }, 2L); // 2 tick delay
            }
        }
    }
}