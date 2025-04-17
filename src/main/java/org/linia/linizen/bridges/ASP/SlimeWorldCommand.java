package org.linia.linizen.bridges.ASP;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.infernalsuite.asp.api.exceptions.CorruptedWorldException;
import com.infernalsuite.asp.api.exceptions.NewerFormatException;
import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.linia.linizen.bridges.ASP.objects.SlimeWorldLoaderTag;
import org.linia.linizen.bridges.ASP.objects.SlimeWorldTag;
import org.linia.linizen.utils.ExecutorUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;

public class SlimeWorldCommand extends AbstractCommand {

    public SlimeWorldCommand() {
        setName("slimeworld");
        setSyntax("slimeworld [create/load/clone] [<name>] (loader:<loader>)");
        isProcedural = false;
        setRequiredArguments(3, 3);
        setPrefixesHandled("loader");
        autoCompile();
    }

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addWithPrefix("loader:", SlimeWorldLoaderTag.loaders.keySet());
    }

    public enum Action { CREATE, LOAD, CLONE }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("action") Action action,
                                   @ArgName("name") @ArgLinear ElementTag name,
                                   @ArgName("loader") @ArgPrefixed SlimeWorldLoaderTag loaderTag) {
        switch (action) {
            case CREATE -> {
                if (loaderTag == null) {
                    Debug.echoError("Need to specify loader");
                    return;
                }
                String sName = name.asString();
                if (Bukkit.getWorld(sName) != null) {
                    Debug.echoError("World " + sName + " already exists");
                    return;
                }
                if (worldsInUse.contains(sName)) {
                    Debug.echoError("World already used somewhere");
                    return;
                }
                worldsInUse.add(sName);
                CompletableFuture.runAsync(() -> {
                    try {
                        if (loaderTag.loader.worldExists(sName)) {
                            Debug.echoError("World " + sName + " already exists");
                            return;
                        }
                        SlimePropertyMap propertyMap = new SlimePropertyMap();
                        SlimeWorld slimeWorld = ASPBridge.instance.createEmptyWorld(sName, false, propertyMap, loaderTag.loader);
                        ASPBridge.instance.saveWorld(slimeWorld);

                        ExecutorUtil.runSyncAndWait(() -> {
                            try {
                                ASPBridge.instance.loadWorld(slimeWorld, true);
                                World world = Bukkit.getWorld(sName);
                                if (world != null) {
                                    Location loc = new Location(world, 0, 61, 0);
                                    world.setSpawnLocation(loc.add(0, 1, 0));
                                    loc.getBlock().setType(Material.BEDROCK);
                                    scriptEntry.saveObject("block_location", new LocationTag(loc));
                                    scriptEntry.saveObject("created_world", new SlimeWorldTag(slimeWorld));
                                }
                            }
                            catch (Exception e) {
                                Debug.echoError(e);
                            }
                        });
                    }
                    catch (IOException e) {
                        Debug.echoError(e);
                    }
                    finally {
                        worldsInUse.remove(sName);
                    }
                });
            }
            case LOAD -> {
                String sName = name.asString();
                if (!isValid(sName)) {
                    return;
                }
                worldsInUse.add(sName);
                CompletableFuture.runAsync(() -> {
                    try {
                        SlimeWorld slimeWorld = ASPBridge.instance.readWorld(loaderTag.loader, sName, false, new SlimePropertyMap());
                        ExecutorUtil.runSyncAndWait(() -> {
                            try {
                                ASPBridge.instance.loadWorld(slimeWorld, true);
                                scriptEntry.saveObject("loaded_world", new SlimeWorldTag(slimeWorld));
                            }
                            catch (IllegalArgumentException e) {
                                Debug.echoError(e);
                            }
                        });
                    }
                    catch (CorruptedWorldException | NewerFormatException | UnknownWorldException | IOException e) {
                        Debug.echoError(e);
                    }
                    finally {
                        worldsInUse.remove(sName);
                    }
                });
            }
            case CLONE -> {
                // TODO to implement
            }
        }
    }

    public static boolean isValid(String name) {
        if (name == null) {
            Debug.echoError("Null world name");
            return false;
        }
        if (worldsInUse.contains(name)) {
            Debug.echoError("World " + name + " already used somewhere!");
            return false;
        }
        return true;
    }

    public static HashSet<String> worldsInUse = new HashSet<>();

}
