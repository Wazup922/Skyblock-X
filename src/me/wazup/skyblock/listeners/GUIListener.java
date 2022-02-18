package me.wazup.skyblock.listeners;

import me.wazup.skyblock.Skyblock;
import me.wazup.skyblock.managers.Customization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

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
//            Inventory inv = e.getInventory();
//            ItemStack clicked = e.getCurrentItem();
//            int rawSlot = e.getRawSlot();
        }
    }

}
