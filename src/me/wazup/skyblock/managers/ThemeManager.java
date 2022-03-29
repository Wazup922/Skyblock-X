package me.wazup.skyblock.managers;

import me.wazup.skyblock.Skyblock;
import me.wazup.skyblock.utils.Cuboid;
import me.wazup.skyblock.utils.ReflectionUtils;
import me.wazup.skyblock.utils.Utils;
import me.wazup.skyblock.utils.XMaterial;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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

        File defaultTheme = new File(mainDirectory, "Default");
        if(!defaultTheme.exists()){
            FileConfiguration defaultThemeConfiguration = YamlConfiguration.loadConfiguration(new InputStreamReader(Skyblock.getInstance().getResource("themes/Default")));
            try {
                defaultThemeConfiguration.save(defaultTheme);
            } catch (IOException e) {
                Utils.error("Failed to generate the default theme!");
                e.printStackTrace();
            }
        }

        for(File themeFile: mainDirectory.listFiles()){ //Directory will now always exist.
            themes.put(themeFile.getName().toLowerCase(), new Theme(themeFile));
        }

    }

    public boolean createTheme(Cuboid cuboid, String name, Player p) {
        ArrayList<String> blocks = new ArrayList<>();
        HashMap<String, ItemStack[]> containers = new HashMap<>();
        Iterator<Block> iterator = cuboid.iterator();

        String spawnLocation = null;
        while(iterator.hasNext()) {
            String coordinates = iterator.toString();
            Block b = iterator.next();
            if(spawnLocation == null && b.getLocation().equals(p.getLocation().add(0, -1, 0).getBlock().getLocation())) spawnLocation = coordinates;
            if(b.getType() != Material.AIR) {
//                if(XMaterial.isNewVersion()){
//                    blocks.add(coordinates + ":" + b.getType().name());
//                } else {
//                    blocks.add(coordinates + ":" + b.getType().name() + (b.getData() > 0 ? ":" + b.getData() : ""));
//                }
                if(XMaterial.isNewVersion()){ //In new versions, data is unrelated to material type. you will save it correctly directly.
                    if(b.getBlockData() instanceof Directional){
                        byte direction = Utils.getByteFromFace(((Directional) b.getBlockData()).getFacing());
                        blocks.add(coordinates + ":" + XMaterial.matchXMaterial(b.getType()).name() + ":" + direction);
                    } else {
                        blocks.add(coordinates + ":" + XMaterial.matchXMaterial(b.getType()).name()); //Not a directional
                    }
                } else {
                    Optional<XMaterial> xmaterial = XMaterial.matchXMaterial(b.getType() + ":" + b.getData()); //In old versions, we cannot just use material type because several materials have the same type but are different in data
                    if(xmaterial.isPresent()) blocks.add(coordinates + ":" + xmaterial.get().name());
                    else { //The id then probably represents direction, not a different material
                        blocks.add(coordinates + ":" + XMaterial.matchXMaterial(b.getType()).name() + ":" + b.getData());
                    }
                }
            }

            //Containers
            Inventory blockInventory = Utils.getBlockInventory(b);
            if(blockInventory != null){
                containers.put(coordinates, blockInventory.getContents());
            }
        }

        if(spawnLocation == null){
            p.sendMessage(Customization.getInstance().prefix + ChatColor.RED + "Your position indicates the spawnpoint of the island! You must stand inside the theme region");
            return false;
        }

        spawnLocation += ":" + p.getLocation().getYaw() + ":" + p.getLocation().getPitch();

        File saveFile = new File(Skyblock.getInstance().getDataFolder() + "/themes", name);
        FileConfiguration editor = YamlConfiguration.loadConfiguration(saveFile);

        editor.set("Spawn", spawnLocation);
        editor.set("Width", cuboid.getWidth());
        editor.set("Length", cuboid.getLength());
        editor.set("Blocks", blocks.toString());
        editor.set("Containers", containers);
        try {
            editor.save(saveFile);
            themes.put(name.toLowerCase(), new Theme(saveFile));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public class Theme {

        public String name;

        HashMap<CLocation, CBlock> blocks;
        int width, length;
        Location spawn; //Holds x, y, z, yaw and pitch (NO WORLD)

        public Theme(File saveFile){
            this.name = saveFile.getName();

            FileConfiguration editor = YamlConfiguration.loadConfiguration(saveFile);

            String[] spliter = editor.getString("Spawn").split(":");
            spawn = new Location(null, Float.parseFloat(spliter[0]) - 0.5f, Float.parseFloat(spliter[1]) + 1f, Float.parseFloat(spliter[2]) - 0.5f, Float.parseFloat(spliter[3]), Float.parseFloat(spliter[4]));

            HashMap<CLocation, ItemStack[]> containers = new HashMap<>();
            if(editor.getConfigurationSection("Containers") != null)
                for(String coordinates: editor.getConfigurationSection("Containers").getKeys(false)){
                    spliter = coordinates.split(":");
                    int x = Integer.parseInt(spliter[0]), y = Integer.parseInt(spliter[1]), z = Integer.parseInt(spliter[2]);

                    List<ItemStack> content = (List<ItemStack>) editor.get("Containers." + coordinates);

                    containers.put(new CLocation(x, y, z), content.toArray(new ItemStack[content.size()]));
                }

            blocks = new HashMap<>();
            String[] blockSpliter = editor.getString("Blocks").replace("[", "").replace("]", "").split(", ");
            for(String block: blockSpliter){
                spliter = block.split(":");
                int x = Integer.parseInt(spliter[0]), y = Integer.parseInt(spliter[1]), z = Integer.parseInt(spliter[2]);
                CLocation cLocation = new CLocation(x, y, z);

                XMaterial xMaterial = XMaterial.valueOf(spliter[3]);
                Material material = xMaterial.parseMaterial();

                byte direction = 0;
                if(spliter.length > 4) direction = Byte.parseByte(spliter[4]);
                byte passedData = (byte) Math.max(direction, XMaterial.isNewVersion() ? 0 : xMaterial.getData()); //One of them will be zero. Cannot be both.

                ItemStack[] inventory = containers.get(cLocation);

                blocks.put(cLocation, new CBlock(material, passedData, inventory));
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
                CBlock cBlock = blocks.get(cl);
                b.setType(cBlock.material, false);

                if(cBlock.data > 0){
                    if(XMaterial.isNewVersion()){
                        BlockData blockData = b.getBlockData();
                        if(blockData instanceof Directional){
                            ((Directional) blockData).setFacing(Utils.getFaceFromByte(cBlock.data));
                            Bukkit.broadcastMessage(Utils.getFaceFromByte(cBlock.data).name());
                            b.setBlockData(blockData);
                        }
                    } else {
                        try {
                            ReflectionUtils.setData.invoke(b, cBlock.data, false);
                        } catch (IllegalAccessException | InvocationTargetException ignored) {
                        }
                    }
                }

                if(cBlock.inventory != null){
                    Inventory blockInventory = Utils.getBlockInventory(b);
                    if(blockInventory != null) blockInventory.setContents(cBlock.inventory);
                }
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

        private class CBlock {

            Material material;
            byte data;
            ItemStack[] inventory;

            public CBlock(Material material, byte data, ItemStack[] inventory){
                this.material = material;
                this.data = data;
                this.inventory = inventory;
            }

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
