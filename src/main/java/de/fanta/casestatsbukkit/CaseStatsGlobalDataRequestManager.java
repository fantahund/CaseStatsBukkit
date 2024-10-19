package de.fanta.casestatsbukkit;

import de.cubeside.connection.GlobalServer;
import de.fanta.casestatsbukkit.data.CaseStat;
import de.fanta.casestatsbukkit.data.PlayerCaseItemStat;
import de.fanta.casestatsbukkit.utils.ItemUtils;
import de.iani.cubesideutils.bukkit.plugin.api.GlobalDataRequestManagerBukkit;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
                    List<CaseStat> cases = this.plugin.getDatabase().getCaseStatList();
                    dataOutputStream.writeInt(cases.size());

                    for (CaseStat caseStat : cases) {
                        dataOutputStream.writeUTF(caseStat.id());
                        dataOutputStream.writeUTF(caseStat.icon().getType().getKey().asString());
                        dataOutputStream.writeUTF(caseStat.icon().getItemMeta().getAsComponentString());
                        dataOutputStream.flush();
                    }
                    return;
                } catch (SQLException var13) {
                    throw new RuntimeException(var13);
                }
            }
            case GET_CASE_STATS -> {
                String caseID = dataInputStream.readUTF();
                int players = dataInputStream.readInt();
                ArrayList<String > names = new ArrayList();

                for(int i = 0; i < players; ++i) {
                    names.add(dataInputStream.readUTF());
                }

                List<String> uuids = names.stream().map((s) -> this.plugin.getPlayerUUIDCache().getPlayer(s, true)).filter(Objects::nonNull).map((cachedPlayer) -> cachedPlayer.getUUID().toString()).collect(Collectors.toList());

                try {
                    ArrayList<PlayerCaseItemStat> stats = this.plugin.getDatabase().getPlayerCaseStats(caseID, uuids);
                    dataOutputStream.writeInt(stats.size());

                    for (PlayerCaseItemStat stat : stats) {
                        ItemStack stack = ItemUtils.getItemStackFromBase64(stat.itemNBT());
                        dataOutputStream.writeUTF(stat.uuid());
                        dataOutputStream.writeUTF(stat.itemId());
                        dataOutputStream.writeUTF(stat.item());
                        dataOutputStream.writeUTF(stack.getItemMeta().getAsComponentString());
                        dataOutputStream.writeInt(stat.amount());
                        dataOutputStream.writeInt(stat.count());
                        dataOutputStream.flush();
                    }

                    return;
                } catch (SQLException var12) {
                    throw new RuntimeException(var12);
                }

            }
            default-> throw new AssertionError("unknown message type " + messageType);
        }
    }

    @Override
    protected Object handleResponse(CaseStatsGlobalDataRequestType messageType, GlobalServer globalServer, DataInputStream dataInputStream) throws IOException {
        return null;
    }
}
