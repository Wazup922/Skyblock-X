package me.wazup.skyblock;

import me.wazup.skyblock.commands.MainCommand;
import me.wazup.skyblock.listeners.GUIListener;
import me.wazup.skyblock.listeners.PlayerListener;
import me.wazup.skyblock.listeners.StatisticListener;
import me.wazup.skyblock.managers.Config;
import me.wazup.skyblock.managers.Customization;
import me.wazup.skyblock.managers.FilesManager;
import me.wazup.skyblock.managers.ThemeManager;
import me.wazup.skyblock.skills.SkillsManager;
import me.wazup.skyblock.utils.*;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Skyblock extends JavaPlugin {

    public HashMap<UUID, PlayerData> playerData = new HashMap<>();

    public HashMap<Integer, Island> islands;

    public HashSet<UUID> players = new HashSet<>();

    public WarpContainer serverWarps;

    private static Skyblock instance;
    public static Skyblock getInstance(){
        return instance;
    }

    @Override
    public void onEnable(){
        instance = this;

        ChatColor color = ChatColor.AQUA;
        Bukkit.getConsoleSender().sendMessage(color + "*************** [Skyblock X] ****************");
        Bukkit.getConsoleSender().sendMessage(color + "Initializing Skyblock X by Wazup92....");
        Bukkit.getConsoleSender().sendMessage(color + "Detected Version: " + getDescription().getVersion());

        MainCommand commandHandler = new MainCommand();
        getCommand("skyblock").setExecutor(commandHandler);

        ReflectionUtils.loadMethods();
        ItemStackBuilder.loadMethods();

        loadAll();

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this); //Needs to be after loading customization
        Bukkit.getPluginManager().registerEvents(new GUIListener(), this);
        Bukkit.getPluginManager().registerEvents(new StatisticListener(), this);

        for(Player p: Utils.getOnlinePlayers()){
            playerData.put(p.getUniqueId(), new PlayerData(p));
        }

        Bukkit.getConsoleSender().sendMessage(color + "Skyblock X loaded successfully...!");
        Bukkit.getConsoleSender().sendMessage(color + "*********************************************");
    }

    @Override
    public void onDisable(){
        for(Player p: Utils.getOnlinePlayers()){
            playerData.get(p.getUniqueId()).saveData(p, false);
        }

        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[Skyblock X] Plugin has been disabled!");
    }

    private void loadAll(){
        new FilesManager();
        new Config();
        new Customization();
        new ThemeManager();
        new SkillsManager();

        loadWorld();
        loadIslands();
        loadWarps();
    }

    private void loadWorld(){
        String worldName = Config.getInstance().worldName;

        Bukkit.getConsoleSender().sendMessage("Loading Skyblock X world: " + ChatColor.LIGHT_PURPLE + worldName + "....");

        try {
            WorldCreator creator = new WorldCreator(worldName);

            creator.type(WorldType.FLAT);
            creator.generatorSettings("2;0;1;"); //Makes the world empty in 1.8-1.12.2

            creator.generator(new ChunkGenerator() { //Makes the world empty in 1.13+
                @Override
                public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
                    return createChunkData(world);
                }
            });

            creator.createWorld();

            Bukkit.getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "World loaded!");
        } catch (Exception e){
            Utils.error("Could not the world!");
            e.printStackTrace();
        }
    }

    private void loadIslands(){
        islands = new HashMap<>();

        FileConfiguration file = FilesManager.getInstance().getConfig("islands.yml");

        ConfigurationSection section = file.getConfigurationSection("Islands");
        if(section == null || section.getKeys(false).isEmpty()) return; //No islands registered

        for(String id: section.getKeys(false)){
            islands.put(Integer.parseInt(id), new Island(file, Integer.parseInt(id)));
        }

        Bukkit.getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "Loaded " + islands.size() + " islands!");
    }

    private void loadWarps(){
        serverWarps = new WarpContainer();

        File file = new File(Skyblock.getInstance().getDataFolder(), "warps.yml");
        FileConfiguration editor = YamlConfiguration.loadConfiguration(file);

        if(editor.getConfigurationSection("Warps") != null){
            for(String warpName: editor.getConfigurationSection("Warps").getKeys(false)){
                try {
                    String path = "Warps." + warpName + ".";
                    Location location = Utils.getLocationFromString(editor.getString(path + "Location"));
                    String displayedName = ChatColor.translateAlternateColorCodes('&', editor.getString(path + "Displayed-Name"));
                    Optional<XMaterial> xmaterial = XMaterial.matchXMaterial(editor.getString(path + "Displayed-Item"));
                    ItemStack displayedItem = xmaterial.isPresent() ? xmaterial.get().parseItem() : XMaterial.PAPER.parseItem();
                    List<String> displayedLore = new ArrayList<>();
                    for (String lore : editor.getStringList(path + "Displayed-Lore"))
                        displayedLore.add(ChatColor.translateAlternateColorCodes('&', lore));

                    serverWarps.addWarp(warpName, displayedName, location, displayedItem, displayedLore);
                } catch (Exception e){
                    Utils.error("You have a problem with warp '" + warpName + "' in warps.yml, please check the error below!");
                    e.printStackTrace();
                }
            }
        }

    }

    public int createIsland(String name, String displayedOwner, String themeName){
        //Determine island ID
        int selectedID = 0;
        for (int id: islands.keySet()) if(id > selectedID) selectedID = id;
        selectedID += 1;

        //Determine island center
        int x = (selectedID - 1) * Config.getInstance().distanceBetweenIslands;
        int z = 0;

        //Build island
        Location center = new Location(Bukkit.getWorld(Config.getInstance().worldName), x, Config.getInstance().islandsYCoordinate, z);
        ThemeManager.Theme theme = ThemeManager.getInstance().themes.get(themeName);
        theme.build(center);

        //Save island to file
        FileConfiguration islandsFile = FilesManager.getInstance().getConfig("islands.yml");
        islandsFile.set("Islands." + selectedID + ".Displayed-Name", name);
        islandsFile.set("Islands." + selectedID + ".Displayed-Owner", displayedOwner);
        islandsFile.set("Islands." + selectedID + ".Center", x + ", " + z);
        islandsFile.set("Islands." + selectedID + ".Warps.Spawn", Utils.getStringFromExactLocation(theme.getSpawn(center)));
        FilesManager.getInstance().saveConfig("islands.yml");

        //Load island
        Island island = new Island(islandsFile, selectedID);
        Skyblock.getInstance().islands.put(selectedID, island);

        return selectedID;
    }

    public boolean join(Player p){
        PlayerData data = playerData.get(p.getUniqueId());

        World world = Bukkit.getWorld(Config.getInstance().worldName);
        if(world == null){
            p.sendMessage(Customization.getInstance().messages.get("World-Not-Loaded"));
            return false;
        }

        if(data.islandID == -1){ //No island
            p.sendMessage(Customization.getInstance().messages.get("Creating-Island"));

            int selectedID = createIsland(p.getName(), p.getName(), "default");

            //Asign island to player data
            data.islandID = selectedID;

            p.sendMessage(Customization.getInstance().messages.get("Island-Creation").replace("%island_id%", String.valueOf(selectedID)));
        }

        if(!Skyblock.getInstance().islands.containsKey(data.islandID)){
            p.sendMessage(Customization.getInstance().messages.get("Island-Not-Found").replace("%island_id%", String.valueOf(data.islandID)));
            return false;
        }

        //TODO Add safe spawn check

        players.add(p.getUniqueId());
        data.shouldSave = true;

        if(Config.getInstance().changePlayerConditionsUponJoin){
            data.outsideCondition = new PlayerCondition();
            data.outsideCondition.extractCondition(p);
        }

        islands.get(data.islandID).join(p);

        if(Config.getInstance().changePlayerConditionsUponJoin){
            PlayerCondition inside = new PlayerCondition();
            boolean loaded = inside.loadConditionFromFile(p);
            if(loaded) inside.applyCondition(p);
            else Utils.clearPlayer(p);
        }

        return true;
    }

    public void leave(Player p){
        players.remove(p.getUniqueId());

        PlayerData data = playerData.get(p.getUniqueId());

        if(Config.getInstance().changePlayerConditionsUponJoin){
            PlayerCondition current = new PlayerCondition();
            current.extractCondition(p);
            current.saveConditionToFile(p);

            data.outsideCondition.applyCondition(p);
        }
    }

}
