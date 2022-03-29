package me.wazup.skyblock;

import me.wazup.skyblock.managers.Config;
import me.wazup.skyblock.managers.Customization;
import me.wazup.skyblock.skills.Skill;
import me.wazup.skyblock.utils.Enums;
import me.wazup.skyblock.utils.ItemStackBuilder;
import me.wazup.skyblock.utils.Utils;
import net.minecraft.util.CubicSampler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class PlayerData {

    public int islandID;

    public HashMap<Enums.Statistic, Skill> statistics = new HashMap<>();

    public String saveName;
    public boolean shouldSave;

    PlayerCondition outsideCondition;

    public Location[] adminSelection;

    public PlayerData(Player p){
        saveName = Config.getInstance().usePlayersUUIDForSavingData ? p.getUniqueId().toString() : p.getName();

        loadData();
    }

    public void loadData(){
        new BukkitRunnable(){
            public void run(){

                //Flatfile
                File saveFile = new File(Skyblock.getInstance().getDataFolder() + "/players/" + saveName, "data");
                if(saveFile.exists()){
                    FileConfiguration editor = YamlConfiguration.loadConfiguration(saveFile);
                    islandID = editor.getInt("Island-ID");
                    for(Enums.Statistic s: Enums.Statistic.values()){
                        int score = editor.getInt("Statistics." + s.name(), 0);
                        statistics.put(s, new Skill(s, score));
                    }
                } else {
                    islandID = -1;
                    for(Enums.Statistic s: Enums.Statistic.values()){
                        statistics.put(s, new Skill(s, 0));
                    }
                }

            }
        }.runTaskAsynchronously(Skyblock.getInstance());

    }

    public void saveData(Player p, boolean async){
        if(!shouldSave) return;

        if(async){ //Calls itself again in an async way
            new BukkitRunnable(){
                public void run(){
                    saveData(p, false);
                }
            }.runTaskAsynchronously(Skyblock.getInstance());
            return;
        }

        File f = new File(Skyblock.getInstance().getDataFolder() + "/players/" + saveName, "data");
        FileConfiguration editor = YamlConfiguration.loadConfiguration(f);
        editor.set("Name", p.getName());
        editor.set("Island-ID", islandID);
        for(Enums.Statistic s: statistics.keySet()){
            editor.set("Statistics." + s.name(), statistics.get(s).score);
        }
        try { editor.save(f); } catch (IOException e) { e.printStackTrace(); }
    }

    public Inventory getStatisticsMenu(boolean withBackButton){
        Customization customization = Customization.getInstance();
        Inventory inv = Bukkit.createInventory(null, 36, customization.inventories.get("Statistics-Menu"));
        Utils.cageInventory(inv, false);
        if(withBackButton) inv.setItem(inv.getSize() - 5, customization.items.get("Back"));

        for(Enums.Statistic statistic: Enums.Statistic.values()){
            String name = statistic.displayedName;
            String score = String.valueOf(statistics.get(statistic).score);
            ItemStackBuilder builder = new ItemStackBuilder(statistic.displayedItem.clone())
            .setName(customization.statisticsMenuItemName.replace("%statistic%", name).replace("%score%", score));
            for(String lore: customization.statisticsMenuItemLore){
                builder.addLore(lore.replace("%statistic%", name).replace("%score%", score));
            }
            inv.addItem(builder.build());
        }

        return inv;
    }

    public Inventory getSkillsMenu(boolean withBackButton){
        Customization customization = Customization.getInstance();
        Inventory inv = Bukkit.createInventory(null, 27, customization.inventories.get("Skills-Menu"));
        Utils.cageInventory(inv, false);
        if(withBackButton) inv.setItem(inv.getSize() - 5, customization.items.get("Back"));

        for(Enums.Statistic statistic: Enums.Statistic.values()){
            if(statistic.skillConfigName == null) continue; //Not a skill
            String name = statistic.skillDisplayedName;
//            String score = String.valueOf(statistics.get(statistic).score);
            ItemStackBuilder builder = new ItemStackBuilder(statistic.displayedItem.clone())
                    .setName(customization.skillsMenuItemName.replace("%skill%", name));
            for(String lore: customization.skillsMenuItemLore){
                if(lore.contains("%description%")){
                    for(String desc: statistic.skillDescription) builder.addLore(desc);
                } else {
                    builder.addLore(lore.replace("%skill%", name));
                }
            }
            inv.addItem(builder.build());
        }

        return inv;
    }

}
