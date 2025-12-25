package com.eric.GobangGameMC;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.UUID;

public class GobangMainMenu implements InventoryHolder {

    private final GobangGameMC plugin;
    private final Inventory inventory;

    public GobangMainMenu(GobangGameMC plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, 27, "§8Gobang Game Menu");
        initializeItems();
    }

    private void initializeItems() {
        // Create Game Item
        ItemStack createGame = new ItemStack(Material.EMERALD);
        ItemMeta createMeta = createGame.getItemMeta();
        createMeta.setDisplayName("§aCreate New Game");
        createMeta.setLore(Arrays.asList(
                "§7Click to invite a player",
                "§7to play Gobang with you"
        ));
        createGame.setItemMeta(createMeta);
        inventory.setItem(11, createGame);

        // Join Game Item
        ItemStack joinGame = new ItemStack(Material.COMPASS);
        ItemMeta joinMeta = joinGame.getItemMeta();
        joinMeta.setDisplayName("§eJoin Game");
        joinMeta.setLore(Arrays.asList(
                "§7View pending invitations",
                "§7and accept games"
        ));
        joinGame.setItemMeta(joinMeta);
        inventory.setItem(13, joinGame);

        // Help Item
        ItemStack help = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = help.getItemMeta();
        helpMeta.setDisplayName("§bGame Rules");
        helpMeta.setLore(Arrays.asList(
                "§7Five in a Row (Gobang)",
                "§7Place 5 pieces in a row",
                "§7to win the game!",
                "",
                "§eBlack goes first",
                "§fWhite goes second",
                "",
                "§6Use /gobanggame help for commands"
        ));
        help.setItemMeta(helpMeta);
        inventory.setItem(15, help);

        // Fill borders with glass panes
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);

        for (int i = 0; i < 27; i++) {
            if (i < 9 || i > 17 || i % 9 == 0 || i % 9 == 8) {
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

        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        if (slot == 11) { // Create Game
            player.closeInventory();
            new PlayerSelectorGUI(plugin, player).open(player);
        } else if (slot == 13) { // Join Game
            player.closeInventory();
            new InvitationGUI(plugin, player).open(player);
        } else if (slot == 15) { // Help
            player.sendMessage("§6=== Gobang Rules ===");
            player.sendMessage("§e1. Players take turns placing pieces");
            player.sendMessage("§e2. Black goes first, White goes second");
            player.sendMessage("§e3. Connect 5 pieces in a row to win");
            player.sendMessage("§e4. Rows can be horizontal, vertical, or diagonal");
            player.sendMessage("§e5. You have 60 seconds per move");
        }
    }
}