package com.eric.GobangGameMC;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GobangGameGUI implements InventoryHolder {

    private final GobangGameManager gameManager;
    private final Inventory inventory;
    private static final int BOARD_SIZE = 15;
    private static final int BOARD_START_SLOT = 0;
    private static final int BOARD_END_SLOT = 44; // 5 rows (0-44)

    // Cache for board items to avoid recreating them every time
    private final Map<String, ItemStack> itemCache = new HashMap<>();

    public GobangGameGUI(GobangGameManager gameManager) {
        this.gameManager = gameManager;
        this.inventory = Bukkit.createInventory(this, 54, "§8Gobang Game");
        initializeBoard();
        updateBoard(); // Initial update
        addStaticItems(); // Add static items that don't change
    }

    private void initializeBoard() {
        // Pre-cache items for better performance
        cacheItems();
    }

    private void cacheItems() {
        // Black piece
        ItemStack blackPiece = new ItemStack(Material.BLACK_CONCRETE);
        ItemMeta blackMeta = blackPiece.getItemMeta();
        blackMeta.setDisplayName("§8Black Piece");
        blackPiece.setItemMeta(blackMeta);
        itemCache.put("black", blackPiece);

        // White piece
        ItemStack whitePiece = new ItemStack(Material.WHITE_CONCRETE);
        ItemMeta whiteMeta = whitePiece.getItemMeta();
        whiteMeta.setDisplayName("§fWhite Piece");
        whitePiece.setItemMeta(whiteMeta);
        itemCache.put("white", whitePiece);

        // Empty slot (multiple variations for different positions)
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                String key = "empty_" + i + "_" + j;
                ItemStack empty = new ItemStack(getEmptySlotMaterial(i, j));
                ItemMeta emptyMeta = empty.getItemMeta();
                emptyMeta.setDisplayName("§7Position " + (i + 1) + "," + (j + 1));
                emptyMeta.setLore(Arrays.asList("§7Click to place piece here"));
                empty.setItemMeta(emptyMeta);
                itemCache.put(key, empty);
            }
        }
    }

    private Material getEmptySlotMaterial(int row, int col) {
        // Use different materials for a checkerboard pattern
        if ((row + col) % 2 == 0) {
            return Material.LIGHT_GRAY_STAINED_GLASS_PANE;
        } else {
            return Material.GRAY_STAINED_GLASS_PANE;
        }
    }

    public void updateBoard() {
        GobangBoard board = gameManager.getBoard();

        // Only update the board area (slots 0-44)
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                int slot = i * 9 + j;
                if (slot > BOARD_END_SLOT) continue; // Only update visible slots

                int piece = board.getPiece(i, j);
                ItemStack item;

                if (piece == 1) { // Black piece
                    item = itemCache.get("black").clone();
                    ItemMeta meta = item.getItemMeta();
                    meta.setLore(Arrays.asList(
                            "§7Position: " + (i + 1) + ", " + (j + 1),
                            "§8Player 1"
                    ));
                    item.setItemMeta(meta);
                } else if (piece == 2) { // White piece
                    item = itemCache.get("white").clone();
                    ItemMeta meta = item.getItemMeta();
                    meta.setLore(Arrays.asList(
                            "§7Position: " + (i + 1) + ", " + (j + 1),
                            "§fPlayer 2"
                    ));
                    item.setItemMeta(meta);
                } else { // Empty spot
                    String key = "empty_" + i + "_" + j;
                    item = itemCache.get(key).clone();
                }

                inventory.setItem(slot, item);
            }
        }

        // Update turn indicator (slot 49) without refreshing whole GUI
        updateTurnIndicator();
    }

    private void addStaticItems() {
        // These items never change, add them once
        addInfoItems();
    }

    private void updateTurnIndicator() {
        // Only update the turn indicator item
        ItemStack turnIndicator = new ItemStack(
                gameManager.isPlayer1Turn() ? Material.BLACK_DYE : Material.WHITE_DYE
        );
        ItemMeta turnMeta = turnIndicator.getItemMeta();
        turnMeta.setDisplayName(gameManager.isPlayer1Turn() ? "§8Black's Turn" : "§fWhite's Turn");
        turnMeta.setLore(Arrays.asList(
                "§7Current player to move",
                gameManager.isPlayer1Turn() ? "§8Player 1 (Black)" : "§fPlayer 2 (White)"
        ));
        turnIndicator.setItemMeta(turnMeta);

        inventory.setItem(49, turnIndicator);
    }

    private void addInfoItems() {
        // Player info (slot 45) - static
        ItemStack playerInfo = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = playerInfo.getItemMeta();
        infoMeta.setDisplayName("§6Game Info");

        Player p1 = gameManager.getPlayer1();
        Player p2 = gameManager.getPlayer2();

        infoMeta.setLore(Arrays.asList(
                "§8Player 1 (Black): §7" + (p1 != null ? p1.getName() : "Offline"),
                "§fPlayer 2 (White): §7" + (p2 != null ? p2.getName() : "Offline"),
                "",
                "§eRules:",
                "§7Get 5 in a row to win!",
                "§760 seconds per move"
        ));
        playerInfo.setItemMeta(infoMeta);
        inventory.setItem(45, playerInfo);

        // Quit button (slot 53) - static
        ItemStack quit = new ItemStack(Material.BARRIER);
        ItemMeta quitMeta = quit.getItemMeta();
        quitMeta.setDisplayName("§cQuit Game");
        quitMeta.setLore(Arrays.asList("§7Click to quit the current game"));
        quit.setItemMeta(quitMeta);
        inventory.setItem(53, quit);

        // Fill borders with glass (slots around the board)
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);

        // Fill bottom row (slots 45-53 except 49)
        for (int i = 45; i <= 53; i++) {
            if (i != 49 && inventory.getItem(i) == null) {
                inventory.setItem(i, border.clone());
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

        // Check if clicked on quit button
        if (slot == 53) {
            player.performCommand("gobanggame quit");
            return;
        }

        // Check if clicked on board area (slots 0-44)
        if (slot >= 0 && slot <= BOARD_END_SLOT) {
            int row = slot / 9;
            int col = slot % 9;

            // Make sure it's a valid board position
            if (row < BOARD_SIZE && col < BOARD_SIZE) {
                // Make move
                boolean success = gameManager.makeMove(player, row, col);

                if (!success) {
                    player.sendMessage("§cIt's not your turn!");
                }
            }
        }
    }

    public void refreshForPlayer(Player player) {
        // Only refresh if player has this GUI open
        if (player.getOpenInventory().getTopInventory().getHolder() == this) {
            updateBoard();
            player.updateInventory(); // Update the inventory without closing/reopening
        }
    }
}