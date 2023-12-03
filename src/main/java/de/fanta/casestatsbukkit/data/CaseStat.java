package de.fanta.casestatsbukkit.data;

import org.bukkit.inventory.ItemStack;

public class CaseStat {

    private final String id;
    private final ItemStack icon;

    public CaseStat(String id, ItemStack icon) {
        this.id = id;
        this.icon = icon;
    }

    public String id() {
        return id;
    }

    public ItemStack icon() {
        return icon;
    }

}
