package me.wazup.skyblock.utils;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public class WarpContainer {

    public HashMap<String, Warp> warps;
    public FlexibleSmartInventory inventory;

    public WarpContainer(){
        warps = new HashMap<>();

        inventory = new FlexibleSmartInventory("Server Warps");
        inventory.addInventory("", FlexibleSmartInventory.InventorySize.SMALL, null);
    }

    public void addWarp(String name, String displayedName, Location location, ItemStack item, List<String> description){
        ItemStackBuilder builder = new ItemStackBuilder(item);
        builder.setName(displayedName).addLore(description);
        FlexibleSmartInventory.PositionData pd = inventory.addItem(builder.build(), 0);

        Warp warp = new Warp(displayedName, location, pd);
        warps.put(name, warp);
    }

    public void removeWarp(String warpName){
        Warp warp = warps.get(warpName);
        warps.remove(warpName);
        inventory.removeItem(warp.positionData);
    }

    public Warp getWarp(String displayedName){
        for(Warp warp: warps.values()) if(warp.displayedName.equals(displayedName)) return warp;
        return null;
    }

    public class Warp {

        String displayedName;
        public Location location;
        FlexibleSmartInventory.PositionData positionData;

        private Warp(String displayedName, Location location, FlexibleSmartInventory.PositionData positionData){
            this.displayedName = displayedName;
            this.location = location;
            this.positionData = positionData;
        }

    }

}
