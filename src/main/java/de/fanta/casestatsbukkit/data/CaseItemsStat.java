package de.fanta.casestatsbukkit.data;

import org.bukkit.inventory.ItemStack;

public class CaseItemsStat {

    private final int id;
    private final String caseId;
    private final ItemStack item;

    public CaseItemsStat(int id, String caseId, ItemStack icon) {
        this.id = id;
        this.caseId = caseId;
        this.item = icon;
    }

    public int id() {
        return id;
    }
    public String caseId() {
        return caseId;
    }

    public ItemStack item() {
        return item;
    }

}
