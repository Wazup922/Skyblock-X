package me.wazup.skyblock;

import me.wazup.skyblock.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlayerCondition {

    private Location location;
    private double maxhealth;
    private double health;
    private int food;
    private int level;
    private float exp;
    private GameMode gameMode;
    private boolean flying;
    private Collection<PotionEffect> effects;
    private ItemStack[] items;
    private ItemStack[] armor;
    private Scoreboard scoreboard;

    public void extractCondition(Player p){
        location = p.getLocation();
        maxhealth = p.getMaxHealth();
        health = p.getHealth();
        food = p.getFoodLevel();
        level = p.getLevel();
        exp = p.getExp();
        gameMode = p.getGameMode();
        flying = p.isFlying();
        effects = p.getActivePotionEffects();
        items = p.getInventory().getContents();
        armor = p.getInventory().getArmorContents();
        scoreboard = p.getScoreboard();
    }

    public void applyCondition(Player p){
        p.teleport(location);
        p.setFallDistance(0);
        p.setMaxHealth(maxhealth);
        p.setHealth(health);
        p.setFoodLevel(food);
        p.setLevel(level);
        p.setExp(exp);
        p.setGameMode(gameMode);
        if(flying){
            p.setAllowFlight(true);
            p.setFlying(true);
        }
        for(PotionEffect effect: p.getActivePotionEffects()){
            p.removePotionEffect(effect.getType());
        }
        p.addPotionEffects(effects);
        p.getInventory().setContents(items);
        p.getInventory().setArmorContents(armor);
        p.updateInventory();
        if(scoreboard != null) p.setScoreboard(scoreboard);
    }

    public void saveConditionToFile(Player p){
        PlayerData data = Skyblock.getInstance().playerData.get(p.getUniqueId());

        File f = new File(Skyblock.getInstance().getDataFolder() + "/players/" + data.saveName, "condition");
        FileConfiguration editor = YamlConfiguration.loadConfiguration(f);

        editor.set("Location", Utils.getStringFromExactLocation(location));
        editor.set("Max-Health", maxhealth);
        editor.set("Health", health);
        editor.set("Food", food);
        editor.set("Level", level);
        editor.set("Exp", exp);
        editor.set("GameMode", gameMode.name());
        editor.set("Flying", flying);
        editor.set("Inventory.Items", items);
        editor.set("Inventory.Armor", armor);
        List<String> potionEffects = new ArrayList<>();
        for(PotionEffect pe: p.getActivePotionEffects()){
            potionEffects.add(pe.getType().getName() + " : " + pe.getDuration() + " : " + pe.getAmplifier());
        }
        editor.set("Potion-Effects", potionEffects);

        try {
            editor.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean loadConditionFromFile(Player p){
        PlayerData data = Skyblock.getInstance().playerData.get(p.getUniqueId());

        File f = new File(Skyblock.getInstance().getDataFolder() + "/players/" + data.saveName, "condition");
        if(!f.exists()) return false;

        FileConfiguration editor = YamlConfiguration.loadConfiguration(f);

        location = Utils.getLocationFromString(editor.getString("Location"));
        maxhealth = editor.getDouble("Max-Health");
        health = editor.getDouble("Health");
        food = editor.getInt("Food");
        level = editor.getInt("Level");
        exp = editor.getInt("Exp");
        gameMode = GameMode.valueOf(editor.getString("GameMode"));
        flying = editor.getBoolean("Flying");

        List<ItemStack> temp_list = (List<ItemStack>) editor.getList("Inventory.Items");
        items = temp_list.toArray(new ItemStack[temp_list.size()]);
        temp_list = (List<ItemStack>) editor.getList("Inventory.Armor");
        armor = temp_list.toArray(new ItemStack[temp_list.size()]);

        effects = new ArrayList<>();
        for(String effect: editor.getStringList("Potion-Effects")){
            String[] splitter = effect.split(" : ");
            String name = splitter[0];
            int duration = Integer.parseInt(splitter[1]);
            int amp = Integer.parseInt(splitter[2]);
            effects.add(new PotionEffect(PotionEffectType.getByName(name), duration, amp));
        }

        return true;
    }

}
