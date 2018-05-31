package de.redstoneworld.redhopperfiller;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class PlayerListener implements Listener {
    private final RedHopperFiller plugin;
    
    public PlayerListener(RedHopperFiller plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (event.getClick() != plugin.getClickType()
                || event.getCursor() == null || event.getCursor().getType() == Material.AIR
                || event.getClickedInventory().getType() != InventoryType.HOPPER) {
            return;
        }
        
        plugin.fillHopper(event.getWhoClicked(), event.getCursor(), event.getClickedInventory(), plugin.getDefaultTargetStrength());
    }
}
