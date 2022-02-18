package me.wazup.skyblock.managers;

import me.wazup.skyblock.Skyblock;
import me.wazup.skyblock.utils.Cuboid;
import me.wazup.skyblock.utils.XMaterial;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;

public class ThemeManager {

    public HashMap<String, Theme> themes;

    private static ThemeManager instance;
    public static ThemeManager getInstance(){
        return instance;
    }

    public ThemeManager(){
        instance = this;

        themes = new HashMap<>();

        File mainDirectory = new File(Skyblock.getInstance().getDataFolder(), "themes");
        if(mainDirectory.exists()){
            for(File themeFolder: mainDirectory.listFiles()){
                if(themeFolder.isDirectory()){
                    String themeName = themeFolder.getName();
                    File saveFile = new File(Skyblock.getInstance().getDataFolder() + "/themes/" + themeName, "blocks");
                    if(saveFile.exists()) themes.put(themeName.toLowerCase(), new Theme(themeName));
                }
            }
        }

    }

    public boolean createTheme(Cuboid cuboid, String name, Player p) {
        ArrayList<String> blocks = new ArrayList<>();
        Iterator<Block> iterator = cuboid.iterator();

        String spawnLocation = null;
        while(iterator.hasNext()) {
            String coordinates = iterator.toString();
            Block b = iterator.next();
            if(spawnLocation == null && b.getLocation().equals(p.getLocation().add(0, -1, 0).getBlock().getLocation())) spawnLocation = coordinates;
            if(b.getType() != Material.AIR) blocks.add(coordinates + ":" + b.getType());
        }

        if(spawnLocation == null){
            p.sendMessage(Customization.getInstance().prefix + ChatColor.RED + "Your position indicates the spawnpoint of the island! You must stand inside the theme region");
            return false;
        }

        spawnLocation += ":" + p.getLocation().getYaw() + ":" + p.getLocation().getPitch();

        File saveFile = new File(Skyblock.getInstance().getDataFolder() + "/themes/" + name, "blocks");
        FileConfiguration editor = YamlConfiguration.loadConfiguration(saveFile);

        editor.set("Spawn", spawnLocation);
        editor.set("Blocks", blocks);
        editor.set("Width", cuboid.getWidth());
        editor.set("Length", cuboid.getLength());
        try {
            editor.save(saveFile);
            themes.put(name.toLowerCase(), new Theme(name));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public class Theme {

        public String name;

        HashMap<CLocation, Material> blocks;
        int width, length;
        Location spawn; //Holds x, y, z, yaw and pitch (NO WORLD)

        public Theme(String name){
            this.name = name;

            File saveFile = new File(Skyblock.getInstance().getDataFolder() + "/themes/" + name, "blocks");
            FileConfiguration editor = YamlConfiguration.loadConfiguration(saveFile);

            String[] spliter = editor.getString("Spawn").split(":");
            spawn = new Location(null, Float.parseFloat(spliter[0]) - 0.5f, Float.parseFloat(spliter[1]) + 1f, Float.parseFloat(spliter[2]) - 0.5f, Float.parseFloat(spliter[3]), Float.parseFloat(spliter[4]));

            blocks = new HashMap<>();
            String[] blockSpliter = editor.getString("Blocks").replace("[", "").replace("]", "").split(", ");
            for(String block: blockSpliter){
                spliter = block.split(":");
                Optional<XMaterial> material = XMaterial.matchXMaterial(spliter[3]);
                if(!material.isPresent()) continue;
                int x = Integer.parseInt(spliter[0]), y = Integer.parseInt(spliter[1]), z = Integer.parseInt(spliter[2]);

                blocks.put(new CLocation(x, y, z), material.get().parseMaterial());
            }

            width = editor.getInt("Width");
            length = editor.getInt("Length");
        }

        public void build(Location centerBlock){
            World w = centerBlock.getWorld();
            float subtractWidth = width / 2f;
            float subtractLength = length / 2f;
            for(CLocation cl: blocks.keySet()) {
                Location l = new Location(w, centerBlock.getBlockX() + cl.x - subtractWidth, centerBlock.getBlockY() + cl.y, centerBlock.getBlockZ() + cl.z - subtractLength);
                Block b = w.getBlockAt(l);
                b.setType(blocks.get(cl), false);
            }
        }

        public Location getSpawn(Location centerBlock){
            float subtractWidth = width / 2f;
            float subtractLength = length / 2f;
            Location l = centerBlock.clone().add(spawn.getX() - subtractWidth, spawn.getY(), spawn.getZ() - subtractLength);
            l.setYaw(spawn.getYaw());
            l.setPitch(spawn.getPitch());
            return l;
        }

        private class CLocation {
            int x, y, z;

            private CLocation(int x, int y, int z){
                this.x = x;
                this.y = y;
                this.z = z;
            }

//            private CLocation(Location l){
//                this.x = l.getBlockX();
//                this.y = l.getBlockY();
//                this.z = l.getBlockZ();
//            }

            public boolean equals(Object obj){
                CLocation cl = (CLocation) obj;
                return x == cl.x && y == cl.y && z == cl.z;
            }

            public int hashCode(){
                return (x + "" + y + "" + z).hashCode();
            }

        }

    }

}
