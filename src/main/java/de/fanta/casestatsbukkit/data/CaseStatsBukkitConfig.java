package de.fanta.casestatsbukkit.data;

import de.fanta.casestatsbukkit.CaseStatsBukkit;
import de.iani.cubesideutils.bukkit.sql.SQLConfigBukkit;
import de.iani.cubesideutils.sql.SQLConfig;
import org.bukkit.configuration.file.FileConfiguration;

public class CaseStatsBukkitConfig {
    private final CaseStatsBukkit plugin;

    private SQLConfig sqlConfig;

    public CaseStatsBukkitConfig(CaseStatsBukkit plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        reloadConfig();
    }

    public void reloadConfig() {
        FileConfiguration config = plugin.getConfig();
        config.options().copyDefaults(true);
        plugin.saveConfig();

        sqlConfig = new SQLConfigBukkit(config.getConfigurationSection("database"));
    }

    public SQLConfig getSQLConfig() {
        return sqlConfig;
    }
}
