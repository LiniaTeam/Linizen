package org.linia.linizen;

import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.linia.linizen.bridges.ASP.ASPBridge;
import org.linia.linizen.bridges.ASP.SlimeWorldFlagHandler;
import org.linia.linizen.extensions.ExtensionsRegistry;
import org.linia.linizen.oneblock.OneBlock;

public class Linizen extends JavaPlugin {

    public static Plugin instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        Debug.log("Linizen loading...");
        new ASPBridge().init();
        OneBlock.init();
        ExtensionsRegistry.register();
    }

    @Override
    public void onDisable() {
        Debug.log("Saving slimeworlds...");
        SlimeWorldFlagHandler.flushAll();
        ASPBridge.saveAll();
    }

}
