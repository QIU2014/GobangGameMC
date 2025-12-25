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
import java.util.Map;
import java.util.UUID;

public class InvitationGUI implements InventoryHolder {

    private final GobangGameMC plugin;
    private final Player viewer;
    private final Inventory inventory;

    public InvitationGUI(GobangGameMC plugin, Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.inventory = Bukkit.createInventory(this, 27, "§8Pending Invitations");
        loadInvitations();
    }

    private void loadInvitations() {
        inventory.clear();

        GameInvitationManager invitationManager = plugin.getInvitationManager();
        Map<UUID, GameInvitationManager.InvitationData> pendingInvitations =
                invitationManager.getPendingInvitations();

        int slot = 0;
        for (GameInvitationManager.InvitationData invitation : pendingInvitations.values()) {
            if (invitation.getReceiver().equals(viewer.getUniqueId())) {
                Player sender = Bukkit.getPlayer(invitation.getSender());
                if (sender != null && sender.isOnline()) {
                    ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta meta = (SkullMeta) head.getItemMeta();
                    meta.setOwningPlayer(sender);
                    meta.setDisplayName("§a" + sender.getName());
                    meta.setLore(Arrays.asList(
                            "§7Invited you to play Gobang",
                            "",
                            "§eClick to accept invitation",
                            "§7Or use command:",
                            "§a/gobanggame accept " + sender.getName()
                    ));
                    head.setItemMeta(meta);

                    inventory.setItem(slot, head);
                    slot++;

                    if (slot >= 27) break;
                }
            }
        }

        // Add back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§eBack to Menu");
        back.setItemMeta(backMeta);
        inventory.setItem(22, back);

        // Fill empty slots
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);

        for (int i = 0; i < 27; i++) {
            if (inventory.getItem(i) == null) {
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

        if (slot == 22) {
            // Back to menu
            viewer.closeInventory();
            new GobangMainMenu(plugin).open(viewer);
        } else if (slot < 27) {
            ItemStack item = event.getCurrentItem();
            if (item != null && item.getType() == Material.PLAYER_HEAD) {
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                if (meta != null && meta.getOwningPlayer() != null) {
                    Player sender = meta.getOwningPlayer().getPlayer();
                    if (sender != null) {
                        viewer.closeInventory();
                        viewer.performCommand("gobanggame accept " + sender.getName());
                    }
                }
            }
        }
    }
}