package me.wazup.skyblock.skills;

import me.wazup.skyblock.utils.Enums.Statistic;
import me.wazup.skyblock.skills.SkillsManager.SkillLevel;

public class Skill {

    Statistic statistic;
    public int score;
    public double currentImpact;
    
    SkillLevel currentSkillLevel;
    SkillLevel nextSkillLevel;
    
    public Skill(Statistic statistic, int score){
        this.statistic = statistic;
        this.score = score;

        //Both of these can be null
        currentSkillLevel = SkillsManager.getInstance().evaluateLevel(statistic, score);
        nextSkillLevel = SkillsManager.getInstance().getNextLevel(statistic, currentSkillLevel);
        
        if(currentSkillLevel == null) currentImpact = 0;
    }

    public void addScore(int add){
        score += add;

        //Check also for achievements

        if(nextSkillLevel != null){
            if(score >= nextSkillLevel.scoreNeeded){
                //Leveled up
                currentSkillLevel = nextSkillLevel;
                nextSkillLevel = SkillsManager.getInstance().getNextLevel(statistic, currentSkillLevel);
                
                currentImpact = currentSkillLevel.impact;
            }
        }
    }

}
