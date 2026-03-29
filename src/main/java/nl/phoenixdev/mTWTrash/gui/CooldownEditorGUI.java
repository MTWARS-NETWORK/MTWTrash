package nl.phoenixdev.mTWTrash.gui;

import nl.phoenixdev.mTWTrash.MTWTrash;
import nl.phoenixdev.mTWTrash.models.TrashCanData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class CooldownEditorGUI implements InventoryHolder {

    public static final String TITLE_PREFIX = "\u00A78Cooldown: ";

    private static final int DECREASE_5M_SLOT = 10;
    private static final int DECREASE_1M_SLOT = 11;
    private static final int DECREASE_30S_SLOT = 12;
    private static final int DISPLAY_SLOT = 13;
    private static final int INCREASE_30S_SLOT = 14;
    private static final int INCREASE_1M_SLOT = 15;
    private static final int INCREASE_5M_SLOT = 16;
    private static final int SAVE_SLOT = 22;
    private static final int BACK_SLOT = 18;

    private final MTWTrash plugin;
    private final TrashCanData trashCan;
    private final Inventory inventory;
    private int currentCooldown;

    public CooldownEditorGUI(MTWTrash plugin, TrashCanData trashCan) {
        this.plugin = plugin;
        this.trashCan = trashCan;
        this.currentCooldown = trashCan.getCooldownSeconds();
        this.inventory = Bukkit.createInventory(this, 27, TITLE_PREFIX + trashCan.getId());
        build();
    }

    public void build() {
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "\u00A77");
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, border);
        }

        inventory.setItem(DECREASE_5M_SLOT, createItem(Material.RED_CONCRETE, "\u00A7c- 5 minuten"));
        inventory.setItem(DECREASE_1M_SLOT, createItem(Material.ORANGE_CONCRETE, "\u00A7c- 1 minuut"));
        inventory.setItem(DECREASE_30S_SLOT, createItem(Material.YELLOW_CONCRETE, "\u00A7e- 30 seconden"));

        updateDisplay();

        inventory.setItem(INCREASE_30S_SLOT, createItem(Material.LIME_CONCRETE, "\u00A7a+ 30 seconden"));
        inventory.setItem(INCREASE_1M_SLOT, createItem(Material.GREEN_CONCRETE, "\u00A7a+ 1 minuut"));
        inventory.setItem(INCREASE_5M_SLOT, createItem(Material.CYAN_CONCRETE, "\u00A7a+ 5 minuten"));

        inventory.setItem(SAVE_SLOT, createItem(Material.EMERALD, "\u00A7aOpslaan",
                "\u00A77Sla de cooldown op."));
        inventory.setItem(BACK_SLOT, createItem(Material.ARROW, "\u00A7cTerug",
                "\u00A77Ga terug zonder opslaan."));
    }

    private void updateDisplay() {
        String formatted = formatTime(currentCooldown);
        ItemStack display = createItem(Material.CLOCK,
                "\u00A7eCooldown: \u00A7f" + formatted,
                "\u00A77Huidige waarde: \u00A7e" + currentCooldown + " seconden");
        inventory.setItem(DISPLAY_SLOT, display);
    }

    public void handleClick(int slot) {
        switch (slot) {
            case DECREASE_5M_SLOT -> adjustCooldown(-300);
            case DECREASE_1M_SLOT -> adjustCooldown(-60);
            case DECREASE_30S_SLOT -> adjustCooldown(-30);
            case INCREASE_30S_SLOT -> adjustCooldown(30);
            case INCREASE_1M_SLOT -> adjustCooldown(60);
            case INCREASE_5M_SLOT -> adjustCooldown(300);
        }
    }

    private void adjustCooldown(int delta) {
        currentCooldown = Math.max(10, currentCooldown + delta);
        updateDisplay();
    }

    public void save() {
        trashCan.setCooldownSeconds(currentCooldown);
        plugin.getTrashCanManager().save();
    }

    public static void open(MTWTrash plugin, Player player, TrashCanData trashCan) {
        CooldownEditorGUI gui = new CooldownEditorGUI(plugin, trashCan);
        player.openInventory(gui.getInventory());
    }

    public static int getSaveSlot() {
        return SAVE_SLOT;
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

    private static String formatTime(int seconds) {
        if (seconds < 60) return seconds + "s";
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        if (minutes < 60) return minutes + "m " + remainingSeconds + "s";
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        return hours + "u " + remainingMinutes + "m";
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
