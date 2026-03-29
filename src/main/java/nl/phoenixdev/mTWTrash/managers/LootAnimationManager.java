package nl.phoenixdev.mTWTrash.managers;

import nl.phoenixdev.mTWTrash.MTWTrash;
import nl.phoenixdev.mTWTrash.models.LootItem;
import nl.phoenixdev.mTWTrash.tasks.LootRevealTask;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LootAnimationManager {

    private final MTWTrash plugin;
    private final Map<UUID, Inventory> activeAnimations = new HashMap<>();

    public LootAnimationManager(MTWTrash plugin) {
        this.plugin = plugin;
    }

    public void startAnimation(Player player, List<LootItem> rolledLoot) {
        Inventory inv = LootRevealTask.createAnimationInventory(plugin);
        activeAnimations.put(player.getUniqueId(), inv);
        player.openInventory(inv);

        LootRevealTask task = new LootRevealTask(plugin, player, inv, rolledLoot, null);
        task.start();
    }

    public boolean isAnimating(Player player) {
        return activeAnimations.containsKey(player.getUniqueId());
    }

    public Inventory getActiveInventory(Player player) {
        return activeAnimations.get(player.getUniqueId());
    }

    public void endAnimation(Player player) {
        activeAnimations.remove(player.getUniqueId());
    }

    public boolean isAnimationInventory(Inventory inventory) {
        return activeAnimations.containsValue(inventory);
    }
}
