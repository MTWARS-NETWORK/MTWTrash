package nl.phoenixdev.mTWTrash.tasks;

import nl.phoenixdev.mTWTrash.MTWTrash;
import nl.phoenixdev.mTWTrash.models.LootItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class LootRevealTask {

    public static final String INVENTORY_TITLE = "\u00A78Prullenbak graaien...";
    public static final int INVENTORY_SIZE = 27;
    private static final int[] LOOT_SLOTS = {9, 10, 11, 12, 13, 14, 15, 16, 17};

    private final MTWTrash plugin;
    private final Player player;
    private final Inventory inventory;
    private final List<LootItem> rolledLoot;
    private final Runnable onComplete;

    public LootRevealTask(MTWTrash plugin, Player player, Inventory inventory,
                          List<LootItem> rolledLoot, Runnable onComplete) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = inventory;
        this.rolledLoot = rolledLoot;
        this.onComplete = onComplete;
    }

    public void start() {
        revealItem(0);
    }

    private void revealItem(int index) {
        if (!player.isOnline()) return;
        if (index >= rolledLoot.size()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        int slot = LOOT_SLOTS[index];
        int paneDelay = plugin.getConfig().getInt("settings.animation-pane-delay", 5);
        int revealDelay = plugin.getConfig().getInt("settings.animation-reveal-delay", 20);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            if (player.getOpenInventory().getTopInventory() != inventory) return;

            inventory.setItem(slot, rolledLoot.get(index).getItem());
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);

            Bukkit.getScheduler().runTaskLater(plugin, () -> revealItem(index + 1), revealDelay);
        }, paneDelay);
    }

    public static Inventory createAnimationInventory(MTWTrash plugin) {
        Inventory inv = Bukkit.createInventory(null, INVENTORY_SIZE, INVENTORY_TITLE);
        ItemStack pane = createGlassPane();
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inv.setItem(i, pane);
        }
        return inv;
    }

    public static boolean isLootSlot(int slot) {
        for (int lootSlot : LOOT_SLOTS) {
            if (lootSlot == slot) return true;
        }
        return false;
    }

    public static boolean isDecorationSlot(int slot) {
        return !isLootSlot(slot);
    }

    public static boolean isGlassPane(ItemStack item) {
        if (item == null) return false;
        if (item.getType() != Material.GRAY_STAINED_GLASS_PANE) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName()
                && meta.getDisplayName().equals("\u00A77");
    }

    private static ItemStack createGlassPane() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("\u00A77");
            pane.setItemMeta(meta);
        }
        return pane;
    }
}
