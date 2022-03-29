package me.wazup.skyblock.listeners;

import me.wazup.skyblock.Skyblock;
import me.wazup.skyblock.managers.Customization;
import me.wazup.skyblock.utils.WarpContainer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        Player p = (Player) e.getWhoClicked();

        if(Skyblock.getInstance().players.contains(p.getUniqueId())) {
            Customization customization = Customization.getInstance();
            String invName = e.getView().getTitle();

            if(invName.equals(customization.inventories.get("Skills-Menu")) || invName.equals(customization.inventories.get("Statistics-Menu"))){
                e.setCancelled(true);
                return;
            }

            Inventory inv = e.getInventory();
            ItemStack clicked = e.getCurrentItem();
            int rawSlot = e.getRawSlot();

            if(invName.startsWith(Skyblock.getInstance().serverWarps.inventory.getName())){
                e.setCancelled(true);
                if(Skyblock.getInstance().serverWarps.inventory.handleClick(p, clicked, inv, rawSlot)) return;
                WarpContainer.Warp warp = Skyblock.getInstance().serverWarps.getWarp(clicked.getItemMeta().getDisplayName());
                if(warp != null){
                    p.teleport(warp.location);
                }
                return;
            }

        }
    }

}
