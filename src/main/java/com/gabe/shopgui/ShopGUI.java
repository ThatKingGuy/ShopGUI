package com.gabe.shopgui;

import net.milkbowl.vault.economy.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Stream;


public final class ShopGUI extends JavaPlugin implements Listener {

    public static Economy econ;
    private List<ShopPage> pages = new ArrayList<>();

    public static String color(String text){
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    public static String format(String text){
        return ChatColor.translateAlternateColorCodes('&', "&aShop >&r " + text);
    }

    @Override
    public void onEnable() {
        String txt =
                "\n  ______   __                             ______   __    __  ______ \n" +
                " /      \\ |  \\                           /      \\ |  \\  |  \\|      \\\n" +
                "|  $$$$$$\\| $$____    ______    ______  |  $$$$$$\\| $$  | $$ \\$$$$$$\n" +
                "| $$___\\$$| $$    \\  /      \\  /      \\ | $$ __\\$$| $$  | $$  | $$  \n" +
                " \\$$    \\ | $$$$$$$\\|  $$$$$$\\|  $$$$$$\\| $$|    \\| $$  | $$  | $$  "+ChatColor.WHITE+"by "+ChatColor.GREEN+"ThatKingGuy"+ChatColor.GOLD+"\n" +
                " _\\$$$$$$\\| $$  | $$| $$  | $$| $$  | $$| $$ \\$$$$| $$  | $$  | $$  "+ChatColor.WHITE+"v"+ChatColor.GREEN+getDescription().getVersion()+ChatColor.GOLD +"\n"+
                "|  \\__| $$| $$  | $$| $$__/ $$| $$__/ $$| $$__| $$| $$__/ $$ _| $$_ \n" +
                " \\$$    $$| $$  | $$ \\$$    $$| $$    $$ \\$$    $$ \\$$    $$|   $$ \\\n" +
                "  \\$$$$$$  \\$$   \\$$  \\$$$$$$ | $$$$$$$   \\$$$$$$   \\$$$$$$  \\$$$$$$\n" +
                "                              | $$                                  \n" +
                "                              | $$                                  \n" +
                "                               \\$$                                  ";
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD+txt);
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        loadShopPages();
        if(!setupEconomy()){
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED+"["+ getDescription().getName()+"] Cannot find vault! Shutting down server...");
            Bukkit.shutdown();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(label.equalsIgnoreCase("shop")){
            if(sender instanceof Player){
                Player player = (Player) sender;
                player.openInventory(getMainPage());
            }
        }
        return true;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public void loadShopPages(){
        ConfigurationSection section = getConfig().getConfigurationSection("shops");
        Set<String> shops = section.getKeys(false);


        for(String shop : shops){

            Material icon = Material.getMaterial(getConfig().getString("shops."+shop + ".items.1.item.material"));
            String name = shop;
            String title = color(getConfig().getString("shops." + name + ".name"));
            List<ShopItem> items = new ArrayList<>();

            for(String s : getConfig().getConfigurationSection("shops."+name+".items").getKeys(false)){

                int slot = Integer.valueOf(s);
                int amount = 1;
                if(getConfig().getString("shops." + name + "." + s + ".item.quantity") != null) {
                    amount = Integer.valueOf(getConfig().getString("shops." + name + ".items." + s + ".item.quantity"));
                }
                if(Material.getMaterial(getConfig().getString("shops." + name + ".items." + s + ".item.material")) == null) {
                 Bukkit.getLogger().log(Level.SEVERE, "INVALID MAT " + getConfig().getString("shops." + name + ".items." + s + ".item.material") + " TRIED "+"shops." + name + "." + s + ".item.material");
                }else {
                    ItemStack item = new ItemStack(Material.getMaterial(getConfig().getString("shops." + name + ".items." + s + ".item.material")), amount);
                    float sellPrice = Float.valueOf(getConfig().getString("shops." + name + ".items." + s + ".sellPrice"));
                    float buyPrice = Float.valueOf(getConfig().getString("shops." + name + ".items." + s + ".buyPrice"));

                    ShopItem i = new ShopItem(item, buyPrice, sellPrice, slot);
                    //Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN+"made item " + item.getType().toString() + " with bprice " + buyPrice);
                    items.add(i);
                }
            }

            ShopPage page = new ShopPage(name, icon, items, title);
            pages.add(page);



        }

    }


    public Inventory getMainPage(){
        Inventory menu = Bukkit.createInventory(null, 27, color("&a&lShop Menu"));
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE,1);
        ItemMeta fm = filler.getItemMeta();
        fm.setDisplayName(" ");
        filler.setItemMeta(fm);

        for(int i = 0; i < 9; i++){
            menu.setItem(i,filler);
        }


        for(ShopPage page : pages){
            ItemStack icon = new ItemStack(page.getIcon());
            ItemMeta im = icon.getItemMeta();
            String str = page.getName();
            im.setDisplayName(color("&d&l"+str.substring(0, 1).toUpperCase() + str.substring(1)));
            List<String> lore = new ArrayList<>();
            lore.add(color("&7Click to view page"));
            im.setLore(lore);
            icon.setItemMeta(im);

            menu.addItem(icon);
        }

        for(int i = 18; i < 27; i++){
            menu.setItem(i,filler);
        }

        return menu;
    }

    public void openPage(ShopPage page, Player player, int pagenum){
        String str = page.getName();
        Inventory menu = Bukkit.createInventory(null, 54, page.getTitle().replace("%page%", pagenum+""));

        ItemStack exitItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta exitMeta = exitItem.getItemMeta();
        exitMeta.setDisplayName(color("&a&lBack to categories"));
        List<String> exit_lore = new ArrayList<>();
        exit_lore.add(color("&7Click here to return"));
        exitMeta.setLore(exit_lore);
        exitItem.setItemMeta(exitMeta);

        ItemStack balance = itemCreator(Material.GOLD_NUGGET, ChatColor.GRAY+""+econ.getBalance(player), "&6&lYour Balance",1);

        for(ShopItem item : page.getItems()){
            ItemStack i = item.getItem();
            ItemMeta im = i.getItemMeta();
            List<String> lore = new ArrayList<>();
            if(item.getBuyPrice() != 0){
                lore.add(color("&7Buy price: &c$"+item.getBuyPrice()));
            }
            if(item.getSellPrice() != 0){
                lore.add(color("&7Sell price: &a$"+item.getSellPrice()));
                lore.add(color("&9Click with MMB to sell all"));
            }
            im.setLore(lore);
            i.setItemMeta(im);
            menu.setItem(item.getSlot()-1, i);

        }

        menu.setItem(53, balance);
        menu.setItem(49, exitItem);

        player.openInventory(menu);
    }

    public ShopPage getPage(Inventory inv){

        for(ShopPage page : pages){
            String str = page.getName();
            Inventory menu = Bukkit.createInventory(null, 54, page.getTitle());

            ItemStack exitItem = new ItemStack(Material.NETHER_STAR);
            ItemMeta exitMeta = exitItem.getItemMeta();
            exitMeta.setDisplayName(color("&a&lBack to categories"));
            List<String> exit_lore = new ArrayList<>();
            exit_lore.add(color("&7Click here to return"));
            exitMeta.setLore(exit_lore);
            exitItem.setItemMeta(exitMeta);

            for(ShopItem item : page.getItems()){
                ItemStack i = item.getItem();
                ItemMeta im = i.getItemMeta();
                List<String> lore = new ArrayList<>();
                if(item.getBuyPrice() != 0){
                    lore.add(color("&7Buy price: &c$"+item.getBuyPrice()));
                }
                if(item.getSellPrice() != 0){
                    lore.add(color("&7Sell price: &a$"+item.getSellPrice()));
                    lore.add(color("&9Click with MMB to sell all"));
                }
                im.setLore(lore);
                i.setItemMeta(im);
                menu.setItem(item.getSlot()-1, i);

            }

            menu.setItem(49, exitItem);

            if(inv.getContents().equals(menu.getContents())){
                return page;
            }
        }
        return null;
    }

    public void showAmountPage(Player player, Material mat, int amount, float price){
        Inventory menu = Bukkit.createInventory(null, 54, color("&aBuying &b&l"+mat.toString()));

        ItemStack currentItem = itemCreator(mat,color("&7Buy price: &c$"+price), amount);

        ItemStack set1 = itemCreator(Material.RED_STAINED_GLASS_PANE,64,"&c&lSet to 1");
        ItemStack remove10 = itemCreator(Material.RED_STAINED_GLASS_PANE,10,"&c&lRemove 10");
        ItemStack remove1 = itemCreator(Material.RED_STAINED_GLASS_PANE,1,"&c&lRemove 1");

        ItemStack set64 = itemCreator(Material.LIME_STAINED_GLASS_PANE,64,"&a&lSet to 64");
        ItemStack add10 = itemCreator(Material.LIME_STAINED_GLASS_PANE,10,"&a&lAdd 10");
        ItemStack add1 = itemCreator(Material.LIME_STAINED_GLASS_PANE,1,"&a&lAdd 1");

        ItemStack confirm = itemCreator(Material.LIME_STAINED_GLASS,1,"&a&lConfirm");
        ItemStack cancel = itemCreator(Material.RED_STAINED_GLASS,1,"&c&lCancel");

        menu.setItem(22,currentItem);
        menu.setItem(39,cancel);
        menu.setItem(41,confirm);

        if(amount < 64){
            menu.setItem(24,add1);
        }
        if(amount < 55){
            menu.setItem(25,add10);
        }
        if(amount != 64){
            menu.setItem(26,set64);
        }

        if(amount > 1){
            menu.setItem(20,remove1);
        }
        if(amount > 10){
            menu.setItem(19,remove10);
        }
        if(amount != 1){
            menu.setItem(18,set1);
        }

        player.openInventory(menu);
    }

    public void showSellPage(Player player, Material mat, int amount, float price){
        Inventory menu = Bukkit.createInventory(null, 54, color("&aSelling &b&l"+mat.toString()));

        ItemStack currentItem = itemCreator(mat,color("&7Sell price: &a$"+price), amount);

        ItemStack set1 = itemCreator(Material.RED_STAINED_GLASS_PANE,64,"&c&lSet to 1");
        ItemStack remove10 = itemCreator(Material.RED_STAINED_GLASS_PANE,10,"&c&lRemove 10");
        ItemStack remove1 = itemCreator(Material.RED_STAINED_GLASS_PANE,1,"&c&lRemove 1");

        ItemStack set64 = itemCreator(Material.LIME_STAINED_GLASS_PANE,64,"&a&lSet to 64");
        ItemStack add10 = itemCreator(Material.LIME_STAINED_GLASS_PANE,10,"&a&lAdd 10");
        ItemStack add1 = itemCreator(Material.LIME_STAINED_GLASS_PANE,1,"&a&lAdd 1");

        ItemStack confirm = itemCreator(Material.LIME_STAINED_GLASS,1,"&a&lConfirm");
        ItemStack cancel = itemCreator(Material.RED_STAINED_GLASS,1,"&c&lCancel");

        menu.setItem(22,currentItem);
        menu.setItem(39,cancel);
        menu.setItem(41,confirm);

        if(amount < 64){
            menu.setItem(24,add1);
        }
        if(amount < 55){
            menu.setItem(25,add10);
        }
        if(amount != 64){
            menu.setItem(26,set64);
        }

        if(amount > 1){
            menu.setItem(20,remove1);
        }
        if(amount > 10){
            menu.setItem(19,remove10);
        }
        if(amount != 1){
            menu.setItem(18,set1);
        }

        player.openInventory(menu);
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent event){
        if(event.getView().getTitle().equals(color("&a&lShop Menu"))){
            event.setCancelled(true);
            if(event.getSlot() != 999){
                if(event.getCurrentItem() != null){
                    if(event.getCurrentItem().getItemMeta() != null) {
                        Player player = (Player) event.getWhoClicked();
                        ItemStack clickedItem = event.getCurrentItem();
                        String itemName = clickedItem.getItemMeta().getDisplayName();

                        for (ShopPage page : pages) {

                            String str = page.getName();
                            if(itemName.equalsIgnoreCase(color("&d&l"+str.substring(0, 1).toUpperCase() + str.substring(1)))){
                                openPage(page, player, 1);
                            }
                        }
                    }
                }
            }
        }
        else{
            if(event.getSlot() != 999){
                if(event.getCurrentItem() != null) {
                    if (event.getCurrentItem().getItemMeta() != null) {
                        ItemStack clickedItem = event.getCurrentItem();
                        String itemName = clickedItem.getItemMeta().getDisplayName();



                        if(itemName.equalsIgnoreCase(color("&a&lBack to categories"))){
                            event.setCancelled(true);
                            Player player = (Player) event.getWhoClicked();
                            player.closeInventory();
                            player.openInventory(getMainPage());
                        }

                        if(itemName.equalsIgnoreCase(color("&6&lYour Balance"))){
                            event.setCancelled(true);
                        }

                        if(event.getView().getTitle().contains(color("&aBuying"))) {
                            event.setCancelled(true);

                            if (itemName.equalsIgnoreCase(color("&c&lCancel"))) {
                                Player player = (Player) event.getWhoClicked();
                                player.closeInventory();
                                player.openInventory(getMainPage());
                            }

                            if (itemName.equalsIgnoreCase(color("&a&lConfirm"))) {
                                Player player = (Player) event.getWhoClicked();
                                List<String> lore = event.getClickedInventory().getItem(22).getItemMeta().getLore();
                                Material mat = event.getClickedInventory().getItem(22).getType();
                                int amount = event.getClickedInventory().getItem(22).getAmount();
                                Bukkit.getLogger().info(amount+"");
                                float buyprice = getPrice(lore, 1);


                                if(econ.getBalance(player) >= buyprice){
                                    econ.withdrawPlayer(player, buyprice);
                                    player.sendMessage(format("You bought &c"+amount+" x "+mat.toString()+" &rfor &a$"+buyprice+"&r."));

                                    final ItemStack yourItem = new ItemStack(mat, amount); // Your itemstack
                                    final Map<Integer, ItemStack> map = player.getInventory().addItem(yourItem); // Attempt to add in inventory
                                    if (!map.isEmpty()) { // If not empty, it means the player's inventory is full.
                                        map.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
                                    }

                                }else {
                                    player.sendMessage(format("You do not have &c$"+buyprice+" &rto spend!"));
                                }
                            }



                            if (itemName.equalsIgnoreCase(color("&c&lSet to 1"))) {
                                Player player = (Player) event.getWhoClicked();
                                Material mat = event.getClickedInventory().getItem(22).getType();
                                int amount = 1;

                                List<String> lore = event.getClickedInventory().getItem(22).getItemMeta().getLore();
                                showAmountPage(player, mat, amount, getPrice(lore, event.getClickedInventory().getItem(22).getAmount())*amount);
                            }

                            if (itemName.equalsIgnoreCase(color("&c&lRemove 1"))) {
                                Player player = (Player) event.getWhoClicked();
                                Material mat = event.getClickedInventory().getItem(22).getType();
                                int amount = event.getClickedInventory().getItem(22).getAmount() -1;

                                List<String> lore = event.getClickedInventory().getItem(22).getItemMeta().getLore();
                                showAmountPage(player, mat, amount, getPrice(lore, event.getClickedInventory().getItem(22).getAmount())*amount);
                            }

                            if (itemName.equalsIgnoreCase(color("&c&lRemove 10"))) {
                                Player player = (Player) event.getWhoClicked();
                                Material mat = event.getClickedInventory().getItem(22).getType();
                                int amount = event.getClickedInventory().getItem(22).getAmount() -10;

                                List<String> lore = event.getClickedInventory().getItem(22).getItemMeta().getLore();
                                showAmountPage(player, mat, amount, getPrice(lore, event.getClickedInventory().getItem(22).getAmount())*amount);
                            }

                            if (itemName.equalsIgnoreCase(color("&a&lSet to 64"))) {
                                Player player = (Player) event.getWhoClicked();
                                Material mat = event.getClickedInventory().getItem(22).getType();
                                int amount = 64;
                                List<String> lore = event.getClickedInventory().getItem(22).getItemMeta().getLore();
                                showAmountPage(player, mat, amount, getPrice(lore, event.getClickedInventory().getItem(22).getAmount())*amount);
                            }

                            if (itemName.equalsIgnoreCase(color("&a&lAdd 1"))) {
                                Player player = (Player) event.getWhoClicked();
                                Material mat = event.getClickedInventory().getItem(22).getType();
                                int amount = event.getClickedInventory().getItem(22).getAmount() +1;

                                List<String> lore = event.getClickedInventory().getItem(22).getItemMeta().getLore();
                                showAmountPage(player, mat, amount, getPrice(lore, event.getClickedInventory().getItem(22).getAmount())*amount);
                            }

                            if (itemName.equalsIgnoreCase(color("&a&lAdd 10"))) {
                                Player player = (Player) event.getWhoClicked();
                                Material mat = event.getClickedInventory().getItem(22).getType();
                                int amount = event.getClickedInventory().getItem(22).getAmount() +10;

                                List<String> lore = event.getClickedInventory().getItem(22).getItemMeta().getLore();
                                showAmountPage(player, mat, amount, getPrice(lore, event.getClickedInventory().getItem(22).getAmount())*amount);
                            }

                        }

                        if(event.getView().getTitle().contains(color("&aSelling"))) {
                            event.setCancelled(true);

                            if (itemName.equalsIgnoreCase(color("&c&lCancel"))) {
                                Player player = (Player) event.getWhoClicked();
                                player.closeInventory();
                                player.openInventory(getMainPage());
                            }

                            if (itemName.equalsIgnoreCase(color("&a&lConfirm"))) {
                                Player player = (Player) event.getWhoClicked();
                                List<String> lore = event.getClickedInventory().getItem(22).getItemMeta().getLore();
                                Material mat = event.getClickedInventory().getItem(22).getType();
                                int amount = event.getClickedInventory().getItem(22).getAmount();
                                Bukkit.getLogger().info(amount+"");
                                float sellprice = getSellPrice(lore, 1);

                                if(getAmount(player, mat) >= amount){
                                    player.sendMessage(format("Sold &a"+amount+" x "+mat.toString()+" &rfor &a$"+(sellprice/amount)+"&r each."));
                                    econ.depositPlayer(player, sellprice);
                                    removeItems(player, mat, amount);
                                }else{
                                    player.sendMessage(format("You do not have &c"+amount+" x "+mat.toString()+"!"));
                                }



                                /*if(econ.getBalance(player) >= buyprice){
                                    econ.withdrawPlayer(player, buyprice);
                                    player.sendMessage(format("You bought &c"+amount+" x "+mat.toString()+" &rfor &a$"+buyprice+"&r."));

                                    final ItemStack yourItem = new ItemStack(mat, amount); // Your itemstack
                                    final Map<Integer, ItemStack> map = player.getInventory().addItem(yourItem); // Attempt to add in inventory
                                    if (!map.isEmpty()) { // If not empty, it means the player's inventory is full.
                                        map.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
                                    }

                                }else {
                                    player.sendMessage(format("You do not have &c$"+buyprice+" &rto spend!"));
                                } */
                            }



                            if (itemName.equalsIgnoreCase(color("&c&lSet to 1"))) {
                                Player player = (Player) event.getWhoClicked();
                                Material mat = event.getClickedInventory().getItem(22).getType();
                                int amount = 1;

                                List<String> lore = event.getClickedInventory().getItem(22).getItemMeta().getLore();
                                showSellPage(player, mat, amount, getSellPrice(lore, event.getClickedInventory().getItem(22).getAmount())*amount);
                            }

                            if (itemName.equalsIgnoreCase(color("&c&lRemove 1"))) {
                                Player player = (Player) event.getWhoClicked();
                                Material mat = event.getClickedInventory().getItem(22).getType();
                                int amount = event.getClickedInventory().getItem(22).getAmount() -1;

                                List<String> lore = event.getClickedInventory().getItem(22).getItemMeta().getLore();
                                showSellPage(player, mat, amount, getSellPrice(lore, event.getClickedInventory().getItem(22).getAmount())*amount);
                            }

                            if (itemName.equalsIgnoreCase(color("&c&lRemove 10"))) {
                                Player player = (Player) event.getWhoClicked();
                                Material mat = event.getClickedInventory().getItem(22).getType();
                                int amount = event.getClickedInventory().getItem(22).getAmount() -10;

                                List<String> lore = event.getClickedInventory().getItem(22).getItemMeta().getLore();
                                showSellPage(player, mat, amount, getSellPrice(lore, event.getClickedInventory().getItem(22).getAmount())*amount);
                            }

                            if (itemName.equalsIgnoreCase(color("&a&lSet to 64"))) {
                                Player player = (Player) event.getWhoClicked();
                                Material mat = event.getClickedInventory().getItem(22).getType();
                                int amount = 64;
                                List<String> lore = event.getClickedInventory().getItem(22).getItemMeta().getLore();
                                showSellPage(player, mat, amount, getSellPrice(lore, event.getClickedInventory().getItem(22).getAmount())*amount);
                            }

                            if (itemName.equalsIgnoreCase(color("&a&lAdd 1"))) {
                                Player player = (Player) event.getWhoClicked();
                                Material mat = event.getClickedInventory().getItem(22).getType();
                                int amount = event.getClickedInventory().getItem(22).getAmount() +1;

                                List<String> lore = event.getClickedInventory().getItem(22).getItemMeta().getLore();
                                showSellPage(player, mat, amount, getSellPrice(lore, event.getClickedInventory().getItem(22).getAmount())*amount);
                            }

                            if (itemName.equalsIgnoreCase(color("&a&lAdd 10"))) {
                                Player player = (Player) event.getWhoClicked();
                                Material mat = event.getClickedInventory().getItem(22).getType();
                                int amount = event.getClickedInventory().getItem(22).getAmount() +10;

                                List<String> lore = event.getClickedInventory().getItem(22).getItemMeta().getLore();
                                showSellPage(player, mat, amount, getSellPrice(lore, event.getClickedInventory().getItem(22).getAmount())*amount);
                            }

                        }

                        if(clickedItem.getItemMeta().getLore() != null){
                            List<String> lore = clickedItem.getItemMeta().getLore();
                            for(int i =0;i < lore.size(); i++){
                                String str = lore.get(i);
                                str = ChatColor.stripColor(str);
                                lore.set(i, str);
                            }
                            for(String line : lore){
                                if(line.contains("Buy price:") || line.contains("Sell price:")){

                                    float bprice = 0;
                                    float sprice = 0;
                                    if(lore.get(0).contains("Buy price:")){
                                        bprice = Float.valueOf(lore.get(0).substring(12));
                                    }
                                    if(lore.size() > 1) {
                                        if (lore.get(1).contains("Sell price:")) {
                                            sprice = Float.valueOf(lore.get(1).substring(13));
                                        }
                                        if (lore.get(1).contains("Sell price:")) {
                                            sprice = Float.valueOf(lore.get(1).substring(13));
                                        }

                                    }

                                    if(!event.getView().getTitle().contains(color("&aBuying")) || !event.getView().getTitle().contains(color("&aSelling"))) {

                                            Player player = (Player) event.getWhoClicked();
                                            //player.sendMessage("bought "+clickedItem.getType()+" price " +bprice+" sellp "+ sprice);
                                            if (event.getClick() == ClickType.LEFT) {
                                                if (bprice != 0) {
                                                    showAmountPage(player, clickedItem.getType(), 1, bprice);
                                                }
                                            } else if (event.getClick() == ClickType.RIGHT) {
                                                if (sprice != 0) {
                                                    showSellPage(player, clickedItem.getType(), 1, sprice);
                                                }
                                            } else if (event.getClick() == ClickType.MIDDLE) {
                                                if (sprice != 0) {
                                                    if(getAmount(player, clickedItem.getType()) != 0){
                                                        int items = getAmount(player, clickedItem.getType());
                                                        float stotal = sprice * items;
                                                        removeItems(player, clickedItem.getType(), items);
                                                        player.sendMessage(format("Sold &a"+items+" x "+clickedItem.getType().toString()+" &rfor &a$"+sprice+"&r each."));
                                                        econ.depositPlayer(player, stotal);
                                                    }else{
                                                        player.sendMessage(format("You have no &a"+clickedItem.getType().toString()+"&r to sell!"));

                                                    }
                                                }
                                            }
                                        }
                                    event.setCancelled(true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    public float getPrice(List<String> lore, int amount){
            for (int i = 0; i < lore.size(); i++) {
                String str = lore.get(i);
                str = ChatColor.stripColor(str);
                lore.set(i, str);
            }
            for (String line : lore) {
                if (line.contains("Buy price:")) {

                    return Float.valueOf(line.substring(12))/amount;
                }
            }
            return 0;
    }

    public float getSellPrice(List<String> lore, int amount){
        for (int i = 0; i < lore.size(); i++) {
            String str = lore.get(i);
            str = ChatColor.stripColor(str);
            lore.set(i, str);
        }
        for (String line : lore) {
            if (line.contains("Sell price:")) {

                return Float.valueOf(line.substring(13))/amount;
            }
        }
        return 0;
    }

    public ItemStack itemCreator(Material mat, int amount, String name){
        ItemStack item = new ItemStack(mat,amount);
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(color(name));
        item.setItemMeta(im);
        return item;
    }

    public ItemStack itemCreator(Material mat, String lore1, int amount){
        ItemStack item = new ItemStack(mat,amount);
        ItemMeta im = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(lore1);
        im.setLore(lore);
        item.setItemMeta(im);
        return item;
    }

    public ItemStack itemCreator(Material mat, String lore1, String name, int amount){
        ItemStack item = new ItemStack(mat,amount);
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(color(name));
        List<String> lore = new ArrayList<>();
        lore.add(lore1);
        im.setLore(lore);
        item.setItemMeta(im);
        return item;
    }

    public String formatName(String mat){
        mat = mat.toLowerCase();
        String[] names = mat.split("_");
        List<String> caps = new ArrayList<>();
        for(String name : names){
            caps.add(name.substring(0, 1).toUpperCase() + name.substring(1));
        }
        String finalstr ="";

        for(String str : caps){
            if(finalstr.length() == 0){
                finalstr.concat(str);
            }else{
                finalstr.concat(" ");
                finalstr.concat(str);
            }
        }

        return finalstr;
    }

    public static int getAmount(Player arg0, Material material) {
        if (material == null)
            return 0;
        int amount = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack slot = arg0.getInventory().getItem(i);
            if (slot == null || !(slot.getType() == material))
                continue;
            amount += slot.getAmount();
        }
        return amount;
    }

    public void removeItems(Player arg0, Material material, int amount) {
        if (material == null)
            return;
        int taken = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack slot = arg0.getInventory().getItem(i);
            if (slot == null || !(slot.getType() == material))
                continue;
            if(taken == amount)
                return;

            if(taken + slot.getAmount() >= amount){
                //arg0.sendMessage("took "+(amount-taken));
                slot.setAmount(slot.getAmount()-(amount-taken));
                taken += amount-taken;

            }else{
                taken += slot.getAmount();
               // arg0.sendMessage("took "+slot.getAmount());
                slot.setAmount(0);

            }
        }

    }
}
