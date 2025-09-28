package it.mcexp.meteoritesPro.Meteorites;

import java.util.ArrayList;
import java.util.List;

import it.mcexp.meteoritesPro.MeteoritesPro;
import it.mcexp.meteoritesPro.Particles.MeteoriteParticleCreator;
import it.mcexp.meteoritesPro.Randomizers.RandomizerClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

public class MeteoriteLayer {
    private int diameter;
    private String innerOrOuterMeteorLayer;
    private RandomizerClass randomizer;
    private MeteoritesPro plugin;

    public MeteoriteLayer(int diameter, String innerOrOuterMeteorLayer, RandomizerClass randomizer, MeteoritesPro plugin) {
        this.diameter = diameter;
        this.innerOrOuterMeteorLayer = innerOrOuterMeteorLayer;
        this.randomizer = randomizer;
        this.plugin = plugin;
    }

    public boolean spawnMeteoriteLayer(World world, Vector vector, MeteoriteCore core, ConfigurationSection layerConfig) {
        for (Location meteorLayerBlockLocation : this.generateSphere(core.getLocation(), this.diameter, true)) {
            BlockData meteoriteLayerBlockData = Bukkit.createBlockData((Material)this.randomizer.getRandomMaterial());
            FallingBlock fallingMeteorLayerBlock = world.spawnFallingBlock(meteorLayerBlockLocation, meteoriteLayerBlockData);
            fallingMeteorLayerBlock.setCustomName(this.innerOrOuterMeteorLayer);
            fallingMeteorLayerBlock.setVelocity(vector);
            fallingMeteorLayerBlock.setHurtEntities(layerConfig.getBoolean("can-hurt-entities"));
            fallingMeteorLayerBlock.setDropItem(layerConfig.getBoolean("drop-item-when-destroyed"));
            if (!this.plugin.getConfig().contains("enable-meteorite-particles", true) || !this.plugin.getConfig().getBoolean("enable-meteorite-particles")) continue;
            MeteoriteParticleCreator.spawnParticle(this.plugin, fallingMeteorLayerBlock);
        }
        return true;
    }

    public List<Location> generateSphere(Location centerBlock, int radius, boolean hollow) {
        ArrayList<Location> circleBlocks = new ArrayList<Location>();
        int bx = centerBlock.getBlockX();
        int by = centerBlock.getBlockY();
        int bz = centerBlock.getBlockZ();
        for (int x = bx - radius; x <= bx + radius; ++x) {
            for (int y = by - radius; y <= by + radius; ++y) {
                for (int z = bz - radius; z <= bz + radius; ++z) {
                    double distance = (bx - x) * (bx - x) + (bz - z) * (bz - z) + (by - y) * (by - y);
                    if (!(distance < (double)(radius * radius)) || hollow && distance < (double)((radius - 1) * (radius - 1))) continue;
                    Location l = new Location(centerBlock.getWorld(), (double)x, (double)y, (double)z);
                    circleBlocks.add(l);
                }
            }
        }
        return circleBlocks;
    }
}
