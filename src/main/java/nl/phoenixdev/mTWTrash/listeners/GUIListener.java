package nl.phoenixdev.mTWTrash.listeners;

import nl.phoenixdev.mTWTrash.MTWTrash;
import nl.phoenixdev.mTWTrash.gui.ChanceSelectorGUI;
import nl.phoenixdev.mTWTrash.gui.CooldownEditorGUI;
import nl.phoenixdev.mTWTrash.gui.LootEditorGUI;
import nl.phoenixdev.mTWTrash.gui.ManagementGUI;
import nl.phoenixdev.mTWTrash.models.LootItem;
import nl.phoenixdev.mTWTrash.tasks.LootRevealTask;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final MTWTrash plugin;

    public GUIListener(MTWTrash plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof ManagementGUI gui) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot == 11) {
                LootEditorGUI.open(plugin, player, gui.getTrashCan());
            } else if (slot == 15) {
                CooldownEditorGUI.open(plugin, player, gui.getTrashCan());
            }
            return;
        }

        if (holder instanceof LootEditorGUI gui) {
            event.setCancelled(true);
            int slot = event.getRawSlot();

            if (slot == LootEditorGUI.getBackSlot()) {
                ManagementGUI.open(plugin, player, gui.getTrashCan());
                return;
            }

            if (slot == LootEditorGUI.getAddSlot()) {
                ItemStack held = player.getInventory().getItemInMainHand();
                if (held.getType().isAir()) {
                    held = player.getInventory().getItemInOffHand();
                }
                if (!held.getType().isAir()) {
                    if (gui.getTrashCan().getLootItems().size() >= 21) {
                        player.sendMessage(plugin.msg("prefix") + "\u00A7cMaximum aantal loot items bereikt (21).");
                        return;
                    }
                    ChanceSelectorGUI.open(plugin, player, gui.getTrashCan(), held);
                }
                return;
            }

            if (LootEditorGUI.isLootSlot(slot)) {
                int index = LootEditorGUI.getLootIndex(slot);
                if (index >= 0 && index < gui.getTrashCan().getLootItems().size()) {
                    gui.getTrashCan().removeLootItem(index);
                    plugin.getTrashCanManager().save();
                    LootEditorGUI.open(plugin, player, gui.getTrashCan());
                }
            }
            return;
        }

        if (holder instanceof ChanceSelectorGUI gui) {
            event.setCancelled(true);
            int slot = event.getRawSlot();

            if (ChanceSelectorGUI.isChanceSlot(slot)) {
                double chance = ChanceSelectorGUI.getChanceForSlot(slot);
                ItemStack item = gui.getPendingItem();
                gui.getTrashCan().addLootItem(new LootItem(item, chance));
                plugin.getTrashCanManager().save();
                player.sendMessage(plugin.msg("prefix") + "\u00A7aItem toegevoegd met \u00A7e" + chance + "% \u00A7akans.");
                LootEditorGUI.open(plugin, player, gui.getTrashCan());
            }
            return;
        }

        if (holder instanceof CooldownEditorGUI gui) {
            event.setCancelled(true);
            int slot = event.getRawSlot();

            if (slot == CooldownEditorGUI.getSaveSlot()) {
                gui.save();
                player.sendMessage(plugin.msg("prefix") + "\u00A7aCooldown opgeslagen.");
                ManagementGUI.open(plugin, player, gui.getTrashCan());
                return;
            }

            if (slot == CooldownEditorGUI.getBackSlot()) {
                ManagementGUI.open(plugin, player, gui.getTrashCan());
                return;
            }

            gui.handleClick(slot);
            return;
        }

        if (plugin.getLootAnimationManager().isAnimationInventory(event.getInventory())) {
            int slot = event.getRawSlot();
            if (slot >= LootRevealTask.INVENTORY_SIZE) return;

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) {
                event.setCancelled(true);
                return;
            }
            if (LootRevealTask.isGlassPane(clicked)) {
                event.setCancelled(true);
                return;
            }
            if (LootRevealTask.isDecorationSlot(slot)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof ManagementGUI
                || holder instanceof LootEditorGUI
                || holder instanceof ChanceSelectorGUI
                || holder instanceof CooldownEditorGUI
                || plugin.getLootAnimationManager().isAnimationInventory(event.getInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        Inventory inv = event.getInventory();

        if (plugin.getLootAnimationManager().isAnimationInventory(inv)) {
            plugin.getLootAnimationManager().endAnimation(player);
            for (int slot = 9; slot <= 17; slot++) {
                ItemStack item = inv.getItem(slot);
                if (item != null && !item.getType().isAir() && !LootRevealTask.isGlassPane(item)) {
                    for (ItemStack leftover : player.getInventory().addItem(item).values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                    }
                }
            }
        }
    }
}
