package de.fanta.casestatsbukkit.guis;

import de.fanta.casestatsbukkit.CaseStatsBukkit;
import de.fanta.casestatsbukkit.data.CaseItemsStat;
import de.fanta.casestatsbukkit.utils.ChatUtil;
import de.fanta.casestatsbukkit.utils.ItemUtils;
import de.iani.cubesideutils.bukkit.inventory.AbstractWindow;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class CaseItemDeleteGui extends AbstractWindow {
    private static final CaseStatsBukkit plugin = CaseStatsBukkit.getPlugin();

    private static final int YES_INDEX = 0;
    private static final int ITEM_INDEX = 2;
    private static final int NO_INDEX = 4;

    private static final int INVENTORY_SIZE = 5;

    private final HashMap<UUID, CaseItemsStat> playerCaseItemStatList = new HashMap<>();

    public CaseItemDeleteGui(Player player, CaseItemsStat caseItemsStat) {
        super(player, Bukkit.createInventory(player, InventoryType.HOPPER, ChatUtil.RED + "Delete Case Item"));
        playerCaseItemStatList.put(player.getUniqueId(), caseItemsStat);
    }


    @Override
    protected void rebuildInventory() {
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            ItemStack item;
            ItemStack stack = playerCaseItemStatList.get(getPlayer().getUniqueId()).item();
            switch (i) {
                case YES_INDEX ->
                        item = ItemUtils.createGuiItem(Material.LIME_CONCRETE, ChatUtil.GREEN + "Yes", false);
                case ITEM_INDEX -> item = stack;
                case NO_INDEX ->
                        item = ItemUtils.createGuiItem(Material.RED_CONCRETE, ChatUtil.RED + "No", false);
                default -> item = ItemUtils.EMPTY_ICON;
            }
            this.getInventory().setItem(i, item);
        }
    }

    @Override
    public void onItemClicked(InventoryClickEvent event) {
        Player player = getPlayer();
        if (!mayAffectThisInventory(event)) {
            return;
        }

        event.setCancelled(true);
        if (!getInventory().equals(event.getClickedInventory())) {
            return;
        }

        int slot = event.getSlot();
        switch (slot) {
            case YES_INDEX -> {
                try {
                    CaseItemsStat caseItemsStat = playerCaseItemStatList.get(player.getUniqueId());
                    plugin.getDatabase().deleteCaseItem(caseItemsStat);
                    ChatUtil.sendNormalMessage(player, "CaseItem wurde erfolgreich gelöscht.");
                    plugin.getLogger().info(player.getName() + " remove CaseItem with id " + caseItemsStat.id() + " from " + caseItemsStat.caseId());
                    player.closeInventory();
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "CaseItem could not be deleted", e);
                    ChatUtil.sendNormalMessage(player, "CaseItem konnte nicht gelöscht werden.");
                    player.closeInventory();
                }
            }
            case NO_INDEX -> player.closeInventory();
            default -> {
            }
        }
    }

    @Override
    public void closed() {
        playerCaseItemStatList.remove(getPlayer().getUniqueId());
    }
}
