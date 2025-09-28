package it.mcexp.meteoritesPro.EventListeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import it.mcexp.meteoritesPro.ExceptionHandling.ConfigException;
import it.mcexp.meteoritesPro.Guardians.TreasureGuardianCreator;
import it.mcexp.meteoritesPro.Meteorites.Meteorite;
import it.mcexp.meteoritesPro.MeteoritesPro;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class EventListenerClass
        implements Listener {
    private MeteoritesPro plugin;
    private Random random = new Random();
    private ItemStack treasureChecker;
    private static List<Block> meteoriteBlockList = new ArrayList<Block>();

    public EventListenerClass(MeteoritesPro plugin) {
        this.plugin = plugin;
        this.initializeTreasureChecker();
    }

    @EventHandler
    public void onMeteoriteFall(EntityChangeBlockEvent e) {
        Entity meteoriteBlockEntity = e.getEntity();
        if (meteoriteBlockEntity instanceof FallingBlock) {
            switch (meteoriteBlockEntity.getName().toLowerCase()) {
                case "core": {
                    ConfigurationSection coreConfig = this.plugin.getConfig().getConfigurationSection("core-settings");
                    assert (coreConfig != null);
                    this.handleMeteoriteBlockFall(meteoriteBlockEntity, coreConfig);
                    this.handleMeteoriteCoreFall(meteoriteBlockEntity);
                    meteoriteBlockList.add(meteoriteBlockEntity.getLocation().getBlock());
                    break;
                }
                case "inner": {
                    ConfigurationSection innerConfig = this.plugin.getConfig().getConfigurationSection("inner-layer-settings");
                    assert (innerConfig != null);
                    this.handleMeteoriteBlockFall(meteoriteBlockEntity, innerConfig);
                    meteoriteBlockList.add(meteoriteBlockEntity.getLocation().getBlock());
                    break;
                }
                case "outer": {
                    ConfigurationSection outerConfig = this.plugin.getConfig().getConfigurationSection("outer-layer-settings");
                    assert (outerConfig != null);
                    this.handleMeteoriteBlockFall(meteoriteBlockEntity, outerConfig);
                    meteoriteBlockList.add(meteoriteBlockEntity.getLocation().getBlock());
                }
            }
        }
    }

    @EventHandler
    public void onTreasureInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null) {
            if (e.getClickedBlock().getType() == Material.BARREL) {
                Barrel treasure = (Barrel)e.getClickedBlock().getState();
                this.checkForTreasure(e, treasure.getInventory(), treasure.getLocation());
            } else if (e.getClickedBlock().getType() == Material.CHEST) {
                Chest treasure = (Chest)e.getClickedBlock().getState();
                this.checkForTreasure(e, treasure.getInventory(), treasure.getLocation());
            }
        }
    }

    private void checkForTreasure(PlayerInteractEvent e, Inventory inventory, Location location) {
        ItemStack treasureCheck = inventory.getItem(26);
        if (treasureCheck != null && treasureCheck.equals((Object)this.treasureChecker)) {
            inventory.remove(treasureCheck);
            if (this.plugin.getConfig().getBoolean("enable-treasure-guardian")) {
                this.spawnMeteoriteGuardian(location, e.getPlayer());
                e.setCancelled(true);
            }
        }
    }

    private void spawnMeteoriteGuardian(Location treasureLocation, Player player) {
        Location playerLocation = player.getLocation();
        Location guardianLocation = this.getMiddleLocation(playerLocation, treasureLocation);
        TreasureGuardianCreator.getGuardianRandomizer().getRandomGuardian().spawnGuardian(guardianLocation, player);
    }

    private Location getMiddleLocation(Location location1, Location location2) {
        Location location = new Location(location1.getWorld(), (location1.getX() + location2.getX()) / 2.0, (location1.getY() + location2.getY()) / 2.0, (location1.getZ() + location2.getZ()) / 2.0);
        while (location.getBlock().getType() != Material.AIR || new Location(location.getWorld(), location.getX(), location.getY() + 1.0, location.getZ()).getBlock().getType() != Material.AIR || new Location(location.getWorld(), location.getX(), location.getY() + 2.0, location.getZ()).getBlock().getType() != Material.AIR) {
            location.add(0.0, 1.0, 0.0);
        }
        return location;
    }

    private void handleMeteoriteBlockFall(Entity meteoriteBlock, ConfigurationSection blockConfig) {
        Location blockLocation = meteoriteBlock.getLocation();
        if (blockConfig.getBoolean("enable-explosion")) {
            meteoriteBlock.getWorld().createExplosion(blockLocation.getX(), blockLocation.getY(), blockLocation.getZ(), (float)blockConfig.getInt("explosion-power"), blockConfig.getBoolean("explosion-sets-fire"), blockConfig.getBoolean("explosion-breaks-blocks"));
        }
        if (blockConfig.getBoolean("enable-lighting-strike")) {
            meteoriteBlock.getWorld().strikeLightning(blockLocation);
        }
    }

    private void handleMeteoriteCoreFall(Entity coreBlock) {
        try {
            FileConfiguration config = this.plugin.getConfig();
            if (config.contains("enable-meteorite-treasure", true) && config.getBoolean("enable-meteorite-treasure")) {
                Inventory inventory;
                Location treasureLocation = coreBlock.getLocation().add((double)(this.random.nextInt(2) - 1), -1.0, (double)(this.random.nextInt(2) - 1));
                Material treasureType = Material.getMaterial((String)Objects.requireNonNull(Objects.requireNonNull(config.getString("treasure-barrel-or-chest")).toUpperCase()));
                if (treasureType != Material.BARREL && treasureType != Material.CHEST) {
                    throw new ConfigException("Invalid treasure type: " + config.getString("treasure-barrel-or-chest") + " -> Treasure must be in a barrel or chest!");
                }
                treasureLocation.getBlock().setType(treasureType);
                if (treasureType == Material.BARREL) {
                    Barrel barrel = (Barrel)treasureLocation.getBlock().getState();
                    inventory = barrel.getInventory();
                    this.determineTreasureContent(inventory);
                } else {
                    Chest chest = (Chest)treasureLocation.getBlock().getState();
                    chest.setCustomName("treasure");
                    inventory = chest.getBlockInventory();
                    this.determineTreasureContent(inventory);
                }
                meteoriteBlockList.add(treasureLocation.getBlock());
            }
            if (config.contains("core-settings.message", true)) {
                String chatMessage = config.getString("core-settings.message");
                assert (chatMessage != null);
                if (!chatMessage.equals("")) {
                    chatMessage = Meteorite.setLocationPlaceholders(chatMessage, coreBlock.getLocation());
                    Bukkit.broadcastMessage((String)ChatColor.translateAlternateColorCodes((char)'&', (String)chatMessage));
                }
            }
            if (config.contains("core-settings.commands", true)) {
                for (String command : config.getStringList("core-settings.commands")) {
                    if (command.equals("")) continue;
                    command = Meteorite.setLocationPlaceholders(command, coreBlock.getLocation());
                    Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)command);
                }
            }
        }
        catch (ConfigException e) {
            ConfigException.handleConfigException(this.plugin, e);
        }
    }

    private void determineTreasureContent(Inventory inventory) {
        try {
            ConfigurationSection config = this.plugin.getConfig().getConfigurationSection("treasure-content");
            assert (config != null);
            for (String itemName : config.getKeys(false)) {
                String itemType;
                if (!config.getBoolean(itemName + ".enabled")) continue;
                int amount = 1;
                boolean chanceVerified = true;
                int count = 0;
                if (config.contains(itemName + ".chance", true)) {
                    double chance = config.getDouble(itemName + ".chance");
                    if (chance < 0.0 || chance > 100.0) {
                        throw new ConfigException("Invalid chance for item " + itemName + ": " + chance + " - Chance must be between 0-100");
                    }
                    if ((double)(this.random.nextInt(100) + 1) > chance) {
                        chanceVerified = false;
                    }
                }
                if (!chanceVerified) continue;
                if (config.contains(itemName + ".item-type", true)) {
                    itemType = config.getString(itemName + ".item-type");
                    if (itemType == null || Material.getMaterial((String)itemType) == null) {
                        throw new ConfigException("Invalid type for item " + itemName + ": " + itemType);
                    }
                } else {
                    throw new ConfigException("You must specify a type for item: " + itemName);
                }
                if (config.contains(itemName + ".amount", true)) {
                    amount = config.getInt(itemName + ".amount");
                }
                ItemStack item = new ItemStack(Objects.requireNonNull(Material.getMaterial((String)itemType)), amount);
                ItemMeta meta = item.getItemMeta();
                if (config.contains(itemName + ".display-name", true) && config.getString(itemName + ".display-name") != null) {
                    String displayName = config.getString(itemName + ".display-name");
                    Objects.requireNonNull(meta).setDisplayName(ChatColor.translateAlternateColorCodes((char)'&', (String)Objects.requireNonNull(displayName)));
                    item.setItemMeta(meta);
                }
                if (config.contains(itemName + ".lore", true)) {
                    ArrayList<String> lore = new ArrayList<String>();
                    for (String loreLine : config.getStringList(itemName + ".lore")) {
                        lore.add(ChatColor.translateAlternateColorCodes((char)'&', (String)loreLine));
                    }
                    Objects.requireNonNull(meta).setLore(lore);
                    item.setItemMeta(meta);
                }
                if (config.contains(itemName + ".unbreakable", true) && config.getBoolean(itemName + ".unbreakable")) {
                    Objects.requireNonNull(meta).setUnbreakable(true);
                    item.setItemMeta(meta);
                }
                if (config.contains(itemName + ".enchants", true)) {
                    for (String enchantLine : Objects.requireNonNull(config.getConfigurationSection(itemName + ".enchants")).getKeys(false)) {
                        if (Enchantment.getByKey((NamespacedKey)NamespacedKey.minecraft((String)enchantLine.toLowerCase())) == null) {
                            throw new ConfigException("Invalid enchantment name for item " + itemName + ": " + enchantLine);
                        }
                        Objects.requireNonNull(meta).addEnchant(Objects.requireNonNull(Enchantment.getByKey((NamespacedKey)NamespacedKey.minecraft((String)enchantLine.toLowerCase()))), config.getInt(itemName + ".enchants." + enchantLine), true);
                    }
                    item.setItemMeta(meta);
                }
                if (config.contains(itemName + ".custom-model-data", true) && config.getInt(itemName + ".custom-model-data") != 0) {
                    int customModelData = config.getInt(itemName + ".custom-model-data");
                    Objects.requireNonNull(meta).setCustomModelData(Integer.valueOf(customModelData));
                    item.setItemMeta(meta);
                }
                if (config.contains(itemName + ".damage", true) && config.getInt(itemName + ".damage") != 0) {
                    int damage = config.getInt(itemName + ".damage");
                    ((Damageable)Objects.requireNonNull(meta)).setDamage(damage);
                    item.setItemMeta(meta);
                }
                inventory.addItem(new ItemStack[]{item});
                if (++count < 25) continue;
                break;
            }
            inventory.setItem(26, this.treasureChecker);
        }
        catch (ConfigException e) {
            ConfigException.handleConfigException(this.plugin, e);
        }
    }

    private void initializeTreasureChecker() {
        this.treasureChecker = new ItemStack(Material.DIRT, 1);
        ItemMeta treasureCheckMeta = this.treasureChecker.getItemMeta();
        assert (treasureCheckMeta != null);
        treasureCheckMeta.setDisplayName("*");
        treasureCheckMeta.setUnbreakable(true);
        treasureCheckMeta.setCustomModelData(Integer.valueOf(2));
        this.treasureChecker.setItemMeta(treasureCheckMeta);
    }

    public static List<Block> getMeteoriteBlockList() {
        return meteoriteBlockList;
    }

    public static void clearMeteoriteBlockList() {
        meteoriteBlockList.clear();
    }
}
