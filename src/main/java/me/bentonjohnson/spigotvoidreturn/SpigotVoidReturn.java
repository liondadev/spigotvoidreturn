package me.bentonjohnson.spigotvoidreturn;

import me.bentonjohnson.spigotvoidreturn.commands.spigotvoidreturncmd;
import me.bentonjohnson.spigotvoidreturn.handlers.MovementHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpigotVoidReturn extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getLogger().info("SpigotVoidReturn is starting...");

        // Save the default configuration
        saveDefaultConfig();
        saveConfig(); // Just incase ;)

        getCommand("spigotvoidreturn").setExecutor(new spigotvoidreturncmd(this));
        new MovementHandler(this); // No wonder this wasn't working, I never called it...

        Bukkit.getLogger().info("SpigotVoidReturn has started. Run /svp or /spigotvoidreturn to see possible commands.");
    }

    @Override
    public void onDisable() {}
}
