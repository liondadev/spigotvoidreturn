package me.bentonjohnson.spigotvoidreturn.util;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public class LanguageManager {
    private Plugin plugin;
    private String langConfigPrefix = "messages.";

    public LanguageManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public String getLang(String lang, String fallback) {
        String str = this.plugin.getConfig().getString(this.langConfigPrefix + lang);
        if (str == null) {
            return fallback;
        } else {
            return ChatColor.translateAlternateColorCodes('&', str);
        }
    }

    public String getLangTemplated(String lang, String fallback, Map<String, String> templates) {
        String str = this.getLang(lang, fallback);
        if (str == null) {
            return fallback; // This should be impossible, but whatever
        } else {
            for (Map.Entry<String, String> entry: templates.entrySet()) {
                str = str.replace(entry.getKey(), entry.getValue());
                if (str == null) {
                    return fallback;
                }
            }

            return ChatColor.translateAlternateColorCodes('&', str);
        }
    }

    // TODO: Impliment dumpStrings, return a HashMap of all the messages
}
