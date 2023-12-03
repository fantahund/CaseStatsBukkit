package de.fanta.casestatsbukkit.data;

import de.fanta.casestatsbukkit.CaseStatsBukkit;
import de.iani.cubesideutils.sql.MySQLConnection;
import de.iani.cubesideutils.sql.SQLConfig;
import de.iani.cubesideutils.sql.SQLConnection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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

    public Database(SQLConfig config, CaseStatsBukkit plugin) {
        this.config = config;
        this.plugin = plugin;

        try {
            this.connection = new MySQLConnection(config.getHost(), config.getDatabase(), config.getUser(), config.getPassword());

            createTablesIfNotExist();
        } catch (SQLException ex) {
            throw new RuntimeException("Could not initialize database", ex);
        }

        insertCaseQuery = "INSERT INTO " + config.getTablePrefix() + "_cases(id, item, itemNBT) VALUE (?, ?, ?) ON DUPLICATE KEY UPDATE id = ?, item = ?, itemNBT = ?";

        selectCaseItemIdQuery = "SELECT id FROM " + config.getTablePrefix() + "_case_items WHERE item = ? AND amount = ? AND itemNBT = ?";
        insertCaseItemQuery = "INSERT INTO " + config.getTablePrefix() + "_case_items(item, amount, itemNBT) VALUE (?, ?, ?)";

        selectCase2CaseItemQuery = "SELECT id FROM " + config.getTablePrefix() + "_case2caseitems WHERE caseId = ? AND caseItemId = ?";
        insertCase2CaseItemQuery = "INSERT INTO " + config.getTablePrefix() + "_case2caseitems(caseId, caseItemId) VALUE (?, ?)";

        insertPlayerStatQuery = "INSERT INTO " + config.getTablePrefix() + "_player_stats(uuid, case2caseitemId, timestamp) VALUE (?, ?, ?)";

        selectCasesQuery = "SELECT * FROM " + config.getTablePrefix() + "_cases";

        selectCaseItemsQuery = "SELECT " + config.getTablePrefix()  + "_case_items.id itemId, " + config.getTablePrefix() + "_case_items.item item, " + config.getTablePrefix() + "_case_items.amount amount, " + config.getTablePrefix() + "_case_items.itemNBT itemNBT FROM " + config.getTablePrefix() + "_case2caseitems INNER JOIN " + config.getTablePrefix() + "_case_items ON " + config.getTablePrefix() + "_case_items.id=" + config.getTablePrefix() + "_case2caseitems.caseItemId WHERE " + config.getTablePrefix() + "_case2caseitems.caseId = ?";

        deleteCaseItemQuary = "DELETE FROM " + config.getTablePrefix() + "_case_items" + " WHERE `id` = ?";
        updateCaseItemQuary = "UPDATE " + config.getTablePrefix() + "_case_items" + " SET `caseId`= ?,`item`= ?,`amount`= ?,`itemNBT`= ? WHERE `id` = ?";

        //GIB ALLE CASES// GIB 1 CASE für List<UUID> (TIME VON - BIS)
    }

    private void createTablesIfNotExist() throws SQLException {
        this.connection.runCommands((connection, sqlConnection) -> {
            Statement smt = connection.createStatement();
            smt.executeUpdate("CREATE TABLE IF NOT EXISTS " + config.getTablePrefix() + "_cases" + " (" +
                    "`id` VARCHAR(255)," +
                    "`item` TINYTEXT," +
                    "`itemNBT` LONGTEXT," +
                    "PRIMARY KEY (id)" +
                    ")"
            );
            smt.executeUpdate("CREATE TABLE IF NOT EXISTS " + config.getTablePrefix() + "_case_items" + " (" +
                    "`id` INT NOT NULL AUTO_INCREMENT," +
                    "`item` TINYTEXT," +
                    "`amount` INT," +
                    "`itemNBT` LONGTEXT," +
                    "CONSTRAINT item_identifier UNIQUE (item, amount, itemNBT)," +
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

            smt.setString(4, caseId);
            smt.setString(5, item);
            smt.setString(6, itemNBT);

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
                String item = rs.getString("item");
                String itemNBT = rs.getString("itemNBT");
                ItemStack stack = new ItemStack(Material.matchMaterial(item), 1);
                plugin.getServer().getUnsafe().modifyItemStack(stack, itemNBT);
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
                String item = rs.getString("item");
                int amount = rs.getInt("amount");
                String itemNBT = rs.getString("itemNBT");
                ItemStack stack = new ItemStack(Material.matchMaterial(item), amount);
                plugin.getServer().getUnsafe().modifyItemStack(stack, itemNBT);
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
}
