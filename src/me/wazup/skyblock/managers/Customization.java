package me.wazup.skyblock.managers;

import me.wazup.skyblock.utils.Enums;
import me.wazup.skyblock.utils.Utils;
import me.wazup.skyblock.utils.XMaterial;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class Customization {

    private static Customization instance;

    public String prefix;

    public HashMap<String, String> messages, inventories;

    public HashMap<String, ItemStack> items;

    public String statisticsMenuItemName, skillsMenuItemName;
    public List<String> statisticsMenuItemLore, skillsMenuItemLore;

    public static Customization getInstance() {
        return instance;
    }

    public Customization() {
        instance = this;

        FileConfiguration file = FilesManager.getInstance().getConfig("customization.yml");

        prefix = c(file.getString("prefix"));

        messages = new HashMap<>();
        for(String key: file.getConfigurationSection("Messages").getKeys(false)){
            messages.put(key, prefix + c(file.getString("Messages." + key)));
        }

        inventories = new HashMap<>();
        for(String key: file.getConfigurationSection("Inventories").getKeys(false)){
            inventories.put(key, c(file.getString("Inventories." + key)));
        }

        items = new HashMap<>();
        for(String key: file.getConfigurationSection("Items").getKeys(false)){
            items.put(key, Utils.getItemStack(file.getString("Items." + key), false, true));
        }

        statisticsMenuItemName = c(file.getString("Statistics-Menu-Format.Item-Name"));
        statisticsMenuItemLore = new ArrayList<>();
        for(String lore: file.getStringList("Statistics-Menu-Format.Item-Lore")){
            statisticsMenuItemLore.add(c(lore));
        }

        skillsMenuItemName = c(file.getString("Skills-Menu-Format.Item-Name"));
        skillsMenuItemLore = new ArrayList<>();
        for(String lore: file.getStringList("Skills-Menu-Format.Item-Lore")){
            skillsMenuItemLore.add(c(lore));
        }

        for(Enums.Statistic statistic: Enums.Statistic.values()){
            statistic.displayedName = file.getString("Statistics." + statistic.name() + ".Displayed-Name");
            Optional<XMaterial> xMaterial = XMaterial.matchXMaterial(file.getString("Statistics." + statistic.name() + ".Displayed-Item"));
            statistic.displayedItem = xMaterial.isPresent() ? xMaterial.get().parseItem() : new ItemStack(Material.PAPER);
        }
    }

    private String c(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }

}
