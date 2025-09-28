package it.mcexp.meteoritesPro.Particles;

import java.util.Arrays;
import java.util.Objects;
import it.mcexp.meteoritesPro.ExceptionHandling.ConfigException;
import it.mcexp.meteoritesPro.MeteoritesPro;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;

public class MeteoriteParticle {
    private MeteoritesPro plugin;
    private String particleConfigName;
    private ConfigurationSection config;
    private Particle particleType;
    private double spread;
    private double speed;
    private int interval;
    private int amount;
    private boolean forceView;

    public MeteoriteParticle(MeteoritesPro plugin, String particleConfigName) throws ConfigException {
        this.plugin = plugin;
        this.particleConfigName = particleConfigName;
        this.config = plugin.getConfig().getConfigurationSection("possible-meteorite-particle-effects." + particleConfigName);
        if (this.config == null) {
            throw new ConfigException("Missing config section possible-meteorite-particle-effects." + particleConfigName);
        }
        this.setParticleSettings();
    }

    private void setParticleSettings() throws ConfigException {
        if (this.plugin.getConfig().contains("meteorite-particle-interval", true)) {
            this.interval = this.plugin.getConfig().getInt("meteorite-particle-interval");
            if (this.interval <= 0 || this.interval > 10) {
                throw new ConfigException("Invalid meteorite-particle-interval: " + this.interval + ". Interval must be a rounded number from 1 - 10.");
            }
            this.interval = 2;
        }
        if (!Arrays.asList(Particle.values()).toString().contains(Objects.requireNonNull(this.config.getString(".particle-effect")).toUpperCase())) {
            throw new ConfigException("Invalid particle type: '" + this.config.getString(".particle-effect") + "' for particle '" + this.particleConfigName + "'");
        }
        try {
            this.particleType = Particle.valueOf((String)Objects.requireNonNull(this.config.getString(".particle-effect")).toUpperCase());
        }
        catch (IllegalArgumentException e) {
            this.plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MeteoritesPro] Invalid particle type: '" + this.config.getString(".particle-effect") + "' for particle '" + this.particleConfigName + "'");
        }
        if (this.config.contains(".amount", true)) {
            this.amount = this.config.getInt(".amount");
            if (this.amount < 0) {
                throw new ConfigException("Invalid amount:" + this.amount + " for particle '" + this.particleConfigName + "'. Amount must be a rounded number from above 0.");
            }
            this.amount = 1;
        }
        if (this.config.contains(".spread", true)) {
            this.spread = this.config.getDouble(".spread");
            if (this.spread < 0.0) {
                throw new ConfigException("Invalid spread:" + this.spread + " for particle '" + this.particleConfigName + "'. Spread must be a number from above 0.");
            }
        } else {
            this.spread = 0.1;
        }
        if (this.config.contains(".speed", true)) {
            this.speed = this.config.getDouble(".speed");
            if (this.speed < 0.0 || this.speed > 2.0) {
                throw new ConfigException("Invalid speed:" + this.speed + " for particle '" + this.particleConfigName + "'. speed must be a number from 0 - 2.");
            }
        } else {
            this.speed = 0.05;
        }
        this.forceView = this.config.contains(".force-visibility", true) ? this.config.getBoolean(".force-visibility") : true;
    }

    public Particle getParticleType() {
        return this.particleType;
    }

    public int getAmount() {
        return this.amount;
    }

    public double getSpread() {
        return this.spread;
    }

    public double getSpeed() {
        return this.speed;
    }

    public boolean isForceView() {
        return this.forceView;
    }
}
