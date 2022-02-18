package me.wazup.skyblock.skills;

import me.wazup.skyblock.managers.FilesManager;
import me.wazup.skyblock.utils.Enums.Statistic;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SkillsManager {

    HashMap<Statistic, List<SkillLevel>> skillLevels;

    private static SkillsManager instance;
    public static SkillsManager getInstance(){
        return instance;
    }

    public SkillsManager(){
        instance = this;

        FileConfiguration file = FilesManager.getInstance().getConfig("skills.yml");

        //Create skills
        if(file.getBoolean("Generate-Default-Skills")){
            file.set("Generate-Default-Skills", false);

            String[] combatLevels = { "1 : 5 : 0.02", "2 : 20 : 0.04", "3 : 50 : 0.06", "4 : 100 : 0.08",
                    "5 : 175 : 0.1", "6 : 275 : 0.12", "7 : 400 : 0.14", "8 : 550 : 0.16", "9 : 725 : 0.18",
                    "10 : 925 : 0.2" };
            for(String level: combatLevels){
                String[] splitter = level.split(" : ");
                String name = splitter[0];
                int scoreNeeded = Integer.parseInt(splitter[1]);
                double impact = Double.parseDouble(splitter[2]);
                file.set("Skills.Combat.Levels." + name + ".Score-Needed", scoreNeeded);
                file.set("Skills.Combat.Levels." + name + ".Impact", impact);
            }

            String[] miningLevels = { "1 : 10 : 0.02", "2 : 40 : 0.04", "3 : 100 : 0.06", "4 : 180 : 0.08",
                    "5 : 280 : 0.1", "6 : 400 : 0.12", "7 : 550 : 0.14", "8 : 800 : 0.16", "9 : 1200 : 0.18",
                    "10 : 1700 : 0.2" };
            for(String level: miningLevels){
                String[] splitter = level.split(" : ");
                String name = splitter[0];
                int scoreNeeded = Integer.parseInt(splitter[1]);
                double impact = Double.parseDouble(splitter[2]);
                file.set("Skills.Mining.Levels." + name + ".Score-Needed", scoreNeeded);
                file.set("Skills.Mining.Levels." + name + ".Impact", impact);
            }

            FilesManager.getInstance().saveConfig("skills.yml");
        }

        //Load skills
        skillLevels = new HashMap<>();
        for(Statistic statistic: Statistic.values()){
            if(statistic.skillConfigName == null) continue;

            statistic.skillDisplayedName = file.getString("Skills." + statistic.skillConfigName + ".Displayed-Name");
            statistic.skillDescription = new ArrayList<>();
            for(String desc: file.getStringList("Skills." + statistic.skillConfigName + ".Description")){
                statistic.skillDescription.add(ChatColor.translateAlternateColorCodes('&', desc));
            }

            ArrayList<SkillLevel> levels = new ArrayList<>();
            for(String levelName: file.getConfigurationSection("Skills." + statistic.skillConfigName + ".Levels").getKeys(false)){
                int scoreNeeded = file.getInt("Skills." + statistic.skillConfigName + ".Levels." + levelName + ".Score-Needed");
                double impact = file.getDouble("Skills." + statistic.skillConfigName + ".Levels." + levelName + ".Impact");
                levels.add(new SkillLevel(levelName, scoreNeeded, impact));
            }
            skillLevels.put(statistic, levels);
        }
    }

    public SkillLevel evaluateLevel(Statistic statistic, int score){
        if(!skillLevels.containsKey(statistic)) return null;

        List<SkillLevel> levels = skillLevels.get(statistic);
        SkillLevel evaluated = null;
        for(SkillLevel level: levels){
            if(score >= level.scoreNeeded) evaluated = level;
            else break;
        }
        return evaluated;
    }

    public SkillLevel getNextLevel(Statistic statistic, SkillLevel level){
        if(!skillLevels.containsKey(statistic)) return null;

        List<SkillLevel> levels = skillLevels.get(statistic);
        if(level == null){ //We do not have a level
            if(levels.size() > 0) return levels.get(0); //Get first level
            else return null; //No levels
        }
        int index = levels.indexOf(level);
        if(index + 1 == levels.size()) return null; //We are already on the last level
        return levels.get(index + 1);
    }

    public class SkillLevel {

        String name;
        int scoreNeeded;
        double impact;

        public SkillLevel(String name, int scoreNeeded, double impact){
            this.name = name;
            this.scoreNeeded = scoreNeeded;
            this.impact = impact;
        }

    }

}
