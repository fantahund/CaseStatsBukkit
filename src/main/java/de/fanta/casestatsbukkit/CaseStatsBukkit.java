package de.fanta.casestatsbukkit;

import de.cubeside.nmsutils.NMSUtils;
import de.fanta.casestatsbukkit.commands.CaseDeleteCommand;
import de.fanta.casestatsbukkit.commands.CaseEditCommand;
import de.fanta.casestatsbukkit.commands.CaseStatsLoginCommand;
import de.fanta.casestatsbukkit.data.CaseStatsBukkitConfig;
import de.fanta.casestatsbukkit.data.Database;
import de.iani.cubesideutils.bukkit.commands.CommandRouter;
import de.iani.playerUUIDCache.PlayerUUIDCache;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public final class CaseStatsBukkit extends JavaPlugin {

    private NMSUtils nmsUtils;
    private CaseStatsGlobalDataRequestManager globalDataRequestManager;
    private CaseStatsGlobalDataHelper globalDataHelper;
    private Collection<UUID> loginRequestList = new ArrayList<>();
    private Database database;
    private CaseStatsBukkitConfig caseStatsConfig;
    private static CaseStatsBukkit plugin;
    public PlayerUUIDCache playerUUIDCache;

    public static CaseStatsBukkit getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        nmsUtils = getServer().getServicesManager().load(NMSUtils.class);
        globalDataRequestManager = new CaseStatsGlobalDataRequestManager(this);
        globalDataHelper = new CaseStatsGlobalDataHelper(this);
        playerUUIDCache = (PlayerUUIDCache) Bukkit.getPluginManager().getPlugin("PlayerUUIDCache");

        this.caseStatsConfig = new CaseStatsBukkitConfig(this);

        this.database = new Database(caseStatsConfig.getSQLConfig(), this);

        CommandRouter router = new CommandRouter(this.getCommand("casestats"));
        router.addCommandMapping(new CaseStatsLoginCommand(this), "login");
        router.addCommandMapping(new CaseEditCommand(this), "editCase");
        router.addCommandMapping(new CaseDeleteCommand(this), "delete");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public CaseStatsGlobalDataRequestManager getGlobalDataRequestManager() {
        return globalDataRequestManager;
    }

    public CaseStatsGlobalDataHelper getGlobalDataHelper() {
        return globalDataHelper;
    }

    public Collection<UUID> getLoginRequestList() {
        return loginRequestList;
    }

    public Database getDatabase() {
        return database;
    }

    public CaseStatsBukkitConfig getCaseStatsConfig() {
        return caseStatsConfig;
    }

    public NMSUtils getNmsUtils() {
        return nmsUtils;
    }

    public PlayerUUIDCache getPlayerUUIDCache() {
        return playerUUIDCache;
    }
}
