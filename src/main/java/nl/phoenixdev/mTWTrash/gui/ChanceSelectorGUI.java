package nl.phoenixdev.mTWTrash.gui;

import nl.phoenixdev.mTWTrash.MTWTrash;
import nl.phoenixdev.mTWTrash.models.LootItem;
import nl.phoenixdev.mTWTrash.models.TrashCanData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ChanceSelectorGUI implements InventoryHolder {

    public static final String TITLE = "\u00A78Selecteer kans";

    private static final double[] CHANCES = {5.0, 10.0, 25.0, 50.0, 75.0, 90.0, 100.0};
    private static final int[] CHANCE_SLOTS = {10, 11, 12, 13, 14, 15, 16};

    private final MTWTrash plugin;
    private final TrashCanData trashCan;
    private final ItemStack pendingItem;
    private final Inventory inventory;

    public ChanceSelectorGUI(MTWTrash plugin, TrashCanData trashCan, ItemStack pendingItem) {
        this.plugin = plugin;
        this.trashCan = trashCan;
        this.pendingItem = pendingItem.clone();
        this.inventory = Bukkit.createInventory(this, 27, TITLE);
        build();
    }

    private void build() {
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "\u00A77");
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, border);
        }

        Material[] materials = {
                Material.RED_DYE,
                Material.ORANGE_DYE,
                Material.YELLOW_DYE,
                Material.LIME_DYE,
                Material.GREEN_DYE,
                Material.CYAN_DYE,
                Material.BLUE_DYE
        };

        for (int i = 0; i < CHANCES.length; i++) {
            ItemStack btn = createItem(materials[i],
                    "\u00A7e" + CHANCES[i] + "%",
                    "\u00A77Voeg dit item toe met",
                    "\u00A77een kans van \u00A7e" + CHANCES[i] + "%");
            inventory.setItem(CHANCE_SLOTS[i], btn);
        }
    }

    public static void open(MTWTrash plugin, Player player, TrashCanData trashCan, ItemStack pendingItem) {
        ChanceSelectorGUI gui = new ChanceSelectorGUI(plugin, trashCan, pendingItem);
        player.openInventory(gui.getInventory());
    }

    public static double getChanceForSlot(int slot) {
        for (int i = 0; i < CHANCE_SLOTS.length; i++) {
            if (CHANCE_SLOTS[i] == slot) return CHANCES[i];
        }
        return -1;
    }

    public static boolean isChanceSlot(int slot) {
        return getChanceForSlot(slot) >= 0;
    }

    public TrashCanData getTrashCan() {
        return trashCan;
    }

    public ItemStack getPendingItem() {
        return pendingItem.clone();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    private static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
