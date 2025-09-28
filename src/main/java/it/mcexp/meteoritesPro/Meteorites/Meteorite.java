package it.mcexp.meteoritesPro.Meteorites;

import java.util.ArrayList;
import java.util.List;
import it.mcexp.meteoritesPro.EventListeners.EventListenerClass;
import it.mcexp.meteoritesPro.MeteoritesPro;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

public class Meteorite {
    private MeteoriteCore core;
    private MeteoriteLayer outerLayer;
    private MeteoriteLayer innerLayer;
    private ConfigurationSection config;
    BukkitScheduler scheduler = Bukkit.getScheduler();
    private int schedulerId;
    List<Block> blockList = new ArrayList<Block>();

    public Meteorite(MeteoriteCore core, MeteoriteLayer outerLayer, ConfigurationSection config) {
        this.core = core;
        this.outerLayer = outerLayer;
        this.innerLayer = null;
        this.config = config;
    }

    public Meteorite(MeteoriteCore core, MeteoriteLayer outerLayer, MeteoriteLayer innerLayer, ConfigurationSection config) {
        this.core = core;
        this.outerLayer = outerLayer;
        this.innerLayer = innerLayer;
        this.config = config;
    }

    public boolean spawnMeteorite(Location location, Vector vector, MeteoritesPro plugin) {
        ConfigurationSection coreConfig = plugin.getConfig().getConfigurationSection("core-settings");
        ConfigurationSection innerConfig = plugin.getConfig().getConfigurationSection("inner-layer-settings");
        ConfigurationSection outerConfig = plugin.getConfig().getConfigurationSection("outer-layer-settings");
        this.core.setLocation(location);
        if (this.config.contains("chat-message", true)) {
            Object chatMessage = this.config.getString("chat-message");
            assert (chatMessage != null);
            if (!((String)chatMessage).equals("")) {
                chatMessage = Meteorite.setLocationPlaceholders((String)chatMessage, this.core.getLocation());
                Bukkit.broadcastMessage((String)ChatColor.translateAlternateColorCodes((char)'&', (String)chatMessage));
            }
        }
        if (this.config.contains("meteorite-spawn-commands", true)) {
            for (String command : this.config.getStringList("meteorite-spawn-commands")) {
                if (command.equals("")) continue;
                command = Meteorite.setLocationPlaceholders(command, this.core.getLocation());
                Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)command);
            }
        }
        World world = location.getWorld();
        if (this.config.contains("clean-up-meteorite-blocks-interval", true) && this.config.getInt("clean-up-meteorite-blocks-interval") > 0) {
            this.schedulerId = this.scheduler.scheduleSyncDelayedTask((Plugin)plugin, () -> {
                this.blockList.addAll(new ArrayList<Block>(EventListenerClass.getMeteoriteBlockList()));
                EventListenerClass.clearMeteoriteBlockList();
            }, 380L);
            this.schedulerId = this.scheduler.scheduleSyncDelayedTask((Plugin)plugin, this::cleanUpMeteoriteBlocks, (long)(this.config.getInt("clean-up-meteorite-blocks-interval") * 20));
        } else {
            this.schedulerId = this.scheduler.scheduleSyncDelayedTask((Plugin)plugin, EventListenerClass::clearMeteoriteBlockList, 380L);
        }
        if (!this.core.spawnMeteoriteCore(world, vector, coreConfig)) {
            return false;
        }
        if (this.innerLayer != null && !this.innerLayer.spawnMeteoriteLayer(world, vector, this.core, innerConfig)) {
            return false;
        }
        return this.outerLayer.spawnMeteoriteLayer(world, vector, this.core, outerConfig);
    }

    public static String setLocationPlaceholders(String string, Location location) {
        string = string.replaceAll("%locationX%", String.valueOf(Math.round(location.getX())));
        string = string.replaceAll("%locationZ%", String.valueOf(Math.round(location.getZ())));
        string = string.replaceAll("%locationY%", String.valueOf(Math.round(location.getY())));
        return string;
    }

    public void cleanUpMeteoriteBlocks() {
        for (Block meteoriteBlock : this.blockList) {
            meteoriteBlock.setType(Material.AIR);
        }
        this.blockList.clear();
    }
}
