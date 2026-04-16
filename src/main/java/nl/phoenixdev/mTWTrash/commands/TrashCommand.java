package nl.phoenixdev.mTWTrash.commands;

import dev.lone.itemsadder.api.CustomFurniture;
import nl.phoenixdev.mTWTrash.MTWTrash;
import nl.phoenixdev.mTWTrash.gui.CooldownEditorGUI;
import nl.phoenixdev.mTWTrash.gui.LootEditorGUI;
import nl.phoenixdev.mTWTrash.gui.ManagementGUI;
import nl.phoenixdev.mTWTrash.managers.TrashCanManager;
import nl.phoenixdev.mTWTrash.models.TrashCanData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class TrashCommand implements CommandExecutor, TabCompleter {

    private final MTWTrash plugin;

    public TrashCommand(MTWTrash plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mtwtrash.admin")) {
            sender.sendMessage(plugin.msg("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "create" -> handleCreate(sender);
            case "delete" -> handleDelete(sender, args);
            case "loot" -> handleLoot(sender, args);
            case "cooldown" -> handleCooldown(sender, args);
            case "list" -> handleList(sender);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        plugin.reloadConfig();
        plugin.getTrashCanManager().load();
        sender.sendMessage(plugin.msg("reload"));
    }

    private void handleCreate(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("\u00A7cDit commando kan alleen door spelers worden uitgevoerd.");
            return;
        }

        Location location = player.getLocation().add(0, -1, 0);
        Block block = location.getBlock();
        if (block == null) {
            return;
        }

        if (!block.getType().isSolid()) {
            player.sendMessage(ChatColor.RED + "Je moet op een blok staan!");
            return;
        }

        CustomFurniture furniture = CustomFurniture.spawnPreciseNonSolid("saturnstudio:garbage_pack_silver_trashcan", player.getLocation());
        if (furniture == null) {
            System.out.println(2);
            player.sendMessage(plugin.msg("not-looking-at-trash"));
            return;
        }

        String iaId = furniture.getNamespacedID();
        if (!plugin.getTrashCanManager().getConfiguredItemsAdderIds().contains(iaId)) {
            player.sendMessage(plugin.msg("prefix") + "\u00A7cDit meubel (\u00A7e" + iaId + "\u00A7c) staat niet in de lijst van prullenbak-IDs in config.yml.");
            return;
        }

        Location loc = player.getLocation();
        TrashCanData existing = plugin.getTrashCanManager().getByLocation(loc);
        if (existing != null) {
            player.sendMessage(plugin.msg("prefix") + "\u00A7cEr bestaat al een prullenbak op deze locatie: \u00A7e" + existing.getId());
            return;
        }

        TrashCanData created = plugin.getTrashCanManager().createTrashCan(loc, iaId);
        player.sendMessage(plugin.msg("trashcan-created").replace("{id}", created.getId()));
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            if (args.length < 2) {
                sender.sendMessage("\u00A7cGebruik: /trash delete <id>");
                return;
            }
            boolean removed = plugin.getTrashCanManager().removeTrashCan(args[1]);
            sender.sendMessage(removed
                    ? plugin.msg("trashcan-deleted").replace("{id}", args[1])
                    : plugin.msg("trashcan-not-found"));
            return;
        }

        String idToDelete;
        if (args.length >= 2) {
            idToDelete = args[1];
        } else {
            Entity nearby = getNearbyFurniture(player);
            if (nearby == null) {
                player.sendMessage(plugin.msg("not-looking-at-trash"));
                return;
            }
            idToDelete = plugin.getTrashCanManager().locationToId(nearby.getLocation());
        }

        boolean removed = plugin.getTrashCanManager().removeTrashCan(idToDelete);
        player.sendMessage(removed
                ? plugin.msg("trashcan-deleted").replace("{id}", idToDelete)
                : plugin.msg("trashcan-not-found"));
    }

    private void handleLoot(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("\u00A7cDit commando kan alleen door spelers worden uitgevoerd.");
            return;
        }

        TrashCanData data = resolveTrashCan(player, args, 1);
        if (data == null) return;

        LootEditorGUI.open(plugin, player, data);
    }

    private void handleCooldown(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("\u00A7cDit commando kan alleen door spelers worden uitgevoerd.");
            return;
        }

        TrashCanData data = resolveTrashCan(player, args, 1);
        if (data == null) return;

        CooldownEditorGUI.open(plugin, player, data);
    }

    private void handleList(CommandSender sender) {
        var all = plugin.getTrashCanManager().getAll();
        if (all.isEmpty()) {
            sender.sendMessage(plugin.msg("prefix") + "\u00A77Geen prullenbakken geregistreerd.");
            return;
        }
        sender.sendMessage(plugin.msg("prefix") + "\u00A7aGeregistreerde prullenbakken (" + all.size() + "):");
        for (TrashCanData data : all) {
            sender.sendMessage("\u00A77 - \u00A7e" + data.getId()
                    + "\u00A77 IA: \u00A7f" + data.getItemsAdderId()
                    + "\u00A77 Cooldown: \u00A7f" + data.getCooldownSeconds() + "s"
                    + "\u00A77 Loot: \u00A7f" + data.getLootItems().size());
        }
    }

    private TrashCanData resolveTrashCan(Player player, String[] args, int idArgIndex) {
        if (args.length > idArgIndex) {
            String id = args[idArgIndex];
            TrashCanData data = plugin.getTrashCanManager().getById(id);
            if (data == null) {
                player.sendMessage(plugin.msg("trashcan-not-found"));
            }
            return data;
        }

        Entity nearby = getNearbyFurniture(player);
        if (nearby == null) {
            player.sendMessage(plugin.msg("not-looking-at-trash"));
            return null;
        }

        TrashCanData data = plugin.getTrashCanManager().getByLocation(nearby.getLocation());
        if (data == null) {
            player.sendMessage(plugin.msg("trashcan-not-found"));
        }
        return data;
    }

    private Entity getNearbyFurniture(Player player) {
        Location loc = player.getLocation();
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 3, 3, 3)) {
            if (entity instanceof Player) {
                continue;
            }
            CustomFurniture furniture = CustomFurniture.byAlreadySpawned(entity);
            System.out.println(entity);
            if (furniture != null) {
                String iaId = furniture.getNamespacedID();
                if (plugin.getTrashCanManager().getConfiguredItemsAdderIds().contains(iaId)) {
                    return entity;
                }
            }
        }
        return null;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("\u00A78[\u00A76MTWTrash\u00A78] \u00A7aCommando's:");
        sender.sendMessage("\u00A7e/trash create \u00A77- Registreer nabijgelegen prullenbak");
        sender.sendMessage("\u00A7e/trash delete [id] \u00A77- Verwijder prullenbak");
        sender.sendMessage("\u00A7e/trash loot [id] \u00A77- Bewerk loot van prullenbak");
        sender.sendMessage("\u00A7e/trash cooldown [id] \u00A77- Pas cooldown aan van prullenbak");
        sender.sendMessage("\u00A7e/trash list \u00A77- Toon alle prullenbakken");
        sender.sendMessage("\u00A7e/trash reload \u00A77- Herlaad configuratie");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("mtwtrash.admin")) return List.of();
        if (args.length == 1) {
            return Arrays.asList("create", "delete", "loot", "cooldown", "list", "reload");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("delete")
                || args[0].equalsIgnoreCase("loot")
                || args[0].equalsIgnoreCase("cooldown"))) {
            return plugin.getTrashCanManager().getAll().stream()
                    .map(TrashCanData::getId)
                    .toList();
        }
        return List.of();
    }
}
