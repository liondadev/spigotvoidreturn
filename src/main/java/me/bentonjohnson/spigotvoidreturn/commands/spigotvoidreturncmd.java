package me.bentonjohnson.spigotvoidreturn.commands;

import me.bentonjohnson.spigotvoidreturn.util.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class spigotvoidreturncmd implements CommandExecutor {
    private Plugin plugin;
    private LanguageManager languageManager;
    private String[] HelpMessage = {
            "&e/svoid help &7- Display this message ",
            "&e/svoid enable (world) &7- Enable for a certain world.",
            "&b^^ &7Defaults to current world.",
            "&e/svoid disable (world) &7- Disable for a certain world.",
            "&b^^ &7Defaults to current world.",
            "&e/svoid setylevel (world) (ylevel) &7- Sets the y-level for a certain world.",
            "&b^^ &7Defaults to your current y-level and world.",
            "&e/svoid setfallback &7- Sets your current position (and direction) to the fallback location incase we can't return the player normaly",
            "&b^^ &7Per World",
            "&c&lA fallback location and y-level is required for the plugin to work properly.",
            "&e/svoid reload &7- Reload the configuration"
    };

    public spigotvoidreturncmd(Plugin plugin) {
        this.plugin = plugin;
        this.languageManager = new LanguageManager(this.plugin); // Init the language utility
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            sendHelpMessage(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("enable")) {
            Boolean hasPermission = checkPermission(player, "svoid.admin.setfallback");
            if (!hasPermission) return true;

            if (args.length == 1) {
                this.toggleForWorld(player.getWorld().getName(), true, 0);

                Map<String, String> templates = new HashMap<String, String>();
                templates.put("{WORLD}", player.getWorld().getName());
                player.sendMessage(this.languageManager.getLangTemplated("enabled", "Enabled for {WORLD}", templates));
            } else {
                // We don't use the world later, but we should still check if it's valid.
                World world = Bukkit.getWorld(args[1]);
                if (world == null) {
                    player.sendMessage("That world could not be found. (" + args[1] + ")");
                    return true;
                }
                this.toggleForWorld(args[1], true, 0);

                Map<String, String> templates = new HashMap<String, String>();
                templates.put("{WORLD}", args[1]);
                player.sendMessage(this.languageManager.getLangTemplated("enabled", "Enabled for {WORLD}", templates));
            }
            return true;
        } else if (args[0].equalsIgnoreCase("disable")) {
            Boolean hasPermission = checkPermission(player, "svoid.admin.toggle");
            if (!hasPermission) return true;

            if (args.length == 1) {
                this.toggleForWorld(player.getWorld().getName(), false, 0);

                Map<String, String> templates = new HashMap<String, String>();
                templates.put("{WORLD}", player.getWorld().getName());
                player.sendMessage(this.languageManager.getLangTemplated("disabled", "Disabled for {WORLD}", templates));
            } else {
                // We don't use the world later, but we should still check if it's valid.
                World world = Bukkit.getWorld(args[1]);
                if (world == null) {
                    player.sendMessage(this.languageManager.getLang("unknown_world", "That world could not be found."));
                    return true;
                }
                this.toggleForWorld(args[1], false, 0);
                Map<String, String> templates = new HashMap<String, String>();
                templates.put("{WORLD}", args[1]);
                player.sendMessage(this.languageManager.getLangTemplated("disabled", "Disabled for {WORLD}", templates));
            }
            return true;
        } else if (args[0].equalsIgnoreCase("setylevel")) {
            Boolean hasPermission = checkPermission(player, "svoid.admin.setylevel");
            if (!hasPermission) return true;

            World world = player.getWorld();
            int ylevel = player.getLocation().getBlockY();


            if (args.length == 2) {
                world = Bukkit.getWorld(args[1]);
                if (world == null) {
                    player.sendMessage(this.languageManager.getLang("unknown_world", "That world could not be found."));

                    return true;
                }
            } else if (args.length == 3) {
                world = Bukkit.getWorld(args[1]);
                if (world == null) {
                    player.sendMessage(this.languageManager.getLang("unknown_world", "That world could not be found."));

                    return true;
                }
                try {
                    ylevel = Integer.parseInt(args[2]);
                } catch (final NumberFormatException e) {
                    player.sendMessage(this.languageManager.getLang("invalid_number", "That isn't a number."));

                    return true;
                }
            }

            toggleForWorld(world.getName(), true, ylevel);

            Map<String, String> templates = new HashMap<String, String>();
            templates.put("{WORLD}", world.getName());
            templates.put("{Y_LEVEL}", String.valueOf(ylevel));
            player.sendMessage(this.languageManager.getLangTemplated("set_y_level", "{WORLD} = {Y_LEVEL}", templates));

            return true;
        } else if (args[0].equalsIgnoreCase("setfallback")) {
            Boolean hasPermission = checkPermission(player, "svoid.admin.setfallback");
            if (!hasPermission) return true;

            setFallbackLocation(player.getWorld(), player.getLocation());
            return true;
        } else if (args[0].equalsIgnoreCase("reload")) {
            Boolean hasPermission = checkPermission(player, "svoid.admin.reload");
            if (!hasPermission) return true;

            this.plugin.reloadConfig();
            player.sendMessage(this.languageManager.getLang("config_reloaded", "No permission."));
            return true;
        } else {
            sendHelpMessage(player);
            return true;
        }
    }

    private void toggleForWorld(String worldName, Boolean enabled, int y_level) {
        // We just default it to zero
        this.plugin.getConfig().set("worlds_config." + worldName + ".level", (enabled) ? y_level : null);
        this.plugin.saveConfig();
    }

    private boolean checkPermission(Player player, String permission) {
        if (!player.hasPermission(permission)) {
            player.sendMessage(this.languageManager.getLang("no_permission", "No permission."));

            return false;
        }

        return true;
    }

    private void setFallbackLocation(World world, Location location) {
        // Get all the values
        String worldName = world.getName();

        // Extract all the variables from the location
        double x = location.getBlockX();
        double y = location.getBlockY();
        double z = location.getBlockZ();
        double pitch = Math.round(location.getPitch() / 20) * 20;
        double yaw = Math.round(location.getYaw() / 20) * 20;

        // Time to update everything
        this.plugin.getConfig().set("worlds_config." + worldName + ".fallback.x", x);
        this.plugin.getConfig().set("worlds_config." + worldName + ".fallback.y", y);
        this.plugin.getConfig().set("worlds_config." + worldName + ".fallback.z", z);
        this.plugin.getConfig().set("worlds_config." + worldName + ".fallback.pitch", pitch);
        this.plugin.getConfig().set("worlds_config." + worldName + ".fallback.yaw", yaw);
        this.plugin.saveConfig(); // Save the config
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(this.languageManager.getLang("help_header", "&eSpigotVoidReturn Help"));
        for (String text : HelpMessage) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', text));
        }
        player.sendMessage(this.languageManager.getLang("help_footer", "&e=================="));
    }
}
