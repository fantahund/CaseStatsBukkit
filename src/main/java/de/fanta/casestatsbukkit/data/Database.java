package de.fanta.casestatsbukkit.data;

import de.cubeside.nmsutils.nbt.CompoundTag;
import de.fanta.casestatsbukkit.CaseStatsBukkit;
import de.fanta.casestatsbukkit.utils.ItemUtils;
import de.iani.cubesideutils.sql.MySQLConnection;
import de.iani.cubesideutils.sql.SQLConfig;
import de.iani.cubesideutils.sql.SQLConnection;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class Database {

    private final SQLConnection connection;
    private final SQLConfig config;
    private final CaseStatsBukkit plugin;
    private final String insertCaseQuery;
    private final String insertCaseItemQuery;
    private final String insertPlayerStatQuery;
    private final String selectCasesQuery;
    private final String selectCaseItemsQuery;
    private final String deleteCaseItemQuary;
    private final String updateCaseItemQuary;
    private final String insertCase2CaseItemQuery;
    private final String selectCaseItemIdQuery;
    private final String selectCase2CaseItemQuery;
    private final String selectPlayerCaseStats;
    private final String deleteCaseQuary;

    public Database(SQLConfig config, CaseStatsBukkit plugin) {
        this.config = config;
        this.plugin = plugin;

        try {
            this.connection = new MySQLConnection(config.getHost(), config.getDatabase(), config.getUser(), config.getPassword());

            createTablesIfNotExist();
        } catch (SQLException ex) {
            throw new RuntimeException("Could not initialize database", ex);
        }

        this.insertCaseQuery = "INSERT INTO " + config.getTablePrefix() + "_cases(id, item, itemNBT, nbtVersion) VALUE (?, ?, ?, ?) ON DUPLICATE KEY UPDATE id = ?, item = ?, itemNBT = ?, nbtVersion = ?";
        this.selectCaseItemIdQuery = "SELECT id FROM " + config.getTablePrefix() + "_case_items WHERE item = ? AND amount = ? AND itemNBT = ?";
        this.insertCaseItemQuery = "INSERT INTO " + config.getTablePrefix() + "_case_items(item, amount, itemNBT, nbtVersion) VALUE (?, ?, ?, ?)";
        this.selectCase2CaseItemQuery = "SELECT id FROM " + config.getTablePrefix() + "_case2caseitems WHERE caseId = ? AND caseItemId = ?";
        this.insertCase2CaseItemQuery = "INSERT INTO " + config.getTablePrefix() + "_case2caseitems(caseId, caseItemId) VALUE (?, ?)";
        this.insertPlayerStatQuery = "INSERT INTO " + config.getTablePrefix() + "_player_stats(uuid, case2caseitemId, timestamp) VALUE (?, ?, ?)";
        this.selectCasesQuery = "SELECT * FROM " + config.getTablePrefix() + "_cases";

        this.selectCaseItemsQuery = "SELECT " + config.getTablePrefix() + "_case_items.id itemId, " + config.getTablePrefix() + "_case_items.item item, " + config.getTablePrefix() + "_case_items.amount amount, " + config.getTablePrefix() + "_case_items.itemNBT itemNBT FROM " + config.getTablePrefix() + "_case2caseitems INNER JOIN " + config.getTablePrefix() + "_case_items ON " + config.getTablePrefix() + "_case_items.id=" + config.getTablePrefix() + "_case2caseitems.caseItemId WHERE " + config.getTablePrefix() + "_case2caseitems.caseId = ?";
        this.selectPlayerCaseStats = "SELECT casestats_player_stats.uuid UUID, casestats_case_items.id itemId, casestats_case_items.item, casestats_case_items.amount amount, casestats_case_items.itemNBT itemNBT, casestats_case_items.nbtVersion nbtVersion, count(casestats_case2caseitems.caseItemId) itemCount FROM casestats_player_stats INNER JOIN casestats_case2caseitems ON case2caseitemId=casestats_case2caseitems.id INNER JOIN casestats_case_items ON casestats_case2caseitems.caseItemId = casestats_case_items.id WHERE casestats_case2caseitems.caseId = ? AND uuid IN (%s) GROUP BY UUID, casestats_case2caseitems.caseItemId";
        this.deleteCaseItemQuary = "DELETE FROM " + config.getTablePrefix() + "_case_items WHERE `id` = ?";
        this.updateCaseItemQuary = "UPDATE " + config.getTablePrefix() + "_case_items SET `item`= ?,`amount`= ?,`itemNBT`= ? WHERE `id` = ?";
        this.deleteCaseQuary = "DELETE FROM " + config.getTablePrefix() + "_cases WHERE `id` = ?";
    }

    private void createTablesIfNotExist() throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            Statement smt = connection.createStatement();
            smt.executeUpdate("CREATE TABLE IF NOT EXISTS " + config.getTablePrefix() + "_cases" + " (" +
                    "`id` VARCHAR(255)," +
                    "`item` TINYTEXT," +
                    "`itemNBT` LONGTEXT," +
                    "`nbtVersion` INT," +
                    "PRIMARY KEY (id)" +
                    ")"
            );
            smt.executeUpdate("CREATE TABLE IF NOT EXISTS " + config.getTablePrefix() + "_case_items" + " (" +
                    "`id` INT NOT NULL AUTO_INCREMENT," +
                    "`item` TINYTEXT," +
                    "`amount` INT," +
                    "`itemNBT` LONGTEXT," +
                    "`nbtVersion` INT," +
                    "CONSTRAINT item_identifier UNIQUE (item, amount, itemNBT, nbtVersion)," +
                    "PRIMARY KEY (id)" +
                    ")"
            );

            smt.executeUpdate("CREATE TABLE IF NOT EXISTS " + config.getTablePrefix() + "_case2caseitems" + " (" +
                    "`id` INT NOT NULL AUTO_INCREMENT," +
                    "`caseId` VARCHAR(255)," +
                    "`caseItemId` INT," +
                    "CONSTRAINT identifier UNIQUE (caseId, caseItemId)," +
                    "PRIMARY KEY (id)" +
                    ")"
            );
            smt.executeUpdate("CREATE TABLE IF NOT EXISTS " + config.getTablePrefix() + "_player_stats" + " (" +
                    "`id` INT NOT NULL AUTO_INCREMENT," +
                    "`uuid` char(36)," +
                    "`case2caseitemId` INT," +
                    "`timestamp` BIGINT," +
                    "PRIMARY KEY (id)" +
                    ")"
            );
            smt.close();
            return null;
        });
    }

    public void insertCase(String caseId, String item, String itemNBT) throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(insertCaseQuery);
            smt.setString(1, caseId);
            smt.setString(2, item);
            smt.setString(3, itemNBT);
            smt.setInt(4, plugin.getNmsUtils().getNbtUtils().getCurrentDataVersion());

            smt.setString(5, caseId);
            smt.setString(6, item);
            smt.setString(7, itemNBT);
            smt.setInt(8, plugin.getNmsUtils().getNbtUtils().getCurrentDataVersion());

            smt.executeUpdate();
            return null;
        });
    }

    public int insertCaseItem(String item, int amount, String itemNBT) throws SQLException {
        int caseItemId = selectCaseItemId(item, amount, itemNBT);
        if (caseItemId != 0) {
            return caseItemId;
        }

        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(insertCaseItemQuery, Statement.RETURN_GENERATED_KEYS);
            smt.setString(1, item);
            smt.setInt(2, amount);
            smt.setString(3, itemNBT);
            smt.setInt(4, plugin.getNmsUtils().getNbtUtils().getCurrentDataVersion());
            smt.executeUpdate();
            ResultSet rs = smt.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : 0;
        });
    }

    public int selectCaseItemId(String item, int amount, String itemNBT) throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(selectCaseItemIdQuery);
            smt.setString(1, item);
            smt.setInt(2, amount);
            smt.setString(3, itemNBT);
            ResultSet rs = smt.executeQuery();
            return rs.next() ? rs.getInt("id") : 0;
        });
    }

    public int insertCase2CaseItem(String caseId, int itemId) throws SQLException {
        int case2CaseItemId = selectCase2CaseItemId(caseId, itemId);
        if (case2CaseItemId != 0) {
            return case2CaseItemId;
        }

        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(insertCase2CaseItemQuery, Statement.RETURN_GENERATED_KEYS);
            smt.setString(1, caseId);
            smt.setInt(2, itemId);
            smt.executeUpdate();
            ResultSet rs = smt.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : 0;
        });
    }

    public int selectCase2CaseItemId(String caseId, int itemId) throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(selectCase2CaseItemQuery);
            smt.setString(1, caseId);
            smt.setInt(2, itemId);
            ResultSet rs = smt.executeQuery();
            return rs.next() ? rs.getInt("id") : 0;
        });
    }

    public void insertPlayerStat(String playerUUID, int case2caseitemId) throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(insertPlayerStatQuery);
            smt.setString(1, playerUUID);
            smt.setInt(2, case2caseitemId);
            smt.setLong(3, System.currentTimeMillis());
            smt.executeUpdate();
            return null;
        });
    }

    public List<CaseStat> getCaseStatList() throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            List<CaseStat> caseStats = new ArrayList<>();
            PreparedStatement statement = sqlConnection.getOrCreateStatement(selectCasesQuery);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String id = rs.getString("id");
                String itemNBT = rs.getString("itemNBT");
                ItemStack stack = ItemUtils.getItemStackFromBase64(itemNBT);
                CaseStat caseStat = new CaseStat(id, stack);
                caseStats.add(caseStat);
            }
            return caseStats;
        });
    }

    public List<CaseItemsStat> getCaseItemStatList(String caseID) throws SQLException {
        List<CaseItemsStat> caseStats = new ArrayList<>();
        this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement statement = sqlConnection.getOrCreateStatement(selectCaseItemsQuery);
            statement.setString(1, caseID);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("itemId");
                String itemNBT = rs.getString("itemNBT");
                ItemStack stack = ItemUtils.getItemStackFromBase64(itemNBT);
                CaseItemsStat caseItemsStat = new CaseItemsStat(id, caseID, stack);
                caseStats.add(caseItemsStat);
            }
            return null;
        });
        return caseStats;
    }

    public void deleteCaseItem(CaseItemsStat caseItemsStat) throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(deleteCaseItemQuary);
            smt.setInt(1, caseItemsStat.id());
            smt.executeUpdate();
            return null;
        });
    }

    public void updateCaseItem(CaseItemsStat caseItemsStat, ItemStack stack) throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(updateCaseItemQuary);
            smt.setString(1, caseItemsStat.caseId());
            smt.setString(2, stack.getType().getKey().asString());
            smt.setInt(3, stack.getAmount());
            smt.setString(4, stack.getItemMeta().getAsString());
            smt.setInt(5, caseItemsStat.id());
            smt.executeUpdate();
            return null;
        });
    }

    public ArrayList<PlayerCaseItemStat> getPlayerCaseStats(String caseId, List<String> players) throws SQLException {
        return this.connection.runCommands((connection, sqlConnection) -> {
            ArrayList<PlayerCaseItemStat> playerCaseItemStats = new ArrayList<>();
            String qs = "?,".repeat(players.size());
            qs = qs.substring(0, qs.length() - 1);
            PreparedStatement statement = sqlConnection.getOrCreateStatement(String.format(selectPlayerCaseStats, qs));
            statement.setString(1, caseId);
            int i = 2;

            String uuid;

            for (String uuidString : players) {
                statement.setString(i, uuidString);
                i++;
            }

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                uuid = rs.getString("UUID");
                String itemId = rs.getString("itemId");
                String item = rs.getString("item");
                String itemNBT = rs.getString("itemNBT");
                int amount = rs.getInt("amount");
                int count = rs.getInt("itemCount");

                PlayerCaseItemStat stat = new PlayerCaseItemStat(uuid, itemId, item, itemNBT, amount, count);
                playerCaseItemStats.add(stat);
            }

            return playerCaseItemStats;
        });
    }


    public void deleteCase(CaseStat caseStat) throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            PreparedStatement smt = sqlConnection.getOrCreateStatement(this.deleteCaseQuary);
            smt.setString(1, caseStat.id());
            smt.executeUpdate();
            return null;
        });
    }


}
