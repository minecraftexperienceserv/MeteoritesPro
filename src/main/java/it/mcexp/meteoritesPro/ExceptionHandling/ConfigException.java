package it.mcexp.meteoritesPro.ExceptionHandling;

import it.mcexp.meteoritesPro.MeteoritesPro;
import org.bukkit.ChatColor;

public class ConfigException
        extends Exception {
    public ConfigException(String message) {
        super(message);
    }

    public static void handleConfigException(MeteoritesPro plugin, ConfigException e) {
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MeteoritesPro] " + e.getMessage());
    }
}
