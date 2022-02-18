package me.wazup.skyblock.managers;

import me.wazup.skyblock.Skyblock;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    public String worldName;

    public int distanceBetweenIslands;
    public int islandsYCoordinate;

    public boolean usePlayersUUIDForSavingData;
    public boolean changePlayerConditionsUponJoin;

    private static Config instance;
    public static Config getInstance(){
        return instance;
    }

    public Config(){
        instance = this;

        FileConfiguration file = Skyblock.getInstance().getConfig();

        worldName = file.getString("World-Name");

        distanceBetweenIslands = file.getInt("Distance-Between-Islands");
        islandsYCoordinate = file.getInt("Islands-Y-Coordinate");

        usePlayersUUIDForSavingData = file.getBoolean("Use-Players-UUID-For-Saving-Data");
        changePlayerConditionsUponJoin = file.getBoolean("Change-Player-Conditions-Upon-Join");

    }

}
