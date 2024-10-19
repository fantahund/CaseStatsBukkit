package de.fanta.casestatsbukkit.guis;

import de.fanta.casestatsbukkit.CaseStatsBukkit;
import de.fanta.casestatsbukkit.data.CaseItemsStat;
import de.fanta.casestatsbukkit.utils.ChatUtil;
import de.fanta.casestatsbukkit.utils.CustomHeadsUtil;
import de.fanta.casestatsbukkit.utils.ItemUtils;
import de.iani.cubesideutils.bukkit.inventory.AbstractWindow;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class CaseItemEditGui extends AbstractWindow {

    public static final int INVENTORY_SIZE = 5;
    private static final CaseStatsBukkit plugin = CaseStatsBukkit.getPlugin();
    private static final int CURRENT_ITEM_INDEX = 0;
    private static final int ARROW_RIGHT_INDEX = 1;
    private static final int ITEM_INDEX = 2;
    private static final int YES_INDEX = 4;
    private final HashMap<UUID, CaseItemsStat> playerCaseItemStatList = new HashMap<>();

    public CaseItemEditGui(CaseItemsStat caseItemsStat, Player player) {
        super(player, Bukkit.createInventory(player, InventoryType.HOPPER, ChatUtil.YELLOW + "Edit Case Item"));
        playerCaseItemStatList.put(player.getUniqueId(), caseItemsStat);
    }

    @Override
    public void onItemClicked(InventoryClickEvent event) {
        Player player = getPlayer();
        if (!mayAffectThisInventory(event)) {
            return;
        }

        if (!getInventory().equals(event.getClickedInventory())) {
            return;
        }

        int slot = event.getSlot();
        switch (slot) {
            case ITEM_INDEX -> {
                switch (event.getAction()) {
                    case MOVE_TO_OTHER_INVENTORY:
                    case NOTHING:
                    case PICKUP_ALL:
                    case PICKUP_HALF:
                    case PICKUP_ONE:
                    case PICKUP_SOME:
                    case PLACE_ALL:
                    case PLACE_ONE:
                    case PLACE_SOME:
                    case SWAP_WITH_CURSOR:
                        break;
                    default:
                        event.setCancelled(true);
                        return;

                }
                plugin.getServer().getScheduler().runTask(plugin, () -> getPlayer().updateInventory());
            }
            case YES_INDEX -> {
                event.setCancelled(true);
                ItemStack stack = getInventory().getItem(ITEM_INDEX);
                if (stack != null && !stack.getType().isAir()) {
                    try {
                        plugin.getDatabase().updateCaseItem(playerCaseItemStatList.get(player.getUniqueId()), stack);
                        ChatUtil.sendNormalMessage(player, "Item wurde erfolgreich ersetzt.");
                    } catch (SQLException e) {
                        plugin.getLogger().log(Level.SEVERE, "Error by edit CaseItem", e);
                        ChatUtil.sendErrorMessage(player, "Item konnte nicht ersetzt werden.");
                    }
                    player.closeInventory();
                } else {
                    ChatUtil.sendErrorMessage(player, "Du musst ein ItemStack in die mitte legen.");
                }

            }
            default -> event.setCancelled(true);
        }
    }

    @Override
    public void onItemDraged(InventoryDragEvent event) {
        if (!this.mayAffectThisInventory(event)) {
            return;
        }
        for (int slot : event.getRawSlots()) {
            if (slot != ITEM_INDEX) {
                event.setCancelled(true);
                return;
            }
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> getPlayer().updateInventory());
    }

    @Override
    protected void rebuildInventory() {
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            ItemStack item;
            switch (i) {
                case YES_INDEX -> item = ItemUtils.createGuiItem(Material.LIME_CONCRETE, ChatUtil.GREEN + "Yes", false);
                case CURRENT_ITEM_INDEX -> item = playerCaseItemStatList.get(getPlayer().getUniqueId()).item();
                case ARROW_RIGHT_INDEX -> item = CustomHeadsUtil.RAINBOW_ARROW_RIGHT.getHead(ChatUtil.RED + "Aktuelles Item überschreiben.");
                case ITEM_INDEX -> item = new ItemStack(Material.AIR);
                default -> item = ItemUtils.EMPTY_ICON;
            }
            this.getInventory().setItem(i, item);
        }
    }
}


