package com.gabe.shopgui;

import org.bukkit.inventory.ItemStack;

public class ShopItem {
    private ItemStack item;
    private int slot;
    private float buyPrice;
    private float sellPrice;

    public ShopItem(ItemStack item, float buyPrice, float sellPrice, int slot){
        this.item = item;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.slot = slot;
    }

    public float getBuyPrice() {
        return buyPrice;
    }

    public float getSellPrice() {
        return sellPrice;
    }

    public ItemStack getItem() {
        return item;
    }

    public int getSlot() {
        return slot;
    }
}
