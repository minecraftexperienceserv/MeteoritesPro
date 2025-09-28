package it.mcexp.meteoritesPro.Guardians;

import it.mcexp.meteoritesPro.ExceptionHandling.ConfigException;
import it.mcexp.meteoritesPro.MeteoritesPro;
import it.mcexp.meteoritesPro.Randomizers.RandomizerClass;
import org.bukkit.configuration.file.FileConfiguration;

public class TreasureGuardianCreator {
    private static RandomizerClass guardianRandomizer;

    public static RandomizerClass getGuardianRandomizer() {
        return guardianRandomizer;
    }

    public static boolean initializeGuardianRandomizer(MeteoritesPro plugin) {
        try {
            FileConfiguration config = plugin.getConfig();
            if (config.getBoolean("enable-treasure-guardian")) {
                guardianRandomizer = new RandomizerClass(123456789L);
                guardianRandomizer.addGuardians(plugin, config.getConfigurationSection("possible-guardians"));
            }
            return true;
        }
        catch (ConfigException e) {
            ConfigException.handleConfigException(plugin, e);
            return false;
        }
    }
}
