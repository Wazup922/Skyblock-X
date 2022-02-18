package me.wazup.skyblock.utils;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Enums {

    public enum SPECIAL_CHARACTER {
        ARROW(StringEscapeUtils.unescapeJava("\u279D"));

        private String value;
        SPECIAL_CHARACTER(String value){
            this.value = value;
        }

        public String toString(){
            return value;
        }

    }

    public enum Statistic {

        MOBS_KILLED("Combat"), ORES_MINED("Mining"), BLOCKS_PLACED(null), BLOCKS_BROKEN(null);

        //For skills associated stats only
        public String skillConfigName; //Null if no skill is associated with it.

        public String skillDisplayedName; //Filled from skills.yml file in SkillsManager
        public List<String> skillDescription;

        //For all stats
        public String displayedName; //Always available from customization.yml
        public ItemStack displayedItem;

        Statistic(String skillConfigName){
            this.skillConfigName = skillConfigName;
        }

    }

}
