package de.redstoneworld.redhopperfiller;

import org.bukkit.ChatColor;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class RedHopperFiller extends JavaPlugin {
    
    private ClickType clickType;
    private boolean returnItems;
    private int defaultTargetStrength;
    private int itemsRequiredForStrength;
    
    @Override
    public void onEnable() {
        loadConfig();
        getCommand(getName().toLowerCase()).setExecutor(new RedHopperFillerCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }
    
    public void loadConfig() {
        saveDefaultConfig();
        reloadConfig();
        getCommand(getName().toLowerCase()).setPermissionMessage(getLang("no-permission"));
        returnItems = getConfig().getBoolean("return-items");
        defaultTargetStrength = getConfig().getInt("default-target-strength");
        itemsRequiredForStrength = getConfig().getInt("items-required-for-strength");
        try {
            clickType = ClickType.valueOf(getConfig().getString("click-type").toUpperCase());
        } catch (IllegalArgumentException e) {
            clickType = ClickType.MIDDLE;
            getLogger().log(Level.WARNING, getConfig().getString("click-type") + " is not a valid click type! Using " + clickType);
        }
    }
    
    void sendMessage(CommandSender sender, String key, String... args) {
        String text = getLang(key, args);
        if (!text.isEmpty()) {
            sender.sendMessage(text);
        }
    }
    
    String getLang(String key, String... args) {
        String lang = getConfig().getString("lang." + key, "&cUnknown language key &6" + key);
        return ChatColor.translateAlternateColorCodes('&', replace(lang, args));
    }
    
    static String replace(String text, String... args) {
        for (int i = 0; i + 1 < args.length; i+=2) {
            text = text.replace("<" + args[i] + ">", args[i + 1]);
        }
        return text;
    }
    
    public ClickType getClickType() {
        return clickType;
    }
    
    public int getDefaultTargetStrength() {
        return defaultTargetStrength;
    }
    
    public boolean fillHopper(HumanEntity filler, ItemStack item, Inventory inventory, int targetStrength) {
        if (item.getMaxStackSize() <= 1) {
            sendMessage(filler, "item-not-stackable", "type", item.getType().toString());
            return false;
        }
        
        if (targetStrength <= 0) {
            sendMessage(filler, "strength-too-low", "strength", String.valueOf(targetStrength));
            return false;
        }
    
        if (targetStrength > 15) {
            sendMessage(filler, "strength-too-high", "strength", String.valueOf(targetStrength));
            return false;
        }
        
        int requiredItems = Math.max(targetStrength, (int) Math.ceil((inventory.getSize() * item.getMaxStackSize() / 14.0) * (targetStrength - 1)));
        requiredItems = requiredItems - itemsRequiredForStrength;
        if (requiredItems < inventory.getSize()) {
            sendMessage(filler, "inventory-too-big",
                    "size", String.valueOf(inventory.getSize()),
                    "required", String.valueOf(requiredItems - 1),
                    "strength", String.valueOf(targetStrength)
            );
            return false;
        }
        
        ItemStack clone = new ItemStack(item);
        for (int i = 0; i < inventory.getSize(); i++) {
            if (returnItems && inventory.getItem(i) != null) {
                filler.getInventory().addItem(inventory.getItem(i));
            }
            int amount = requiredItems - inventory.getSize() + i + 1;
            int minAmount = Math.min(amount, clone.getMaxStackSize());
            clone.setAmount(minAmount);
            requiredItems = requiredItems - clone.getAmount();
            inventory.setItem(i, clone);
        }
        
        if (inventory.getHolder() instanceof BlockState) {
            ((BlockState) inventory.getHolder()).update();
        }
        if (filler instanceof Player && filler.getOpenInventory().getTopInventory() == inventory) {
            ((Player) filler).updateInventory();
        }
        return true;
    }
}
