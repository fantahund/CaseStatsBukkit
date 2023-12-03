package de.fanta.casestatsbukkit;

import de.cubeside.connection.GlobalServer;
import de.cubeside.connection.event.GlobalDataEvent;
import de.cubeside.connection.event.GlobalServerConnectedEvent;
import de.fanta.casestatsbukkit.utils.ChatUtil;
import de.iani.cubesideutils.MinecraftDataInputStream;
import de.iani.cubesideutils.MinecraftDataOutputStream;
import de.iani.cubesideutils.bukkit.plugin.api.GlobalDataHelperBukkit;
import de.speedy64.globalport.GlobalApi;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class CaseStatsGlobalDataHelper extends GlobalDataHelperBukkit<CaseStatsGlobalDataHelper.CaseStatsMessageType> implements Listener {

    public static final String CHANNEL = "CaseStats";
    public static final String CLIENT_CHANNEL = "casestats:data";
    private final CaseStatsBukkit plugin;

    public CaseStatsGlobalDataHelper(CaseStatsBukkit plugin) {
        super(CaseStatsMessageType.class, CHANNEL, plugin);
        this.plugin = plugin;
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CLIENT_CHANNEL);
    }

    @Override
    protected void handleMessage(CaseStatsMessageType challengeMessageType, GlobalServer globalServer, DataInputStream data) {

    }

    @EventHandler
    public void onGlobalDataEvent(GlobalDataEvent event) throws IOException {
        System.out.println(event.getChannel());
        if (event.getChannel().equals(CaseStatsGlobalDataRequestManager.CHANNEL)) {
            Bukkit.getLogger().info(event.getSource().getName());
        }

        if (event.getChannel().equals(CHANNEL)) {
            MinecraftDataInputStream data = new MinecraftDataInputStream(event.getData());
            int channel = data.readByte();
            if (channel == 1) {
                String uuid = data.readUTF();
                Player player = Bukkit.getPlayer(UUID.fromString(uuid));
                if (player != null && player.isOnline() && plugin.getLoginRequestList().contains(player.getUniqueId())) {
                    ByteArrayOutputStream clientOut = new ByteArrayOutputStream();
                    MinecraftDataOutputStream clientDataOut = new MinecraftDataOutputStream(clientOut);
                    int caseStatsDateChannel = 1;
                    int caseStatsDateChannelVersion = 0;
                    String passwort = data.readUTF();
                    String ipString = data.readUTF();
                    int port = data.readInt();

                    clientDataOut.writeByte(caseStatsDateChannel);
                    clientDataOut.writeByte(caseStatsDateChannelVersion);
                    clientDataOut.writeString(uuid);
                    clientDataOut.writeString(passwort);
                    clientDataOut.writeString(ipString);
                    clientDataOut.writeInt(port);
                    clientDataOut.flush();
                    player.sendPluginMessage(plugin, CLIENT_CHANNEL, clientOut.toByteArray());
                    ChatUtil.sendNormalMessage(player, "Login daten werden zum Client übertragen...");
                }
            }
            if (channel == 0) {
                int caseStatsDateChannelVersion = data.readByte();
                if (caseStatsDateChannelVersion == 0) {
                    String caseID = data.readString();
                    String caseItem = data.readString();
                    String caseItemString = data.readString();
                    try {
                        plugin.getDatabase().insertCase(caseID, caseItem, caseItemString);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    int itemsSize = data.readInt();
                    for (int i = 0; i < itemsSize; i++) {
                        String item = data.readString();
                        int amount = data.readInt();
                        String itemNBT = data.readString();
                        try {
                            int itemId = plugin.getDatabase().insertCaseItem(item, amount, itemNBT);
                            int case2CaseItemId = plugin.getDatabase().insertCase2CaseItem(caseID, itemId);
                            plugin.getDatabase().insertPlayerStat(event.getSource().getName(), case2CaseItemId);

                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onGlobalDataClientLogin(GlobalServerConnectedEvent e) {
        String user = e.getServer().getName();
        Player player = Bukkit.getPlayer(UUID.fromString(user));
        if (player != null && player.isOnline() && plugin.getLoginRequestList().contains(player.getUniqueId())) {
            ChatUtil.sendNormalMessage(player, "Verbindung zum Server erfolgreich hergestellt.");
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> GlobalApi.portOnlinePlayerToLocation(player.getName(), "spawn"), 40);
            plugin.getLoginRequestList().remove(player.getUniqueId());
        }
    }

    public void sendLoginRequest(UUID uuid) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(out);
        dataOut.writeInt(0);
        dataOut.writeUTF(uuid.toString());
        dataOut.flush();
        sendData(CHANNEL, out.toByteArray());
    }

    public enum CaseStatsMessageType {
        LOGIN_CLIENT;
    }
}
