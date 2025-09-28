package it.mcexp.meteoritesPro;

import java.util.Objects;
import it.mcexp.meteoritesPro.Commands.CommandClass;
import it.mcexp.meteoritesPro.EventListeners.EventListenerClass;
import it.mcexp.meteoritesPro.Guardians.TreasureGuardianCreator;
import it.mcexp.meteoritesPro.Meteorites.MeteoriteCreator;
import it.mcexp.meteoritesPro.Meteorites.RandomMeteoriteHandler;
import it.mcexp.meteoritesPro.Particles.MeteoriteParticleCreator;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class MeteoritesPro
        extends JavaPlugin {
    public void onEnable() {
        this.sendConsoleStartMessage();
        this.saveDefaultConfig();
        CommandClass commandClass = new CommandClass(this);
        EventListenerClass eventListenerClass = new EventListenerClass(this);
        Objects.requireNonNull(this.getCommand("mp")).setExecutor((CommandExecutor)commandClass);
        Objects.requireNonNull(this.getCommand("meteoritespro")).setExecutor((CommandExecutor)commandClass);
        this.getServer().getPluginManager().registerEvents((Listener)eventListenerClass, (Plugin)this);
        this.initializePluginRandomizers();
        RandomMeteoriteHandler.randomMeteoriteHandler(this);
    }

    public void onDisable() {
    }

    private void sendConsoleStartMessage() {
        this.getServer().getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "         " + ChatColor.BLUE + "__");
        this.getServer().getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "   |\\/| " + ChatColor.BLUE + "|__)   " + ChatColor.GOLD + "MeteoritesPro " + ChatColor.DARK_GRAY + "v" + this.getDescription().getVersion());
        this.getServer().getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "   |  | " + ChatColor.BLUE + "|      " + ChatColor.WHITE + "  by OliPulse");
        this.getServer().getConsoleSender().sendMessage(" ");
    }

    public Boolean initializePluginRandomizers() {
        boolean successfulMeteorite = MeteoriteCreator.initializeMeteorites(this);
        boolean successfulTreasureGuardian = true;
        boolean successfulParticles = true;
        if (this.getConfig().contains("enable-treasure-guardian", true) && this.getConfig().getBoolean("enable-treasure-guardian")) {
            successfulTreasureGuardian = TreasureGuardianCreator.initializeGuardianRandomizer(this);
        }
        if (this.getConfig().contains("enable-meteorite-particles", true) && this.getConfig().getBoolean("enable-meteorite-particles")) {
            successfulParticles = MeteoriteParticleCreator.initializeParticleRandomizer(this);
        }
        return successfulMeteorite && successfulTreasureGuardian && successfulParticles;
    }
}
