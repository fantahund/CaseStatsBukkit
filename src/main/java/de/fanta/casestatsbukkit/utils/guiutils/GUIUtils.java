package de.fanta.casestatsbukkit.utils.guiutils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GUIUtils {

    public static final ItemStack EMPTY_ICON = createGuiItem(Material.GREEN_STAINED_GLASS_PANE, "§1", false);

    public static ItemStack createGuiItem(Material material, String name, String... lore) {
        return createGuiItem(material, name, false, lore);
    }

    public static ItemStack createGuiItem(final Material material, final String name, boolean glowing, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        if (glowing) {
            meta.addEnchant(Enchantment.MENDING, 1, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }
}
