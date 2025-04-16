package org.linia.linizen;

import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.linia.linizen.bridges.ASP.ASPBridge;

import java.io.IOException;

public class Linizen extends JavaPlugin {

    public static Plugin instance;

    @Override
    public void onEnable() {
        instance = this;
        Debug.log("Linizen loading...");
        saveDefaultConfig();
        new ASPBridge().init();
    }

    @Override
    public void onDisable() {

        Debug.log("Saving slimeworlds...");
        for (SlimeWorldInstance s : ASPBridge.instance.getLoadedWorlds()) {
            try {
                ASPBridge.instance.saveWorld(s);
            }
            catch (IOException e) {
                Debug.echoError(e);
            }
        }
    }

}
