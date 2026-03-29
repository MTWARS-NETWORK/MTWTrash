package nl.phoenixdev.mTWTrash.models;

import java.util.ArrayList;
import java.util.List;

public class TrashCanData {

    private final String id;
    private final String itemsAdderId;
    private List<LootItem> lootItems;
    private int cooldownSeconds;

    public TrashCanData(String id, String itemsAdderId, int cooldownSeconds) {
        this.id = id;
        this.itemsAdderId = itemsAdderId;
        this.cooldownSeconds = cooldownSeconds;
        this.lootItems = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getItemsAdderId() {
        return itemsAdderId;
    }

    public List<LootItem> getLootItems() {
        return lootItems;
    }

    public void setLootItems(List<LootItem> lootItems) {
        this.lootItems = lootItems;
    }

    public void addLootItem(LootItem item) {
        this.lootItems.add(item);
    }

    public void removeLootItem(int index) {
        if (index >= 0 && index < lootItems.size()) {
            lootItems.remove(index);
        }
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public List<LootItem> rollLoot() {
        List<LootItem> result = new ArrayList<>();
        for (LootItem lootItem : lootItems) {
            if (lootItem.roll()) {
                result.add(lootItem);
            }
        }
        return result;
    }
}
