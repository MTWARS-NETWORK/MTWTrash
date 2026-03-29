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

public class ManagementGUI implements InventoryHolder {

    public static final String TITLE_PREFIX = "\u00A78Prullenbak: ";

    private final MTWTrash plugin;
    private final TrashCanData trashCan;
    private final Inventory inventory;

    public ManagementGUI(MTWTrash plugin, TrashCanData trashCan) {
        this.plugin = plugin;
        this.trashCan = trashCan;
        this.inventory = Bukkit.createInventory(this, 27, TITLE_PREFIX + trashCan.getId());
        build();
    }

    private void build() {
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "\u00A77");
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, border);
        }

        ItemStack lootBtn = createItem(Material.CHEST, "\u00A7aLoot bewerken",
                "\u00A77Beheer de loot items",
                "\u00A77voor deze prullenbak.");
        inventory.setItem(11, lootBtn);

        ItemStack cooldownBtn = createItem(Material.CLOCK, "\u00A7eCooldown instellen",
                "\u00A77Huidige cooldown: \u00A7e" + trashCan.getCooldownSeconds() + "s",
                "\u00A77Pas de cooldown aan.");
        inventory.setItem(15, cooldownBtn);
    }

    public static void open(MTWTrash plugin, Player player, TrashCanData trashCan) {
        ManagementGUI gui = new ManagementGUI(plugin, trashCan);
        player.openInventory(gui.getInventory());
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
