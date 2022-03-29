package me.wazup.skyblock.managers;

import me.wazup.skyblock.Skyblock;
import me.wazup.skyblock.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class FilesManager {

    private static FilesManager instance;

    private HashMap<String, FileConfiguration> configurations = new HashMap<>();

    public FilesManager(){
        instance = this;

        Skyblock.getInstance().reloadConfig();
        Skyblock.getInstance().getConfig().options().copyDefaults(true);
        Skyblock.getInstance().saveConfig();

        registerConfig("customization.yml");
        registerConfig("islands.yml");
        registerConfig("skills.yml");

        for(String fileName: configurations.keySet()){
            reloadConfig(fileName);
            configurations.get(fileName).options().copyDefaults(true);
            saveConfig(fileName);
        }

    }

    public static FilesManager getInstance() {
        return instance;
    }

    private void registerConfig(String name){
        configurations.put(name, YamlConfiguration.loadConfiguration(new File(Skyblock.getInstance().getDataFolder(), name)));
    }

    public FileConfiguration getConfig(String fileName){
        return configurations.get(fileName);
    }

    private void reloadConfig(String fileName){
        InputStream inputStream = Skyblock.getInstance().getResource(fileName);
        if(inputStream != null){
            InputStreamReader reader = new InputStreamReader(inputStream);
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(reader);
            configurations.get(fileName).setDefaults(defConfig);
            try {
                reader.close();
                inputStream.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void saveConfig(String fileName){
        try {
            configurations.get(fileName).save(new File(Skyblock.getInstance().getDataFolder(), fileName));
        } catch (IOException ex) {
            Utils.error("Couldn't save " + fileName + "!");
        }
    }

    public static void deleteFile(File path){
        if(path.exists()){
            if(path.isDirectory())
                for(File f: path.listFiles()){
                    if(f.isDirectory()) deleteFile(f); else f.delete();
                }
        }
        path.delete();
    }

//    public static void copyFile(File source, File target){
//        try {
//            if(source.isDirectory()){
//                if(!target.exists()) target.mkdirs();
//                String files[] = source.list();
//                for (String file : files) {
//                    File srcFile = new File(source, file);
//                    File destFile = new File(target, file);
//                    copyFile(srcFile, destFile);
//                }
//
//            } else {
//                FileInputStream inputStream = new FileInputStream(source);
//                FileOutputStream outputStream = new FileOutputStream(target);
//                FileChannel inChannel = inputStream.getChannel();
//                FileChannel outChannel = outputStream.getChannel();
//                try {
//                    inChannel.transferTo(0, inChannel.size(), outChannel);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } finally {
//                    if(inChannel != null) inChannel.close();
//                    if(outChannel != null) outChannel.close();
//                    inputStream.close();
//                    outputStream.close();
//                }
//            }
//        } catch (IOException e){
//            Utils.error("Failed to copy files!");
//            e.printStackTrace();
//        }
//    }

}
