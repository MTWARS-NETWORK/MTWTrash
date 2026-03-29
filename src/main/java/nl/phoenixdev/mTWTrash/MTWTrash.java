package nl.phoenixdev.mTWTrash;

import nl.phoenixdev.mTWTrash.commands.TrashCommand;
import nl.phoenixdev.mTWTrash.listeners.GUIListener;
import nl.phoenixdev.mTWTrash.listeners.TrashCanListener;
import nl.phoenixdev.mTWTrash.managers.DatabaseManager;
import nl.phoenixdev.mTWTrash.managers.LootAnimationManager;
import nl.phoenixdev.mTWTrash.managers.TrashCanManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class MTWTrash extends JavaPlugin {

    private DatabaseManager databaseManager;
    private TrashCanManager trashCanManager;
    private LootAnimationManager lootAnimationManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        databaseManager = new DatabaseManager(this);
        if (!databaseManager.connect()) {
            getLogger().severe("Kon geen verbinding maken met de database. Plugin wordt uitgeschakeld.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        trashCanManager = new TrashCanManager(this);
        trashCanManager.load();

        lootAnimationManager = new LootAnimationManager(this);

        if (getServer().getPluginManager().getPlugin("ItemsAdder") != null) {
            getServer().getPluginManager().registerEvents(new TrashCanListener(this), this);
            getLogger().info("ItemsAdder gevonden - prullenbak listener actief.");
        } else {
            getLogger().warning("ItemsAdder niet gevonden! Prullenbak interactie is uitgeschakeld.");
        }

        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        TrashCommand trashCommand = new TrashCommand(this);
        var trashCmd = getCommand("trash");
        if (trashCmd != null) {
            trashCmd.setExecutor(trashCommand);
            trashCmd.setTabCompleter(trashCommand);
        }

        getLogger().info("MTWTrash v" + getDescription().getVersion() + " ingeschakeld.");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("MTWTrash uitgeschakeld.");
    }

    public String msg(String key) {
        String prefix = ChatColor.translateAlternateColorCodes('&',
                getConfig().getString("messages.prefix", "&8[&6MTWTrash&8] &r"));
        String message = getConfig().getString("messages." + key, "&c[Ontbrekende bericht: " + key + "]");
        return prefix + ChatColor.translateAlternateColorCodes('&', message);
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public TrashCanManager getTrashCanManager() {
        return trashCanManager;
    }

    public LootAnimationManager getLootAnimationManager() {
        return lootAnimationManager;
    }
}
