package com.eric.GobangGameMC;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerSelectorGUI implements InventoryHolder {

    private final GobangGameMC plugin;
    private final Player selector;
    private final Inventory inventory;
    private int page;

    public PlayerSelectorGUI(GobangGameMC plugin, Player selector) {
        this.plugin = plugin;
        this.selector = selector;
        this.inventory = Bukkit.createInventory(this, 54, "§8Select Player to Invite");
        this.page = 0;
        loadPage(0);
    }

    private void loadPage(int page) {
        this.page = page;
        inventory.clear();

        // Get online players (excluding self)
        List<Player> players = Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.equals(selector))
                .filter(p -> !plugin.getPlayerGames().containsKey(p.getUniqueId()))
                .collect(Collectors.toList());

        int startIndex = page * 45;
        int endIndex = Math.min(startIndex + 45, players.size());

        // Add player heads
        for (int i = startIndex; i < endIndex; i++) {
            Player target = players.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);
            meta.setDisplayName("§a" + target.getName());
            meta.setLore(Arrays.asList(
                    "§7Click to invite to play Gobang",
                    "",
                    "§eStatus: §aOnline",
                    "§ePing: §7" + target.getPing() + "ms"
            ));
            head.setItemMeta(meta);
            inventory.setItem(i - startIndex, head);
        }

        // Add navigation buttons
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            prevMeta.setDisplayName("§ePrevious Page");
            prev.setItemMeta(prevMeta);
            inventory.setItem(45, prev);
        }

        if (endIndex < players.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.setDisplayName("§eNext Page");
            next.setItemMeta(nextMeta);
            inventory.setItem(53, next);
        }

        // Add back button
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§cBack to Menu");
        back.setItemMeta(backMeta);
        inventory.setItem(49, back);

        // Fill empty slots with glass panes
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);

        for (int i = 0; i < 54; i++) {
            if (inventory.getItem(i) == null && (i < 45 || i >= 54)) {
                inventory.setItem(i, border);
            }
        }
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);

        int slot = event.getRawSlot();

        if (slot == 45 && page > 0) {
            // Previous page
            loadPage(page - 1);
        } else if (slot == 53) {
            // Next page
            loadPage(page + 1);
        } else if (slot == 49) {
            // Back to menu
            selector.closeInventory();
            new GobangMainMenu(plugin).open(selector);
        } else if (slot < 45) {
            // Player head clicked
            ItemStack item = event.getCurrentItem();
            if (item != null && item.getType() == Material.PLAYER_HEAD) {
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                if (meta != null && meta.getOwningPlayer() != null) {
                    Player target = meta.getOwningPlayer().getPlayer();
                    if (target != null && target.isOnline()) {
                        selector.closeInventory();
                        plugin.getInvitationManager().sendInvitation(selector, target);
                    }
                }
            }
        }
    }
}