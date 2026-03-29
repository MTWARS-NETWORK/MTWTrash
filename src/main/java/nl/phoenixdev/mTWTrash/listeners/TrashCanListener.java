package nl.phoenixdev.mTWTrash.listeners;

import dev.lone.itemsadder.api.Events.FurnitureInteractEvent;
import nl.phoenixdev.mTWTrash.MTWTrash;
import nl.phoenixdev.mTWTrash.gui.ManagementGUI;
import nl.phoenixdev.mTWTrash.models.LootItem;
import nl.phoenixdev.mTWTrash.models.TrashCanData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;

public class TrashCanListener implements Listener {

    private final MTWTrash plugin;

    public TrashCanListener(MTWTrash plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFurnitureInteract(FurnitureInteractEvent event) {
        Player player = event.getPlayer();
        String itemsAdderId = event.getNamespacedID();
        Location furnitureLoc = event.getBukkitEntity().getLocation();

        if (!plugin.getTrashCanManager().getConfiguredItemsAdderIds().contains(itemsAdderId)) {
            return;
        }

        TrashCanData trashCan = plugin.getTrashCanManager().getByLocation(furnitureLoc);
        if (trashCan == null) {
            return;
        }

        event.setCancelled(true);

        if (player.isSneaking() && player.hasPermission("mtwtrash.admin")) {
            ManagementGUI.open(plugin, player, trashCan);
            return;
        }

        if (!player.hasPermission("mtwtrash.use")) {
            player.sendMessage(plugin.msg("no-permission"));
            return;
        }

        if (plugin.getLootAnimationManager().isAnimating(player)) {
            return;
        }

        long lastUsed = plugin.getDatabaseManager().getCooldown(player.getUniqueId(), trashCan.getId());
        long cooldownMs = (long) trashCan.getCooldownSeconds() * 1000L;
        long remaining = (lastUsed + cooldownMs) - System.currentTimeMillis();

        if (remaining > 0) {
            String timeStr = formatTime(remaining);
            player.sendMessage(plugin.msg("cooldown").replace("{time}", timeStr));
            return;
        }

        List<LootItem> rolled = trashCan.rollLoot();
        if (rolled.isEmpty()) {
            player.sendMessage(plugin.msg("no-loot"));
            plugin.getDatabaseManager().setCooldown(player.getUniqueId(), trashCan.getId());
            return;
        }

        plugin.getDatabaseManager().setCooldown(player.getUniqueId(), trashCan.getId());
        plugin.getLootAnimationManager().startAnimation(player, rolled);
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        if (seconds < 60) {
            return seconds + "s";
        }
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        if (minutes < 60) {
            return minutes + "m " + remainingSeconds + "s";
        }
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        return hours + "u " + remainingMinutes + "m";
    }
}
