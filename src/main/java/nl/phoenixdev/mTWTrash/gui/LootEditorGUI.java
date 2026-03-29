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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LootEditorGUI implements InventoryHolder {

    public static final String TITLE_PREFIX = "\u00A78Loot: ";
    private static final int SIZE = 54;
    private static final int[] LOOT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };
    private static final int ADD_SLOT = 49;
    private static final int BACK_SLOT = 45;

    private final MTWTrash plugin;
    private final TrashCanData trashCan;
    private final Inventory inventory;

    public LootEditorGUI(MTWTrash plugin, TrashCanData trashCan) {
        this.plugin = plugin;
        this.trashCan = trashCan;
        this.inventory = Bukkit.createInventory(this, SIZE, TITLE_PREFIX + trashCan.getId());
        build();
    }

    public void build() {
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "\u00A77");
        for (int i = 0; i < SIZE; i++) {
            inventory.setItem(i, border);
        }

        List<LootItem> lootItems = trashCan.getLootItems();
        for (int i = 0; i < lootItems.size() && i < LOOT_SLOTS.length; i++) {
            LootItem loot = lootItems.get(i);
            ItemStack display = loot.getItem().clone();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                List<String> existingLore = meta.hasLore() ? meta.getLore() : null;
                List<String> lore = existingLore != null ? new ArrayList<>(existingLore) : new ArrayList<>();
                lore.add("");
                lore.add("\u00A77Kans: \u00A7e" + String.format("%.1f", loot.getChance()) + "%");
                lore.add("\u00A7c\u00A7lLinks-klik om te verwijderen");
                meta.setLore(lore);
                display.setItemMeta(meta);
            }
            inventory.setItem(LOOT_SLOTS[i], display);
        }

        ItemStack addBtn = createItem(Material.LIME_DYE, "\u00A7aItem toevoegen",
                "\u00A77Houd een item vast en klik",
                "\u00A77om het toe te voegen als loot.");
        inventory.setItem(ADD_SLOT, addBtn);

        ItemStack backBtn = createItem(Material.ARROW, "\u00A7cTerug",
                "\u00A77Ga terug naar het beheer menu.");
        inventory.setItem(BACK_SLOT, backBtn);
    }

    public static void open(MTWTrash plugin, Player player, TrashCanData trashCan) {
        LootEditorGUI gui = new LootEditorGUI(plugin, trashCan);
        player.openInventory(gui.getInventory());
    }

    public static boolean isLootSlot(int slot) {
        for (int s : LOOT_SLOTS) {
            if (s == slot) return true;
        }
        return false;
    }

    public static int getLootIndex(int slot) {
        for (int i = 0; i < LOOT_SLOTS.length; i++) {
            if (LOOT_SLOTS[i] == slot) return i;
        }
        return -1;
    }

    public static int getAddSlot() {
        return ADD_SLOT;
    }

    public static int getBackSlot() {
        return BACK_SLOT;
    }

    public TrashCanData getTrashCan() {
        return trashCan;
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
