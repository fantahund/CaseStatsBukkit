package de.fanta.casestatsbukkit;

import de.cubeside.connection.GlobalServer;
import de.cubeside.connection.event.GlobalDataEvent;
import de.cubeside.connection.event.GlobalServerConnectedEvent;
import de.fanta.casestatsbukkit.utils.ChatUtil;
import de.fanta.casestatsbukkit.utils.ItemUtils;
import de.iani.cubesideutils.MinecraftDataInputStream;
import de.iani.cubesideutils.MinecraftDataOutputStream;
import de.iani.cubesideutils.bukkit.plugin.api.GlobalDataHelperBukkit;
import de.speedy64.globalport.GlobalApi;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class CaseStatsGlobalDataHelper extends GlobalDataHelperBukkit<CaseStatsGlobalDataHelper.CaseStatsMessageType> implements Listener {

    public static final String CHANNEL = "CaseStats";
    public static final String LOGIN_CLIENT_CHANNEL = "globaldatalogin:data";
    private final CaseStatsBukkit plugin;

    public CaseStatsGlobalDataHelper(CaseStatsBukkit plugin) {
        super(CaseStatsMessageType.class, CHANNEL, plugin);
        this.plugin = plugin;
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, LOGIN_CLIENT_CHANNEL);
    }

    @Override
    protected void handleMessage(CaseStatsMessageType challengeMessageType, GlobalServer globalServer, DataInputStream data) {

    }

    @EventHandler
    public void onGlobalDataEvent(GlobalDataEvent event) throws IOException {
        if (event.getChannel().equals(CHANNEL)) {
            MinecraftDataInputStream data = new MinecraftDataInputStream(event.getData());
            int channel = data.readByte();
            int itemsSize;
            int i;
            String item;
            if (channel == 1) {
                String uuid = data.readUTF();
                Player player = Bukkit.getPlayer(UUID.fromString(uuid));
                if (player != null && player.isOnline() && this.plugin.getLoginRequestList().contains(player.getUniqueId())) {
                    ByteArrayOutputStream clientOut = new ByteArrayOutputStream();
                    MinecraftDataOutputStream clientDataOut = new MinecraftDataOutputStream(clientOut);
                    itemsSize = 1;
                    i = 0;
                    item = data.readUTF();
                    String ipString = data.readUTF();
                    int port = data.readInt();
                    clientDataOut.writeByte(itemsSize);
                    clientDataOut.writeByte(i);
                    clientDataOut.writeString(uuid);
                    clientDataOut.writeString(item);
                    clientDataOut.writeString(ipString);
                    clientDataOut.writeInt(port);
                    clientDataOut.flush();
                    player.sendPluginMessage(this.plugin, "globaldatalogin:data", clientOut.toByteArray());
                    ChatUtil.sendNormalMessage(player, "Login daten werden zum Client übertragen...");
                }
            }

            if (channel == 0) {
                int caseStatsDateChannelVersion = data.readByte();
                if (caseStatsDateChannelVersion == 0) {
                    String caseID = data.readString();
                    String caseItem = data.readString();
                    String caseItemString = data.readString();
                    ItemStack stack = Bukkit.getItemFactory().createItemStack(caseItem + caseItemString);

                    try {
                        this.plugin.getDatabase().insertCase(caseID, caseItem, ItemUtils.getBase64StringFromItemStack(stack));
                    } catch (SQLException var16) {
                        throw new RuntimeException(var16);
                    }

                    itemsSize = data.readInt();
                    for (i = 0; i < itemsSize; ++i) {
                        item = data.readString();
                        int amount = data.readInt();
                        String itemNBT = data.readString();
                        ItemStack caseItemStack = Bukkit.getItemFactory().createItemStack(item + itemNBT);
                        if (caseItemStack.getItemMeta() instanceof MapMeta mapMeta) {
                            mapMeta.setMapId(1);
                            caseItemStack.setItemMeta(mapMeta);
                        }

                        try {
                            int itemId = this.plugin.getDatabase().insertCaseItem(item, amount, ItemUtils.getBase64StringFromItemStack(caseItemStack));
                            int case2CaseItemId = this.plugin.getDatabase().insertCase2CaseItem(caseID, itemId);
                            this.plugin.getDatabase().insertPlayerStat(event.getSource().getName(), case2CaseItemId);
                        } catch (SQLException var15) {
                            throw new RuntimeException(var15);
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
        if (player != null && player.isOnline() && this.plugin.getLoginRequestList().contains(player.getUniqueId())) {
            ChatUtil.sendNormalMessage(player, "Verbindung zum Server erfolgreich hergestellt.");
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> GlobalApi.portOnlinePlayerToLocation(player.getName(), "spawn"), 40L);
            this.plugin.getLoginRequestList().remove(player.getUniqueId());
        }

    }

    public void sendLoginRequest(UUID uuid) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(out);
        dataOut.writeInt(0);
        dataOut.writeUTF(uuid.toString());
        dataOut.flush();
        this.sendData("CaseStats", out.toByteArray());
    }

    public enum CaseStatsMessageType {
        LOGIN_CLIENT;
    }

}
