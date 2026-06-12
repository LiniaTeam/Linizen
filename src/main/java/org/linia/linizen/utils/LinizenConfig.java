package org.linia.linizen.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.linia.linizen.Linizen;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class LinizenConfig {

    public static Map<String, String> getFileLoaders() {
        ConfigurationSection section = Linizen.instance.getConfig().getConfigurationSection("asp.file-loaders");
        if (section == null) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            result.put(key, section.getString(key));
        }
        return result;
    }

}
