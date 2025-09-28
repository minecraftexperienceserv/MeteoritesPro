package it.mcexp.meteoritesPro.Meteorites;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import it.mcexp.meteoritesPro.ExceptionHandling.ConfigException;
import it.mcexp.meteoritesPro.MeteoritesPro;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.CreateClaimResult;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class RandomMeteoriteHandler {
    private static BukkitScheduler scheduler;
    private static int schedulerId;
    private static Runnable runnable;

    public static boolean randomMeteoriteHandler(MeteoritesPro plugin) {
        try {
            FileConfiguration config = plugin.getConfig();
            scheduler = plugin.getServer().getScheduler();
            long delay = config.getLong("random-meteorite-interval") * 20L;

            Random random = new Random();
            double height = config.getDouble("random-meteorite-spawn-height");
            double maxX = config.getDouble("random-meteorite-max-spawn-x-coord");
            double maxZ = config.getDouble("random-meteorite-max-spawn-z-coord");
            double minX = config.getDouble("random-meteorite-min-spawn-x-coord");
            double minZ = config.getDouble("random-meteorite-min-spawn-z-coord");

            if (maxX <= minX) {
                throw new ConfigException("Max X coordinate for random meteorite may not be smaller than min X coordinate");
            }
            if (maxZ <= minZ) {
                throw new ConfigException("Max Z coordinate for random meteorite may not be smaller than min Z coordinate");
            }

            Vector randomVector = new Vector();
            String worldName = config.getString("random-meteorite-world");
            if (worldName == null) {
                throw new ConfigException("Invalid world name for random meteorites.");
            }
            World world = plugin.getServer().getWorld(worldName);
            if (world == null) {
                throw new ConfigException("Invalid world name for random meteorites.");
            }

            // Classe anonima senza argomenti: usa direttamente le variabili esterne
            runnable = new Runnable() {
                Location randomLocation;
                double randomX, randomZ, differenceX, differenceZ;

                boolean WGisEnabled = false;
                WorldGuardPlugin worldGuardPlugin;
                RegionContainer regionContainer;
                RegionManager regionManager;
                ProtectedRegion protectedRegion;
                int safeZoneBufferWG;

                int tryCount = 0;

                boolean GPisEnabled = false;
                GriefPrevention griefPrevention;
                int safeZoneBufferGP;

                boolean foundSafeLocation = false;
                boolean locationIsSafeForGP;
                boolean locationIsSafeForWG;

                @Override
                public void run() {
                    // GriefPrevention
                    if (config.contains("enable-griefprevention-safe-zones", true)
                            && config.getBoolean("enable-griefprevention-safe-zones")) {
                        griefPrevention = RandomMeteoriteHandler.getGriefPrevention(plugin);
                    }
                    if (griefPrevention != null && griefPrevention.claimsEnabledForWorld(world)) {
                        GPisEnabled = true;
                    }

                    // WorldGuard
                    if (config.contains("enable-worldguard-safe-zones", true)
                            && config.getBoolean("enable-worldguard-safe-zones")) {
                        worldGuardPlugin = RandomMeteoriteHandler.getWorldGuard(plugin);
                    }
                    if (worldGuardPlugin != null) {
                        regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
                        regionManager = regionContainer.get(BukkitAdapter.adapt(Objects.requireNonNull(world)));
                        if (regionManager != null) {
                            WGisEnabled = true;
                        }
                    }

                    if (checkAllSafeZones()) {
                        RandomMeteoriteHandler.setRandomVector(randomVector, random, plugin);
                        boolean ok = MeteoriteCreator.getMeteoriteRandomizer()
                                .getRandomMeteorite()
                                .spawnMeteorite(randomLocation, randomVector, plugin);
                        if (!ok) {
                            plugin.getServer().getConsoleSender().sendMessage(
                                    ChatColor.RED + "[MeteoritePro] Failed to shoot random meteorite.");
                        }
                    } else {
                        plugin.getServer().getConsoleSender().sendMessage(
                                ChatColor.RED + "[MeteoritePro] Couldn't find safe random meteorite location after " + tryCount + " attempts.");
                        plugin.getServer().getConsoleSender().sendMessage(
                                ChatColor.RED + "[MeteoritePro] Make sure there's enough space that isn't protected for the meteorite to spawn in! Are your buffers too high?");
                    }

                    tryCount = 0;
                    foundSafeLocation = false;
                }

                private boolean checkAllSafeZones() {
                    setRandomLocation();
                    while (tryCount < 20 && !foundSafeLocation) {
                        if (GPisEnabled) {
                            locationIsSafeForGP = checkIfInGPClaim();
                            if (!locationIsSafeForGP) {
                                ++tryCount;
                                setRandomLocation();
                                continue;
                            }
                        }
                        if (WGisEnabled) {
                            locationIsSafeForWG = checkIfInWGSafeZone();
                            if (!locationIsSafeForWG) {
                                ++tryCount;
                                setRandomLocation();
                                continue;
                            }
                        }
                        foundSafeLocation = true;
                    }
                    return tryCount < 20;
                }

                private boolean checkIfInGPClaim() {
                    Claim claim = griefPrevention.dataStore.getClaimAt(randomLocation, true, null);
                    if (claim != null) {
                        return false;
                    }
                    safeZoneBufferGP = config.getInt("griefprevention-safe-zone-buffer");
                    CreateClaimResult bufferClaimResult = griefPrevention.dataStore.createClaim(
                            world,
                            (int) randomLocation.getX() - safeZoneBufferGP,
                            (int) randomLocation.getX() + safeZoneBufferGP,
                            0, 256,
                            (int) randomLocation.getZ() - safeZoneBufferGP,
                            (int) randomLocation.getZ() + safeZoneBufferGP,
                            null, null, 88888888L, null
                    );
                    if (bufferClaimResult.succeeded) {
                        griefPrevention.dataStore.deleteClaim(griefPrevention.dataStore.getClaim(88888888L));
                        return true;
                    }
                    return false;
                }

                private boolean checkIfInWGSafeZone() {
                    safeZoneBufferWG = config.getInt("worldguard-safe-zone-buffer");
                    if (plugin.getConfig().contains("protect-all-worldguard-zones", true)
                            && plugin.getConfig().getBoolean("protect-all-worldguard-zones")) {
                        ArrayList<ProtectedRegion> protectedRegionList =
                                new ArrayList<>(regionManager.getRegions().values());
                        for (ProtectedRegion safeZone : protectedRegionList) {
                            if (checkIfRandomLocationIsInSafeZonePlusBuffer(safeZone)) {
                                return false;
                            }
                        }
                        return true;
                    }
                    for (String safeZoneName : config.getStringList("worldguard-safe-zone-names")) {
                        protectedRegion = regionManager.getRegion(safeZoneName);
                        if (protectedRegion == null) {
                            plugin.getServer().getConsoleSender().sendMessage(
                                    ChatColor.RED + "[MeteoritePro] Caution, there is no matching world guard region for safe zone: '" + safeZoneName + "'.");
                            continue;
                        }
                        if (checkIfRandomLocationIsInSafeZonePlusBuffer(protectedRegion)) {
                            return false;
                        }
                    }
                    return true;
                }

                private boolean checkIfRandomLocationIsInSafeZonePlusBuffer(ProtectedRegion safeZone) {
                    ProtectedCuboidRegion fullSafeZone = new ProtectedCuboidRegion(
                            "meteoriteSafeZone123",
                            safeZone.getMinimumPoint().add(-safeZoneBufferWG, 0, -safeZoneBufferWG),
                            safeZone.getMaximumPoint().add( safeZoneBufferWG, 0,  safeZoneBufferWG)
                    );

                    BlockVector3 test = BlockVector3.at(
                            randomLocation.getBlockX(),
                            randomLocation.getBlockY(),
                            randomLocation.getBlockZ()
                    );

                    if (fullSafeZone.contains(test)) {
                        plugin.getServer().getConsoleSender().sendMessage(
                                "Try " + tryCount + ", Meteorite Location " + randomLocation + " was in WG safe zone: " + safeZone);
                        // Non è necessario aggiungere/rimuovere davvero la regione temporanea dal manager,
                        // ma in caso il decompilato lo facesse:
                        if (regionManager != null) {
                            regionManager.removeRegion("meteoriteSafeZone123");
                        }
                        return true;
                    }
                    return false;
                }

                private void setRandomLocation() {
                    differenceX = maxX - minX;
                    randomX = (double) random.nextInt((int) Math.max(1, differenceX)) + minX;

                    differenceZ = maxZ - minZ;
                    randomZ = (double) random.nextInt((int) Math.max(1, differenceZ)) + minZ;

                    randomLocation = new Location(world, randomX, height, randomZ);
                }
            };

            if (config.getBoolean("enable-random-meteorites")) {
                schedulerId = scheduler.scheduleSyncRepeatingTask(plugin, runnable, delay, delay);
            }
            return true;
        } catch (ConfigException e) {
            ConfigException.handleConfigException(plugin, e);
            return false;
        }
    }

    private static WorldGuardPlugin getWorldGuard(MeteoritesPro meteoritesPro) {
        Plugin plugin = meteoritesPro.getServer().getPluginManager().getPlugin("WorldGuard");
        if (!(plugin instanceof WorldGuardPlugin)) {
            return null;
        }
        return (WorldGuardPlugin) plugin;
    }

    private static GriefPrevention getGriefPrevention(MeteoritesPro meteoritesPro) {
        Plugin plugin = meteoritesPro.getServer().getPluginManager().getPlugin("GriefPrevention");
        if (!(plugin instanceof GriefPrevention)) {
            return null;
        }
        return (GriefPrevention) plugin;
    }

    private static void setRandomVector(Vector randomVector, Random random, MeteoritesPro plugin) {
        double speed = 2.0;
        if (plugin.getConfig().contains("meteorite-speed", true)) {
            speed = plugin.getConfig().getDouble("meteorite-speed");
        }
        randomVector.setX((random.nextInt(2000) - 1000) / 1000.0 * speed);
        randomVector.setZ((random.nextInt(2000) - 1000) / 1000.0 * speed);
        randomVector.setY(random.nextInt(3) - 2);
    }

    public static BukkitScheduler getScheduler() {
        return scheduler;
    }

    public static int getSchedulerId() {
        return schedulerId;
    }

    public static void shootRandomMeteorite(MeteoritesPro plugin) {
        scheduler.runTask(plugin, runnable);
    }
}