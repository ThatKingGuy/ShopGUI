package com.gabe.shopgui;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.List;

public class ShopPage {
    private List<ShopItem> items;
    private String name;
    private Material icon;
    private String title;
    private int pageNums;

    public ShopPage(String name, Material icon, List<ShopItem> items, String title){
        this.items = items;
        this.name = name;
        this.icon = icon;
        pageNums = (int) Math.ceil((double)items.size()/45);
        Bukkit.getLogger().info(name+" was registered with " + pageNums+" page/s");
        this.title = title;
    }

    public List<ShopItem> getItems() {
        return items;
    }

    public Material getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getTitle(){
        return title;
    }
}
