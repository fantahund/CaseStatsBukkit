package de.fanta.casestatsbukkit;

import de.cubeside.connection.GlobalServer;
import de.fanta.casestatsbukkit.data.CaseStat;
import de.iani.cubesideutils.bukkit.plugin.api.GlobalDataRequestManagerBukkit;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

public class CaseStatsGlobalDataRequestManager extends GlobalDataRequestManagerBukkit<CaseStatsGlobalDataRequestType> {

    public static final String CHANNEL = "CaseStats-Requests";
    private final CaseStatsBukkit plugin;

    public CaseStatsGlobalDataRequestManager(CaseStatsBukkit plugin) {
        super(CaseStatsGlobalDataRequestType.class, CHANNEL, plugin);
        this.plugin = plugin;
    }

    @Override
    protected void respondToRequest(CaseStatsGlobalDataRequestType messageType, GlobalServer globalServer, DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        Bukkit.getLogger().info(messageType + " " + globalServer.getName());
        switch (messageType) {
            case GET_CASES -> {
                try {
                    List<CaseStat> cases = plugin.getDatabase().getCaseStatList();
                    dataOutputStream.writeInt(cases.size());
                    for (CaseStat caseStat : cases) {
                        dataOutputStream.writeUTF(caseStat.id());
                        dataOutputStream.writeUTF(caseStat.icon().getType().getKey().asString());
                        dataOutputStream.writeUTF(caseStat.icon().getItemMeta().getAsString());
                        dataOutputStream.flush();
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            }
            case GET_CASE_STATS -> {
                /*String yamlString = dataInputStream.readUTF();
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.loadFromString(yamlString);
                List<String> names = yaml.getStringList("names");
                List<UUID> uuids = names.stream().map(plugin.getPlayerUUIDCache()::getPlayer).map(OfflinePlayer::getUniqueId).toList();
                // TODO: work
                String responseString = "";
                this.sendMsgPart(dataOutputStream, responseString);*/
            }
            default-> throw new AssertionError("unknown message type " + messageType);
        }
    }

    @Override
    protected Object handleResponse(CaseStatsGlobalDataRequestType messageType, GlobalServer globalServer, DataInputStream dataInputStream) throws IOException {
        return null;
    }
}
