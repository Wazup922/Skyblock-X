package me.wazup.skyblock.listeners;

import me.wazup.skyblock.PlayerData;
import me.wazup.skyblock.Skyblock;
import me.wazup.skyblock.utils.Enums;
import me.wazup.skyblock.utils.XMaterial;
import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Arrays;
import java.util.List;

public class StatisticListener implements Listener {

    List<Material> ores = Arrays.asList(XMaterial.COAL_ORE.parseMaterial(), XMaterial.IRON_ORE.parseMaterial(), XMaterial.GOLD_ORE.parseMaterial(),
            XMaterial.DIAMOND_ORE.parseMaterial(), XMaterial.REDSTONE_ORE.parseMaterial(), XMaterial.LAPIS_ORE.parseMaterial(),
            XMaterial.EMERALD_ORE.parseMaterial());

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e){
        LivingEntity entity = e.getEntity();
        if(entity instanceof Creature) {
            Player killer = entity.getKiller();

            if (killer != null && Skyblock.getInstance().players.contains(killer.getUniqueId())) {
                PlayerData data = Skyblock.getInstance().playerData.get(killer.getUniqueId());
                data.statistics.get(Enums.Statistic.MOBS_KILLED).addScore(1);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        Player p = e.getPlayer();
        if(Skyblock.getInstance().players.contains(p.getUniqueId())){
            PlayerData data = Skyblock.getInstance().playerData.get(p.getUniqueId());

            data.statistics.get(Enums.Statistic.BLOCKS_BROKEN).addScore(1);

            Material type = e.getBlock().getType();
            if(ores.contains(type)){
                data.statistics.get(Enums.Statistic.ORES_MINED).addScore(1);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e){
        Player p = e.getPlayer();
        if(Skyblock.getInstance().players.contains(p.getUniqueId())){
            PlayerData data = Skyblock.getInstance().playerData.get(p.getUniqueId());
            data.statistics.get(Enums.Statistic.BLOCKS_PLACED).addScore(1);
        }
    }

}
