package de.fanta.casestatsbukkit.utils;

import de.iani.cubesideutils.bukkit.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Base64;

public class ItemUtils {

    public static final ItemStack EMPTY_ICON = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", true, false);

    public static ItemStack createGuiItem(Material material, String name, String... lore) {
        return createGuiItem(material, name, false, lore);
    }

    public static ItemStack createGuiItem(Material material, String name, boolean glowing, boolean showTooltip, String... lore) {
        ItemBuilder builder = ItemBuilder.fromMaterial(material).displayName(name).lore(lore);
        if (glowing) {
            builder.enchantment(Enchantment.UNBREAKING, 1, true).flag(ItemFlag.HIDE_ENCHANTS);
        }
        if (!showTooltip) {
            builder.hideTooltip(true);
        }
        return builder.build();
    }

    public static ItemStack createGuiItem(Material material, String name, boolean glowing, String... lore) {
        return createGuiItem(material, name, glowing, true, lore);
    }

    public static String getBase64StringFromItemStack(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(stack.serializeAsBytes());
    }

    public static ItemStack getItemStackFromBase64(String itemString) {
        if (itemString == null) {
            return null;
        }

        byte[] itemBytes = Base64.getDecoder().decode(itemString);
        return ItemStack.deserializeBytes(itemBytes);
    }
}
