package it.mcexp.meteoritesPro.Guardians;

import java.util.Arrays;
import java.util.Objects;
import it.mcexp.meteoritesPro.ExceptionHandling.ConfigException;
import it.mcexp.meteoritesPro.Meteorites.Meteorite;
import it.mcexp.meteoritesPro.MeteoritesPro;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class TreasureGuardian {
    private MeteoritesPro plugin;
    private ConfigurationSection config;
    private Entity guardian;
    private String guardianConfigName;
    private EntityType entityType;
    private Material mainHand;
    private Material offHand;
    private Material helmet;
    private Material chestplate;
    private Material leggings;
    private Material boots;
    private double maxHealth;
    private double attackDamage;
    private double movementSpeed;
    private Sound spawnSound;
    private float spawnSoundVolume;
    private float spawnSoundPitch;

    public TreasureGuardian(MeteoritesPro plugin, String guardianConfigName) throws ConfigException {
        this.plugin = plugin;
        this.guardianConfigName = guardianConfigName;
        this.config = plugin.getConfig().getConfigurationSection("possible-guardians." + guardianConfigName);
        if (this.config == null) {
            throw new ConfigException("Missing config section possible-guardians." + guardianConfigName);
        }
        this.setEntityType();
    }

    public void spawnGuardian(Location spawnLocation, Player player) {
        this.sendPlayerMessage(player);
        if (this.config.contains("commands", true)) {
            for (String command : this.config.getStringList("commands")) {
                if (command.equals("")) continue;
                command = Meteorite.setLocationPlaceholders(command, spawnLocation);
                command = command.replaceAll("%player%", player.getName());
                Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)command);
            }
        }
        if (!this.config.contains("use-command-only", true) || !this.config.getBoolean("use-command-only")) {
            this.guardian = Objects.requireNonNull(spawnLocation.getWorld()).spawnEntity(spawnLocation, this.entityType);
            this.setDisplayName();
            this.setGuardianEquipment();
            this.setGuardianAttributes();
            this.playSpawnSound(player, spawnLocation);
        }
    }

    private void playSpawnSound(Player player, Location spawnLocation) {
        player.playSound(spawnLocation, this.spawnSound, this.spawnSoundVolume, this.spawnSoundPitch);
    }

    private void sendPlayerMessage(Player player) {
        if (this.config.contains("player-message") && !Objects.equals(this.config.getString("player-message"), "")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)Objects.requireNonNull(this.config.getString("player-message"))));
        }
    }

    private void setEntityType() throws ConfigException {
        if (!Arrays.asList(EntityType.values()).toString().contains(Objects.requireNonNull(this.config.getString(".guardian-mob-type")).toUpperCase())) {
            throw new ConfigException("Invalid mob type: '" + this.config.getString(".guardian-mob-type") + "' for guardian '" + this.guardianConfigName + "'");
        }
        try {
            this.entityType = EntityType.valueOf((String)Objects.requireNonNull(this.config.getString(".guardian-mob-type")).toUpperCase());
        }
        catch (IllegalArgumentException e) {
            this.plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MeteoritesPro] Invalid mob type: '" + this.config.getString(".guardian-mob-type") + "' for guardian '" + this.guardianConfigName + "'");
        }
    }

    private void setDisplayName() {
        if (this.config.getString(".guardian-display-name") != null && !Objects.equals(this.config.getString(".guardian-display-name"), "")) {
            this.guardian.setCustomName(ChatColor.translateAlternateColorCodes((char)'&', (String)Objects.requireNonNull(this.config.getString(".guardian-display-name"))));
            this.guardian.setCustomNameVisible(true);
        }
    }

    private void setGuardianEquipment() {
        EntityEquipment equipment;
        if (this.config.getBoolean("enable-guardian-equipment") && (equipment = ((LivingEntity)this.guardian).getEquipment()) != null) {
            if (this.mainHand != null) {
                equipment.setItemInMainHand(new ItemStack(this.mainHand));
            }
            if (this.offHand != null) {
                equipment.setItemInOffHand(new ItemStack(this.offHand));
            }
            if (this.helmet != null) {
                equipment.setHelmet(new ItemStack(this.helmet));
            }
            if (this.chestplate != null) {
                equipment.setChestplate(new ItemStack(this.chestplate));
            }
            if (this.leggings != null) {
                equipment.setLeggings(new ItemStack(this.leggings));
            }
            if (this.boots != null) {
                equipment.setBoots(new ItemStack(this.boots));
            }
        }
    }

    private void setGuardianAttributes() {
        if (this.config.contains("guardian-attack-damage", true) && !Objects.requireNonNull(this.config.getString("guardian-attack-damage")).equalsIgnoreCase("default")) {
            Objects.requireNonNull(((Attributable)this.guardian).getAttribute(Attribute.ATTACK_DAMAGE)).setBaseValue(this.attackDamage);
        }
        if (this.config.contains("guardian-health", true) && !Objects.requireNonNull(this.config.getString("guardian-health")).equalsIgnoreCase("default")) {
            Objects.requireNonNull(((Attributable)this.guardian).getAttribute(Attribute.MAX_HEALTH)).setBaseValue(this.maxHealth);
            ((LivingEntity)this.guardian).setHealth(this.maxHealth);
        }
        if (this.config.contains("guardian-movement-speed", true) && !Objects.requireNonNull(this.config.getString("guardian-movement-speed")).equalsIgnoreCase("default")) {
            Objects.requireNonNull(((Attributable)this.guardian).getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(this.movementSpeed);
        }
    }

    public void setGuardianMaterials() throws ConfigException {
        if (this.config.getBoolean("enable-guardian-equipment")) {
            ConfigurationSection materialConfig = this.config.getConfigurationSection(".guardian-equipment");
            if (materialConfig == null) {
                throw new ConfigException("Missing guardian-equipment in config file for guardian: " + this.guardianConfigName);
            }
            if (materialConfig.contains(".main-hand", true) && !Objects.equals(materialConfig.getString(".main-hand"), "")) {
                this.mainHand = Material.getMaterial((String)Objects.requireNonNull(materialConfig.getString(".main-hand")));
                if (this.mainHand == null) {
                    throw new ConfigException("Invalid material type for main hand '" + materialConfig.getString(".main-hand") + "' of guardian: " + this.guardianConfigName);
                }
            }
            if (materialConfig.contains(".off-hand", true) && !Objects.equals(materialConfig.getString(".off-hand"), "")) {
                this.offHand = Material.getMaterial((String)Objects.requireNonNull(materialConfig.getString(".off-hand")));
                if (this.offHand == null) {
                    throw new ConfigException("Invalid material type for off hand '" + materialConfig.getString(".off-hand") + "' of guardian: " + this.guardianConfigName);
                }
            }
            if (materialConfig.contains(".helmet", true) && !Objects.equals(materialConfig.getString(".helmet"), "")) {
                this.helmet = Material.getMaterial((String)Objects.requireNonNull(materialConfig.getString(".helmet")));
                if (this.helmet == null) {
                    throw new ConfigException("Invalid material type for helmet '" + materialConfig.getString(".helmet") + "' of guardian: " + this.guardianConfigName);
                }
            }
            if (materialConfig.contains(".chestplate", true) && !Objects.equals(materialConfig.getString(".chestplate"), "")) {
                this.chestplate = Material.getMaterial((String)Objects.requireNonNull(materialConfig.getString(".chestplate")));
                if (this.chestplate == null) {
                    throw new ConfigException("Invalid material type for chestplate '" + materialConfig.getString(".chestplate") + "' of guardian: " + this.guardianConfigName);
                }
            }
            if (materialConfig.contains(".leggings", true) && !Objects.equals(materialConfig.getString(".leggings"), "")) {
                this.leggings = Material.getMaterial((String)Objects.requireNonNull(materialConfig.getString(".leggings")));
                if (this.leggings == null) {
                    throw new ConfigException("Invalid material type for leggings '" + materialConfig.getString(".leggings") + "' of guardian: " + this.guardianConfigName);
                }
            }
            if (materialConfig.contains(".boots", true) && !Objects.equals(materialConfig.getString(".boots"), "")) {
                this.boots = Material.getMaterial((String)Objects.requireNonNull(materialConfig.getString(".boots")));
                if (this.boots == null) {
                    throw new ConfigException("Invalid material type for boots '" + materialConfig.getString(".boots") + "' of guardian: " + this.guardianConfigName);
                }
            }
        }
    }

    public void setGuardianAttributeValues() throws ConfigException {
        if (this.config.contains("guardian-attack-damage", true) && !Objects.requireNonNull(this.config.getString("guardian-attack-damage")).equalsIgnoreCase("default")) {
            if (this.config.getDouble("guardian-attack-damage") >= 0.0 && this.config.getDouble("guardian-attack-damage") <= 500.0) {
                this.attackDamage = this.config.getDouble("guardian-attack-damage");
            } else {
                throw new ConfigException("guardian-attack-damage must be a value from 0 - 500 for guardian: '" + this.guardianConfigName + "', but was: " + this.config.getDouble("guardian-attack-damage"));
            }
        }
        if (this.config.contains("guardian-health", true) && !Objects.requireNonNull(this.config.getString("guardian-health")).equalsIgnoreCase("default")) {
            if (this.config.getDouble("guardian-health") >= 1.0 && this.config.getDouble("guardian-health") <= 2000.0) {
                this.maxHealth = this.config.getDouble("guardian-health");
            } else {
                throw new ConfigException("guardian-health must be a value from 1 - 2000 for guardian: '" + this.guardianConfigName + "', but was: " + this.config.getDouble("guardian-health"));
            }
        }
        if (this.config.contains("guardian-movement-speed", true) && !Objects.requireNonNull(this.config.getString("guardian-movement-speed")).equalsIgnoreCase("default")) {
            if (this.config.getDouble("guardian-movement-speed") >= 0.0 && this.config.getDouble("guardian-movement-speed") <= 0.7) {
                this.movementSpeed = this.config.getDouble("guardian-movement-speed");
            } else {
                throw new ConfigException("guardian-health must be a value from 0 - 1 for guardian: '" + this.guardianConfigName + "', but was: " + this.config.getDouble("guardian-movement-speed"));
            }
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void setGuardianSpawnSound() throws ConfigException {
        if (!Arrays.asList(Sound.values()).toString().contains(Objects.requireNonNull(this.config.getString(".guardian-spawn-sound")).toUpperCase())) {
            throw new ConfigException("Invalid sound type: '" + this.config.getString(".guardian-spawn-sound") + "' for guardian '" + this.guardianConfigName + "'");
        }
        try {
            if (this.config.contains("guardian-spawn-sound", true) && !Objects.equals(this.config.getString("guardian-spawn-sound"), "")) {
                this.spawnSound = Sound.valueOf((String)Objects.requireNonNull(this.config.getString("guardian-spawn-sound")).toUpperCase());
            }
        }
        catch (IllegalArgumentException e) {
            this.plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[MeteoritesPro] Invalid sound type: '" + this.config.getString(".guardian-spawn-sound") + "' for guardian '" + this.guardianConfigName + "'");
        }
        if (this.config.contains("guardian-spawn-sound-volume", true)) {
            if (!(this.config.getDouble("guardian-spawn-sound-volume") >= 0.0)) throw new ConfigException("guardian-spawn-sound-volume must be a value from larger than 0 for guardian: '" + this.guardianConfigName + "', but was: " + this.config.getDouble("guardian-spawn-sound-volume"));
            this.spawnSoundVolume = (float)this.config.getDouble("guardian-spawn-sound-volume");
        } else {
            this.spawnSoundVolume = 5.0f;
        }
        if (this.config.contains("guardian-spawn-sound-pitch", true)) {
            if (!(this.config.getDouble("guardian-spawn-sound-pitch") >= 0.5) || !(this.config.getDouble("guardian-spawn-sound-pitch") <= 2.0)) throw new ConfigException("guardian-spawn-sound-pitch must be a value from 0.5 - 2.0 for guardian: '" + this.guardianConfigName + "', but was: " + this.config.getDouble("guardian-spawn-sound-pitch"));
            this.spawnSoundPitch = (float)this.config.getDouble("guardian-spawn-sound-pitch");
            return;
        } else {
            this.spawnSoundPitch = 1.0f;
        }
    }
}
