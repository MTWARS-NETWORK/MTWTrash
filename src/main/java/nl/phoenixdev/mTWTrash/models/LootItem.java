package nl.phoenixdev.mTWTrash.models;

import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class LootItem {

    private static final Random RANDOM = new Random();

    private final ItemStack item;
    private final double chance;

    public LootItem(ItemStack item, double chance) {
        this.item = item.clone();
        this.chance = chance;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public double getChance() {
        return chance;
    }

    public boolean roll() {
        return RANDOM.nextDouble() * 100.0 <= chance;
    }
}
