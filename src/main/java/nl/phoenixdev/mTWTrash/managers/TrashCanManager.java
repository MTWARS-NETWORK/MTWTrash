package nl.phoenixdev.mTWTrash.managers;

import nl.phoenixdev.mTWTrash.MTWTrash;
import nl.phoenixdev.mTWTrash.models.LootItem;
import nl.phoenixdev.mTWTrash.models.TrashCanData;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class TrashCanManager {

    private final MTWTrash plugin;
    private final Map<String, TrashCanData> trashCans = new HashMap<>();
    private File trashcansFile;
    private FileConfiguration trashcansConfig;

    public TrashCanManager(MTWTrash plugin) {
        this.plugin = plugin;
    }

    public void load() {
        trashcansFile = new File(plugin.getDataFolder(), "trashcans.yml");
        if (!trashcansFile.exists()) {
            plugin.saveResource("trashcans.yml", false);
        }
        trashcansConfig = YamlConfiguration.loadConfiguration(trashcansFile);
        trashCans.clear();

        ConfigurationSection section = trashcansConfig.getConfigurationSection("trashcans");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            ConfigurationSection canSection = section.getConfigurationSection(id);
            if (canSection == null) continue;

            String iaId = canSection.getString("itemsadder-id", "unknown");
            int cooldown = canSection.getInt("cooldown", plugin.getConfig().getInt("settings.default-cooldown", 300));

            TrashCanData data = new TrashCanData(id, iaId, cooldown);

            List<?> lootList = canSection.getList("loot");
            if (lootList != null) {
                for (Object obj : lootList) {
                    if (obj instanceof Map<?, ?> map) {
                        try {
                            ItemStack item = (ItemStack) map.get("item");
                            Object chanceObj = map.get("chance");
                            double chance = chanceObj instanceof Number n ? n.doubleValue() : 50.0;
                            if (item != null) {
                                data.addLootItem(new LootItem(item, chance));
                            }
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.WARNING, "Ongeldige loot entry voor prullenbak " + id, e);
                        }
                    }
                }
            }

            trashCans.put(id, data);
        }

        plugin.getLogger().info("Geladen: " + trashCans.size() + " prullenbak(ken).");
    }

    public void save() {
        trashcansConfig.set("trashcans", null);

        for (TrashCanData data : trashCans.values()) {
            String path = "trashcans." + data.getId();
            trashcansConfig.set(path + ".itemsadder-id", data.getItemsAdderId());
            trashcansConfig.set(path + ".cooldown", data.getCooldownSeconds());

            List<Map<String, Object>> lootList = new ArrayList<>();
            for (LootItem lootItem : data.getLootItems()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("item", lootItem.getItem());
                entry.put("chance", lootItem.getChance());
                lootList.add(entry);
            }
            trashcansConfig.set(path + ".loot", lootList);
        }

        try {
            trashcansConfig.save(trashcansFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Kon trashcans.yml niet opslaan: " + e.getMessage(), e);
        }
    }

    public String locationToId(Location location) {
        return location.getWorld().getName()
                + ":" + location.getBlockX()
                + ":" + location.getBlockY()
                + ":" + location.getBlockZ();
    }

    public TrashCanData getByLocation(Location location) {
        return trashCans.get(locationToId(location));
    }

    public TrashCanData getById(String id) {
        return trashCans.get(id);
    }

    public boolean isTrashCan(Location location) {
        return trashCans.containsKey(locationToId(location));
    }

    public TrashCanData createTrashCan(Location location, String itemsAdderId) {
        String id = locationToId(location);
        int defaultCooldown = plugin.getConfig().getInt("settings.default-cooldown", 300);
        TrashCanData data = new TrashCanData(id, itemsAdderId, defaultCooldown);
        trashCans.put(id, data);
        save();
        return data;
    }

    public boolean removeTrashCan(String id) {
        if (trashCans.remove(id) != null) {
            save();
            return true;
        }
        return false;
    }

    public Collection<TrashCanData> getAll() {
        return trashCans.values();
    }

    public List<String> getConfiguredItemsAdderIds() {
        return plugin.getConfig().getStringList("settings.itemsadder-ids");
    }
}
