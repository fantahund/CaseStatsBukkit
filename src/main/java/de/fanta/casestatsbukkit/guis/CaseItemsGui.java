package de.fanta.casestatsbukkit.guis;

import de.fanta.casestatsbukkit.data.CaseItemsStat;
import de.fanta.casestatsbukkit.utils.ChatUtil;
import de.fanta.casestatsbukkit.utils.CustomHeadsUtil;
import de.iani.cubesideutils.StringUtil;
import de.iani.cubesideutils.bukkit.inventory.AbstractWindow;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CaseItemsGui extends AbstractWindow {
    private static final int NUM_OF_COLUMNS = 9;
    private static final int NUM_OF_ITEM_COLUMNS = NUM_OF_COLUMNS - 1;
    private static final int NUM_OF_ROWS = 6;
    private static final int WINDOW_SIZE = NUM_OF_COLUMNS * NUM_OF_ROWS;
    private static final int END_OF_FIRST_ROW_INDEX = NUM_OF_COLUMNS - 1;
    private static final int END_OF_LAST_ROW_INDEX = WINDOW_SIZE - 1;

    private static final int CLOSE_INDEX = 0;
    private static final int CHANGE_SORT_INDEX = 1;
    private final int numOfItemRows;
    private int scrollAmount;
    private static final HashMap<UUID, List<CaseItemsStat>> caseItemStatListByPlayer = new HashMap<>();

    public CaseItemsGui(List<CaseItemsStat> caseStatList, String caseID, Player player) {
        super(player, Bukkit.createInventory(player, WINDOW_SIZE, ChatUtil.GREEN + "Case: " + ChatUtil.BLUE + StringUtil.capitalizeFirstLetter(caseID, false)));
        caseItemStatListByPlayer.put(player.getUniqueId(), caseStatList);
        this.numOfItemRows = (int) (Math.ceil(caseStatList.size() / (double) NUM_OF_ITEM_COLUMNS));
        this.scrollAmount = 0;
    }

    protected void rebuildInventory() {
        ItemStack[] content = new ItemStack[WINDOW_SIZE];

        // build scroll buttons
        ItemStack scrollUpHead = this.scrollAmount <= 0 ? CustomHeadsUtil.RAINBOW_BLANK.getHead() : CustomHeadsUtil.RAINBOW_ARROW_UP.getHead();
        ItemMeta scrollUpMeta = scrollUpHead.getItemMeta();
        scrollUpMeta.setDisplayName((this.scrollAmount <= 0 ? ChatColor.GRAY : ChatColor.WHITE) + "Up");
        scrollUpHead.setItemMeta(scrollUpMeta);
        content[NUM_OF_COLUMNS - 1] = scrollUpHead;

        ItemStack scrollDownHead = this.scrollAmount - 1 >= this.numOfItemRows - NUM_OF_ROWS ? CustomHeadsUtil.RAINBOW_BLANK.getHead() : CustomHeadsUtil.RAINBOW_ARROW_DOWN.getHead();
        ItemMeta scrollDownMeta = scrollDownHead.getItemMeta();
        scrollDownMeta.setDisplayName((this.scrollAmount - 1 >= this.numOfItemRows - NUM_OF_ROWS ? ChatColor.GRAY : ChatColor.WHITE) + "Down");
        scrollDownHead.setItemMeta(scrollDownMeta);
        content[WINDOW_SIZE - 1] = scrollDownHead;

        // build scrollbar
        for (int i = 1; i < NUM_OF_ROWS - 1; i++) {
            content[(i + 1) * NUM_OF_COLUMNS - 1] = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        }
        int numOfScrollbarRows = NUM_OF_ROWS - 2;
        int currentScrollbarRow =
                (int) Math.round(this.scrollAmount * numOfScrollbarRows / (double) this.numOfItemRows);
        content[(currentScrollbarRow + 2) * NUM_OF_COLUMNS - 1] = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);

        // fill in items
        int index = this.scrollAmount * NUM_OF_ITEM_COLUMNS;
        List<CaseItemsStat> caseItemStats = caseItemStatListByPlayer.get(getPlayer().getUniqueId());
        for (int row = 1; row < NUM_OF_ROWS && index < caseItemStats.size(); row++) {
            for (int column = 0; column < NUM_OF_ITEM_COLUMNS && index < caseItemStats.size(); column++) {
                CaseItemsStat caseStat = new ArrayList<>(caseItemStats).get(index++);
                content[row * NUM_OF_COLUMNS + column] = caseStat.item();
            }
        }

        content[CLOSE_INDEX] = CustomHeadsUtil.RAINBOW_X.getHead("Menü Schließen");
        //TODO SORT??? content[CHANGE_SORT_INDEX] = isRainbowListByPlayer.get(getPlayer().getUniqueId()) ? CustomHeadsUtil.RAINBOW_PATTERN.getHead(plugin.getMessages().getSortRainbow()) : CustomHeadsUtil.MODERN_CLOCK.getHead(plugin.getMessages().getSortDate());
        getInventory().setContents(content);
    }

    public void onItemClicked(InventoryClickEvent event) {
        if (!mayAffectThisInventory(event)) {
            return;
        }

        event.setCancelled(true);

        if (!getInventory().equals(event.getClickedInventory())) {
            return;
        }

        int slot = event.getSlot();
        if (slot == CLOSE_INDEX) {
            getPlayer().closeInventory();
            return;
        }
        if (slot == CHANGE_SORT_INDEX) {
            //new SavedColorsGui(plugin.getPlayerColors(getPlayer()), getPlayer(), plugin, !isRainbowListByPlayer.get(getPlayer().getUniqueId())).open();
            return;
        }

        if (slot == END_OF_FIRST_ROW_INDEX) {
            attempScrollUp();
        } else if (slot == END_OF_LAST_ROW_INDEX) {
            attempScrollDown();
        }

        if ((slot + 1) % NUM_OF_COLUMNS == 0) {
            int numOfScrollbarRows = NUM_OF_ROWS - 2;
            int selectedScrollBarRow = (slot + 1) / NUM_OF_COLUMNS - 2;
            int targetScrollAmount = Math.round(selectedScrollBarRow * this.numOfItemRows / numOfScrollbarRows);
            this.scrollAmount = Math.min(targetScrollAmount, this.numOfItemRows - NUM_OF_ROWS);
            this.scrollAmount = Math.max(this.scrollAmount, 0);
            rebuildInventory();
            return;
        }

        Player p = getPlayer();
        int caseStatsIndex = ((slot - (slot / NUM_OF_COLUMNS)) + this.scrollAmount * NUM_OF_ITEM_COLUMNS) - NUM_OF_COLUMNS + 1;
        List<CaseItemsStat> caseItemStatList = caseItemStatListByPlayer.get(getPlayer().getUniqueId());
        if (caseStatsIndex >= caseItemStatList.size()) {
            return;
        }

        CaseItemsStat caseItemsStat = caseItemStatListByPlayer.get(p.getUniqueId()).get(caseStatsIndex);
        if (caseItemsStat != null) {
            if (event.isLeftClick()) {
                if (event.isShiftClick()) {
                    getPlayer().getInventory().addItem(caseItemsStat.item());
                    ChatUtil.sendNormalMessage(getPlayer(), "ItemID: " + caseItemsStat.id() + " aus case: " + caseItemsStat.caseId() + " gegeben!");
                } else {
                    new CaseItemEditGui(caseItemsStat, getPlayer()).open();
                }
            } else if (event.isRightClick()) {
                new CaseItemDeleteGui(p, caseItemsStat).open();
            }
        }
    }

    private void attempScrollUp() {
        if (this.scrollAmount <= 0) {
            return;
        }
        this.scrollAmount--;
        rebuildInventory();
    }

    private void attempScrollDown() {
        if (this.scrollAmount - 1 >= this.numOfItemRows - NUM_OF_ROWS) {
            return;
        }
        this.scrollAmount++;
        rebuildInventory();
    }
}
