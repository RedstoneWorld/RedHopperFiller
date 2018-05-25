package de.redstoneworld.redtrichterfiller;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RedTrichterFillerCommand implements CommandExecutor {
    private final RedTrichterFiller plugin;
    private static final Set<Material> TRANSPARENT = new HashSet<>();
    
    static {
        for (Material material : Material.values()) {
            if (material.isBlock() && !material.isSolid()) {
                TRANSPARENT.add(material);
            }
        }
    }
    
    public RedTrichterFillerCommand(RedTrichterFiller plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0) {
            if ("reload".equalsIgnoreCase(args[0]) && sender.hasPermission("rwm.trichterfiller.reload")) {
                plugin.loadConfig();
                sender.sendMessage(ChatColor.YELLOW + "Config reloaded!");
                return true;
            }
        }
        
        if (!(sender instanceof Player)) {
            return false;
        }
        
        int targetStrength = plugin.getDefaultTargetStrength();
        if (args.length > 0 && sender.hasPermission("rwm.trichterfiller.setstrength")) {
            try {
                targetStrength = Integer.parseInt(args[0]);
            } catch (IllegalArgumentException e) {
                plugin.sendMessage(sender, "wrong-syntax");
                return true;
            }
        }
        
        ItemStack item = ((Player) sender).getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            plugin.sendMessage(sender, "no-item");
            return true;
        }
        
        List<Block> blocks = ((Player) sender).getLastTwoTargetBlocks(TRANSPARENT, 6);
        if (blocks.get(1).getType() != Material.HOPPER) {
            plugin.sendMessage(sender, "not-a-hopper");
            return true;
        }

        PlayerInteractEvent interactEvent = new PlayerInteractEvent((Player) sender, Action.RIGHT_CLICK_BLOCK, item, blocks.get(1), blocks.get(1).getFace(blocks.get(0)), EquipmentSlot.HAND);
        plugin.getServer().getPluginManager().callEvent(interactEvent);
        
        if (!interactEvent.isCancelled()) {
            if (plugin.fillHopper((Player) sender, item, ((InventoryHolder) blocks.get(1).getState()).getInventory(), targetStrength)) {
                plugin.sendMessage(sender, "filled");
            }
        }
        return true;
    }
}
