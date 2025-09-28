package it.mcexp.meteoritesPro.Particles;

import java.util.ArrayList;
import it.mcexp.meteoritesPro.ExceptionHandling.ConfigException;
import it.mcexp.meteoritesPro.MeteoritesPro;
import it.mcexp.meteoritesPro.Randomizers.RandomizerClass;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.FallingBlock;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class MeteoriteParticleCreator {
    private static RandomizerClass particleRandomizer;

    public static RandomizerClass getParticleRandomizer() {
        return particleRandomizer;
    }

    public static boolean initializeParticleRandomizer(MeteoritesPro plugin) {
        try {
            FileConfiguration config = plugin.getConfig();
            if (config.getBoolean("enable-meteorite-particles")) {
                particleRandomizer = new RandomizerClass(123456789L);
                particleRandomizer.addParticles(plugin, config.getConfigurationSection("possible-meteorite-particle-effects"));
            }
            return true;
        }
        catch (ConfigException e) {
            ConfigException.handleConfigException(plugin, e);
            return false;
        }
    }

    public static void spawnParticle(MeteoritesPro plugin, final FallingBlock fallingBlock) {
        int interval = plugin.getConfig().getInt("meteorite-particle-interval");
        final ArrayList locationList = new ArrayList();
        if (interval > 0) {
            new BukkitRunnable(){

                public void run() {
                    if (fallingBlock.isDead()) {
                        this.cancel();
                    }
                    MeteoriteParticle particle = particleRandomizer.getRandomParticle();
                    Location fallingBlockLocation = fallingBlock.getLocation();
                    double spread = particle.getSpread();
                    fallingBlock.getWorld().spawnParticle(particle.getParticleType(), fallingBlockLocation, particle.getAmount(), spread, spread, spread, particle.getSpeed(), null, particle.isForceView());
                    if (locationList.size() > 30 && ((Location)locationList.get(locationList.size() - 1)).equals((Object)fallingBlockLocation)) {
                        this.cancel();
                    }
                    locationList.add(fallingBlockLocation);
                }
            }.runTaskTimer((Plugin)plugin, 1L, (long)interval);
        }
        new BukkitRunnable(){

            public void run() {
            }
        }.runTaskLater((Plugin)plugin, 200L);
    }
}
