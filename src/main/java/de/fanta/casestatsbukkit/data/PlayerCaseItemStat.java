package de.fanta.casestatsbukkit.data;

public record PlayerCaseItemStat(String uuid, String itemId, String item, String itemNBT, int amount, int count) {
    public PlayerCaseItemStat(String uuid, String itemId, String item, String itemNBT, int amount, int count) {
        this.uuid = uuid;
        this.itemId = itemId;
        this.item = item;
        this.itemNBT = itemNBT;
        this.amount = amount;
        this.count = count;
    }

    public String uuid() {
        return this.uuid;
    }

    public String itemId() {
        return this.itemId;
    }

    public String item() {
        return this.item;
    }

    public String itemNBT() {
        return this.itemNBT;
    }

    public int amount() {
        return this.amount;
    }

    public int count() {
        return this.count;
    }
}
