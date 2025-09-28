package it.mcexp.meteoritesPro.Meteorites;

import it.mcexp.meteoritesPro.MeteoritesPro;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

public class MeteoriteCore {
    private Material material;
    private Location location;
    private MeteoritesPro plugin;

    public MeteoriteCore(Material material, MeteoritesPro plugin) {
        this.material = material;
        this.plugin = plugin;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return this.location;
    }

    public boolean spawnMeteoriteCore(World world, Vector vector, ConfigurationSection coreConfig) {
        BlockData coreBlockData = Bukkit.createBlockData((Material)this.material);
        FallingBlock fallingCore = world.spawnFallingBlock(this.location, coreBlockData);
        fallingCore.setCustomName("core");
        fallingCore.setVelocity(vector);
        fallingCore.setHurtEntities(coreConfig.getBoolean("can-hurt-entities"));
        fallingCore.setDropItem(coreConfig.getBoolean("drop-item-when-destroyed"));
        return true;
    }
}
