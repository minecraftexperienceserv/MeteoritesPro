package it.mcexp.meteoritesPro.Meteorites;

import java.util.HashMap;
import java.util.Objects;
import it.mcexp.meteoritesPro.ExceptionHandling.ConfigException;
import it.mcexp.meteoritesPro.MeteoritesPro;
import it.mcexp.meteoritesPro.Randomizers.RandomizerClass;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

public class MeteoriteCreator {
    private static HashMap<String, Meteorite> meteoriteList = new HashMap();
    private static RandomizerClass meteoriteRandomizer;

    public static boolean initializeMeteorites(MeteoritesPro plugin) {
        try {
            meteoriteRandomizer = new RandomizerClass(123456789L);
            for (String meteoriteStringName : Objects.requireNonNull(plugin.getConfig().getConfigurationSection("meteorites")).getKeys(false)) {
                Meteorite meteorite;
                int innerLayerSize;
                ConfigurationSection config = plugin.getConfig().getConfigurationSection("meteorites." + meteoriteStringName);
                if (config == null) continue;
                int outerLayerSize = config.getInt("outer-layer-size");
                if (outerLayerSize <= (innerLayerSize = config.getInt("inner-layer-size")) && config.getBoolean("enable-inner-layer")) {
                    throw new ConfigException("Outer layer meteorite size must be greater than inner layer size for meteorite '" + meteoriteStringName + "'");
                }
                if (outerLayerSize < 2 || innerLayerSize < 2) {
                    throw new ConfigException("Outer and inner meteorite layer size must be greater than 1 for meteorite '" + meteoriteStringName + "'");
                }
                if (config.contains("clean-up-meteorite-blocks-interval", true) && config.getInt("clean-up-meteorite-blocks-interval") < 20 && config.getInt("clean-up-meteorite-blocks-interval") > 0) {
                    throw new ConfigException("invalid 'clean-up-meteorite-blocks-interval' for meteorite: '" + meteoriteStringName + "'. Must be at least 20 but was: " + config.getInt("clean-up-meteorite-blocks-interval"));
                }
                RandomizerClass coreRandomizer = new RandomizerClass(123456789L);
                if (!coreRandomizer.addMaterials(config.getConfigurationSection("core-block"), plugin)) {
                    throw new ConfigException("Error trying to create meteorite core for meteorite '" + meteoriteStringName + "'");
                }
                MeteoriteCore core = new MeteoriteCore(coreRandomizer.getRandomMaterial(), plugin);
                RandomizerClass outerRandomizer = new RandomizerClass(123456789L);
                if (!outerRandomizer.addMaterials(config.getConfigurationSection("outer-layer-blocks"), plugin)) {
                    throw new ConfigException("Error trying to create meteorite outer layer for meteorite '" + meteoriteStringName + "'");
                }
                MeteoriteLayer outerLayer = new MeteoriteLayer(config.getInt("outer-layer-size"), "outer", outerRandomizer, plugin);
                if (config.getBoolean("enable-inner-layer")) {
                    RandomizerClass innerRandomizer = new RandomizerClass(123456789L);
                    if (!innerRandomizer.addMaterials(config.getConfigurationSection("inner-layer-blocks"), plugin)) {
                        throw new ConfigException("Error trying to create meteorite inner layer for meteorite '" + meteoriteStringName + "'");
                    }
                    MeteoriteLayer innerLayer = new MeteoriteLayer(config.getInt("inner-layer-size"), "inner", innerRandomizer, plugin);
                    meteorite = new Meteorite(core, outerLayer, innerLayer, config);
                } else {
                    meteorite = new Meteorite(core, outerLayer, config);
                }
                meteoriteList.put(meteoriteStringName, meteorite);
                meteoriteRandomizer.addMeteoriteChance(meteorite, config.getInt("chance"));
            }
            return true;
        }
        catch (ConfigException e) {
            ConfigException.handleConfigException(plugin, e);
            return false;
        }
    }

    public static boolean createMeteorite(Location location, Vector vector, MeteoritesPro plugin, ConfigurationSection config) {
        Meteorite meteorite = meteoriteList.get(config.getName());
        return meteorite.spawnMeteorite(location, vector, plugin);
    }

    public static RandomizerClass getMeteoriteRandomizer() {
        return meteoriteRandomizer;
    }

    public static HashMap<String, Meteorite> getMeteoriteList() {
        return meteoriteList;
    }
}
