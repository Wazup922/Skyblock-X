package me.wazup.skyblock;

import me.wazup.skyblock.managers.Config;
import me.wazup.skyblock.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class Island {

    public int id;
    public String displayedName;
    public String displayedOwner;
    Location center;
    int borderSize;

    HashMap<String, Location> warps;

    public Island(FileConfiguration file, int id){
        this.id = id;

        String path = "Islands." + id;

        displayedName = file.getString( path + ".Displayed-Name");
        displayedOwner = file.getString(path + ".Displayed-Owner");

        String[] centerString = file.getString(path + ".Center").split(", ");
        int centerX = Integer.parseInt(centerString[0]);
        int centerZ = Integer.parseInt(centerString[1]);
        center = new Location(Bukkit.getWorld(Config.getInstance().worldName), centerX, 0, centerZ);

        borderSize = 100; //Temp

        warps = new HashMap<>();
        for(String warp: file.getConfigurationSection(path + ".Warps").getKeys(false)){
            Location l = Utils.getLocationFromString(file.getString(path + ".Warps." + warp));
            warps.put(warp, l);
        }
    }

    public void join(Player p){
        p.teleport(center);

        try {
            Utils.createPlayerBorder(p, center, borderSize);
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
    }

}
