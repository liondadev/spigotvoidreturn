package me.bentonjohnson.spigotvoidreturn.handlers;

import me.bentonjohnson.spigotvoidreturn.util.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.*;

public class MovementHandler implements Listener {

    Plugin plugin;
    Map<UUID, Location> playerLastPositions;
    LanguageManager languageManager;

    public MovementHandler(Plugin plugin) {
        this.plugin = plugin;
        this.playerLastPositions = new HashMap<UUID, Location>();

        // This manages getting language stuff from the config
        this.languageManager = new LanguageManager(this.plugin);

        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMovement(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!isConfiguredWorld(player.getWorld())) return; // We don't want to do anything if the world is not configured

        if (this.plugin.getConfig().getBoolean("config.ignore_in_creative", true) && player.getGameMode() == GameMode.CREATIVE) return;
        if (this.plugin.getConfig().getBoolean("config.ignore_in_spectator", true) && player.getGameMode() == GameMode.SPECTATOR) return;
        if (this.plugin.getConfig().getBoolean("config.ignore_in_survival", false) && player.getGameMode() == GameMode.SURVIVAL) return;

        if (player.getLocation().getBlockY() <= getTeleportLevel(player.getWorld())) {
            if (this.playerLastPositions.get(player.getUniqueId()) != null) {
                resetVelocity(player);
                player.teleport(this.playerLastPositions.get(player.getUniqueId()).setDirection(player.getLocation().getDirection()));
            } else {
                resetVelocity(player);
                player.teleport(getFallbackLocation(player.getWorld()));
            }

            player.sendMessage(this.languageManager.getLang("teleport_back", "Welcome back."));
        } else {
            if (player.isOnGround()) { // Only save the location if the player is on the ground
                this.playerLastPositions.put(player.getUniqueId(), player.getLocation());
            }
        }
    }

    private void resetVelocity(Player player) {
        Vector v = player.getVelocity();

        v.setX(0);
        v.setY(0.01);
        v.setZ(0);

        player.setVelocity(v);
    }

    private Location getFallbackLocation(World world) {
        // This is wierd to handle for errors
        // Please don't touch this if it doesn't break
        try {
            Configuration config = this.plugin.getConfig();
            String prefix = "worlds_config." + world.getName() + ".fallback";
            double x = config.getDouble(prefix + ".x");
            double y = config.getDouble(prefix + ".y");
            double z = config.getDouble(prefix + ".z");
            double pitch = config.getDouble(prefix + ".pitch");
            double yaw = config.getDouble(prefix + ".yaw");

            Location loc = new Location(world, x, y, z, (float) pitch, (float) yaw);

            return loc;
        } catch (Exception e) {
            return new Location(Bukkit.getWorld("world"), 0, 0, 0);
        }
    }

    private int getTeleportLevel(World world) {
        try {
            int level = this.plugin.getConfig().getInt("worlds_config." + world.getName() + ".level");

            return level;
        } catch (Exception e) {
            return -62;
        }
    }

    private boolean isConfiguredWorld(World world) {
        /*
         This works by looping over all the values in the config to check
         I'll cache this later on, but IDC enough right now... it's late lol
        */
        try {
            /*
             There is definitely a better way of doing this...
             This is probably the equivalent of repairing plumbing with duc-tape (is that how you spell it????)
            */
            List<String> checks = new ArrayList<String>();
            checks.add("level");
            checks.add("fallback.x");
            checks.add("fallback.y");
            checks.add("fallback.z");

            for (String check : checks) {
                try {
                    double value = this.plugin.getConfig().getDouble("worlds_config." + world.getName() + "." + check);
                    double var = value + (double) 5;
                } catch (Exception e) {
                    return false;
                }
            }

            return true; // if we had no errors getting values, just return true
        } catch (Exception e) {
            return false;
        }
    }
}
