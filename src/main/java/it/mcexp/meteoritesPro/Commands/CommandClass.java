package it.mcexp.meteoritesPro.Commands;

import java.util.Objects;
import it.mcexp.meteoritesPro.ExceptionHandling.ConfigException;
import it.mcexp.meteoritesPro.Meteorites.MeteoriteCreator;
import it.mcexp.meteoritesPro.Meteorites.RandomMeteoriteHandler;
import it.mcexp.meteoritesPro.MeteoritesPro;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class CommandClass
        implements CommandExecutor {
    private MeteoritesPro plugin;
    private final String PERMISSIONPREFIX = "meteoritespro.";
    private final String ADMINPERMISSION = "meteoritespro.admin";
    private final String CHATPREFIX = "&9[&6MeteoritesPro&9] ";
    private double meteoriteSpeed = 2.0;

    public CommandClass(MeteoritesPro plugin) {
        this.plugin = plugin;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("mp")) {
            if (!command.getName().equalsIgnoreCase("meteoritespro")) return true;
        }
        Player player = null;
        if (sender instanceof Player) {
            player = ((Player)sender).getPlayer();
        }
        switch (args.length) {
            case 0: {
                if (!sender.hasPermission("meteoritespro.default") && !sender.hasPermission("meteoritespro.admin")) {
                    this.sendPlayerNoPerm(sender);
                    return true;
                }
                this.sendPlayerHelp(sender);
                return true;
            }
            case 1: {
                switch (args[0].toLowerCase()) {
                    case "help": {
                        if (!sender.hasPermission("meteoritespro.default") && !sender.hasPermission("meteoritespro.admin")) {
                            this.sendPlayerNoPerm(sender);
                            return true;
                        }
                        this.sendPlayerHelp(sender);
                        return true;
                    }
                    case "discord": {
                        if (!sender.hasPermission("meteoritespro.default") && !sender.hasPermission("meteoritespro.admin")) {
                            this.sendPlayerNoPerm(sender);
                            return true;
                        }
                        this.sendPlayerDiscord(sender);
                        return true;
                    }
                    case "reload": {
                        if (sender.hasPermission("meteoritespro.reload") || sender.hasPermission("meteoritespro.admin")) {
                            try {
                                this.plugin.reloadConfig();
                                if (RandomMeteoriteHandler.getScheduler() != null) {
                                    RandomMeteoriteHandler.getScheduler().cancelTask(RandomMeteoriteHandler.getSchedulerId());
                                }
                                if (this.plugin.initializePluginRandomizers() == false) throw new ConfigException("There was an error reloading the plugin. Check the console for the error message!");
                                if (!RandomMeteoriteHandler.randomMeteoriteHandler(this.plugin)) throw new ConfigException("There was an error reloading the plugin. Check the console for the error message!");
                                sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9[&6MeteoritesPro&9] &aSuccessfully reloaded the plugin"));
                                return true;
                            }
                            catch (ConfigException e) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)("&9[&6MeteoritesPro&9] &c" + e.getMessage())));
                                return true;
                            }
                        }
                        this.sendPlayerNoPerm(sender);
                        return true;
                    }
                    case "shoot": {
                        if (!sender.hasPermission("meteoritespro.shoot") && !sender.hasPermission("meteoritespro.admin")) {
                            this.sendPlayerNoPerm(sender);
                            return true;
                        }
                        if (player != null) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9[&6MeteoritesPro&9] &cPlease specify the meteorite you want to shoot"));
                            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)("&9[&6MeteoritesPro&9] &cYour available meteorite names are: &a" + this.getAvailableMeteoriteNames() + ".")));
                            return true;
                        }
                        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9[&6MeteoritesPro&9] &cYou must be in game to execute this command"));
                        return true;
                    }
                    case "shootrandom": {
                        if (!sender.hasPermission("meteoritespro.shootrandom")) {
                            if (!sender.hasPermission("meteoritespro.admin")) return true;
                        }
                        RandomMeteoriteHandler.shootRandomMeteorite(this.plugin);
                        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9[&6MeteoritesPro&9] &7Shooting &9Meteorite &7. . . "));
                        return true;
                    }
                    case "start": {
                        if (!sender.hasPermission("meteoritespro.start") && !sender.hasPermission("meteoritespro.admin")) {
                            this.sendPlayerNoPerm(sender);
                            return true;
                        }
                        if (!this.plugin.getConfig().getBoolean("enable-random-meteorites")) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9[&6MeteoritesPro&9] &cRandom meteorites are disabled in the config.yml"));
                            return true;
                        }
                        if (!RandomMeteoriteHandler.getScheduler().isCurrentlyRunning(RandomMeteoriteHandler.getSchedulerId()) && !RandomMeteoriteHandler.getScheduler().isQueued(RandomMeteoriteHandler.getSchedulerId())) {
                            RandomMeteoriteHandler.randomMeteoriteHandler(this.plugin);
                            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9[&6MeteoritesPro&9] &aRandom meteorites will now fall"));
                            return true;
                        }
                        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9[&6MeteoritesPro&9] &cRandom meteorites are already falling"));
                        return true;
                    }
                    case "stop": {
                        if (!sender.hasPermission("meteoritespro.stop") && !sender.hasPermission("meteoritespro.admin")) {
                            this.sendPlayerNoPerm(sender);
                            return true;
                        }
                        if (!RandomMeteoriteHandler.getScheduler().isCurrentlyRunning(RandomMeteoriteHandler.getSchedulerId()) && !RandomMeteoriteHandler.getScheduler().isQueued(RandomMeteoriteHandler.getSchedulerId())) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9[&6MeteoritesPro&9] &cRandom meteorites are not falling"));
                            return true;
                        }
                        RandomMeteoriteHandler.getScheduler().cancelTask(RandomMeteoriteHandler.getSchedulerId());
                        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9[&6MeteoritesPro&9] &aRandom meteorites have stopped falling"));
                        return true;
                    }
                }
                this.sendPlayerUnknownCommand(sender);
                return true;
            }
            case 2: {
                switch (args[0].toLowerCase()) {
                    case "shoot": {
                        if (!sender.hasPermission("meteoritespro.shoot") && !sender.hasPermission("meteoritespro.admin")) {
                            this.sendPlayerNoPerm(sender);
                            return true;
                        }
                        if (player == null) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9[&6MeteoritesPro&9] &cYou must be in game to execute this command"));
                            return true;
                        }
                        if (this.plugin.getConfig().contains("meteorites", true) && Objects.requireNonNull(this.plugin.getConfig().getConfigurationSection("meteorites")).getKeys(false).contains(args[1])) {
                            ConfigurationSection config = this.plugin.getConfig().getConfigurationSection("meteorites." + args[1]);
                            if (config == null) return true;
                            if (config.contains("meteorite-speed", true)) {
                                this.meteoriteSpeed = config.getDouble("meteorite-speed");
                                if (this.meteoriteSpeed > 5.0) {
                                    this.meteoriteSpeed = 5.0;
                                } else if (this.meteoriteSpeed < 0.0) {
                                    this.meteoriteSpeed = 1.0;
                                }
                            }
                            if (MeteoriteCreator.createMeteorite(this.getPlayerLocationForMeteorite(player, 10), this.calculateMeteoriteVectorFromPlayersView(player, this.meteoriteSpeed), this.plugin, config)) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9[&6MeteoritesPro&9] &7Shooting &9Meteorite &7. . . "));
                                return true;
                            }
                            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9[&6MeteoritesPro&9] &cFailed to shoot meteorite. Check the console for the error!"));
                            return true;
                        }
                        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)("&9[&6MeteoritesPro&9] &cInvalid meteorite name: '" + args[1] + "'")));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)("&9[&6MeteoritesPro&9] &cYour available meteorite names are: &a" + this.getAvailableMeteoriteNames() + ".")));
                        return true;
                    }
                }
                this.sendPlayerUnknownCommand(sender);
                return true;
            }
        }
        if (sender.hasPermission("meteoritespro.default") || sender.hasPermission("meteoritespro.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9[&6MeteoritesPro&9] &cToo many arguments"));
            this.sendPlayerHelp(sender);
            return true;
        }
        this.sendPlayerNoPerm(sender);
        return true;
    }

    private void sendPlayerUnknownCommand(CommandSender sender) {
        if (sender.hasPermission("meteoritespro.default") || sender.hasPermission("meteoritespro.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9[&6MeteoritesPro&9] &cUnknown command"));
            this.sendPlayerHelp(sender);
        } else {
            this.sendPlayerNoPerm(sender);
        }
    }

    private Location getPlayerLocationForMeteorite(Player player, int amountOfBlocksAbovePlayer) {
        Location location = player.getLocation();
        location.add(0.0, (double)amountOfBlocksAbovePlayer, 0.0);
        double x = (int)location.getX();
        double z = (int)location.getZ();
        x = x >= 0.0 ? (x += 0.5) : (x -= 0.5);
        z = z >= 0.0 ? (z += 0.5) : (z -= 0.5);
        location.setX(x);
        location.setZ(z);
        return location;
    }

    private Vector calculateMeteoriteVectorFromPlayersView(Player player, double speed) {
        return new Vector(player.getLocation().getDirection().getX() * speed, player.getLocation().getDirection().getY() * speed, player.getLocation().getDirection().getZ() * speed);
    }

    private void sendPlayerHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m--------------------&9&l<< &6MeteoritesPro&9&l >>&r&8&m--------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&f/mp reload &3- &7Reload the plugin's configuration"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&f/mp shoot <name> &3- &7Shoot a meteorite in the direction you're facing"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&f/mp shootrandom &3- &7Shoot a random meteorite in your random area"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&f/mp stop &3- &7Random meteorites stop falling"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&f/mp start &3- &7Random meteorites start falling"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&f/mp help &3- &7Open this menu"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&f/mp discord &3- &7Join our support Discord server"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&8&m----------------------------------------------------------"));
    }

    private void sendPlayerDiscord(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9[&6MeteoritesPro&9] &3https://discord.gg/47YEbMm &e&l<< &e(Click me)"));
    }

    private void sendPlayerNoPerm(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)"&9[&6MeteoritesPro&9] &cYou have no permission to perform that command"));
    }

    private String getAvailableMeteoriteNames() {
        StringBuilder meteoriteNames = new StringBuilder();
        for (String meteoriteStringName : Objects.requireNonNull(this.plugin.getConfig().getConfigurationSection("meteorites")).getKeys(false)) {
            meteoriteNames.append("'").append(meteoriteStringName).append("', ");
        }
        meteoriteNames.delete(meteoriteNames.length() - 2, meteoriteNames.length());
        return meteoriteNames.toString();
    }
}
