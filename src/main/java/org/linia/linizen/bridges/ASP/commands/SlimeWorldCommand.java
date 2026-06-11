package org.linia.linizen.bridges.ASP.commands;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.infernalsuite.asp.api.exceptions.CorruptedWorldException;
import com.infernalsuite.asp.api.exceptions.NewerFormatException;
import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.exceptions.WorldAlreadyExistsException;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.linia.linizen.bridges.ASP.ASPBridge;
import org.linia.linizen.bridges.ASP.SlimeWorldFlagHandler;
import org.linia.linizen.bridges.ASP.objects.FileWorldLoaderTag;
import org.linia.linizen.bridges.ASP.objects.SlimeWorldTag;
import org.linia.linizen.utils.ExecutorUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;

public class SlimeWorldCommand extends AbstractCommand implements Holdable {

    // <--[command]
    // @Name SlimeWorld
    // @Syntax slimeworld [create/load/clone] [<name>] (loader:<loader>) (from:<world>)
    // @Required 2
    // @Maximum 4
    // @Short Manages slimeworlds.
    // @Group Linizen
    //
    // @Description
    // This can create / load / clone slimeworlds.
    // When using 'create' argument: entries block_location and created_world are available.
    // When using 'load' argument: entry loaded_world is available.
    // When using 'clone' argument: entry cloned_world is available.
    //
    // @Usage
    // Creates a new slimeworld with name 'nice'.
    // - slimeworld create nice loader:<[some_loader]>
    //
    // @Usage
    // Loads an existing slimeworld with given name.
    // - slimeworld load nice loader:<[some_loader]>
    //
    // @Usage
    // Clones a world.
    // - slimeworld clone bobs_island loader:<[some_loader]> from:<[template_world]>
    //
    // -->

    public SlimeWorldCommand() {
        setName("slimeworld");
        setSyntax("slimeworld [create/load/clone] [<name>] (loader:<loader>) (from:<world>)");
        isProcedural = false;
        setRequiredArguments(3, 4);
        autoCompile();
    }

    public enum Action { CREATE, LOAD, CLONE }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("action") @ArgLinear Action action,
                                   @ArgName("name") @ArgLinear String name,
                                   @ArgName("loader") @ArgPrefixed FileWorldLoaderTag loader,
                                   @ArgName("from") @ArgPrefixed @ArgDefaultNull String cloneFrom) {
        switch (action) {
            case CREATE -> {
                if (Bukkit.getWorld(name) != null) {
                    Debug.echoError("World " + name + " already exists");
                    return;
                }
                if (worldsInUse.contains(name)) {
                    Debug.echoError("World already used somewhere");
                    return;
                }
                worldsInUse.add(name);
                CompletableFuture.runAsync(() -> {
                    try {
                        if (loader.worldExists(name)) {
                            Debug.echoError("World " + name + " already exists");
                            return;
                        }
                        SlimePropertyMap propertyMap = new SlimePropertyMap();
                        SlimeWorld slimeWorld = ASPBridge.instance.createEmptyWorld(name, false, propertyMap, loader);
                        ASPBridge.instance.saveWorld(slimeWorld);

                        ExecutorUtil.runSyncAndWait(() -> {
                            try {
                                ASPBridge.instance.loadWorld(slimeWorld, true);
                                SlimeWorldFlagHandler.loadFlags(slimeWorld);
                                World world = Bukkit.getWorld(name);
                                if (world != null) {
                                    Location loc = new Location(world, 0, 61, 0);
                                    loc.getBlock().setType(Material.BEDROCK);
                                    world.setSpawnLocation(loc.add(0, 1, 0));
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
                        worldsInUse.remove(name);
                    }
                });
            }
            case LOAD -> {
                if (!isValid(name)) {
                    return;
                }
                worldsInUse.add(name);
                CompletableFuture.runAsync(() -> {
                    try {
                        SlimeWorld slimeWorld = ASPBridge.instance.readWorld(loader, name, false, new SlimePropertyMap());
                        ExecutorUtil.runSyncAndWait(() -> {
                            try {
                                ASPBridge.instance.loadWorld(slimeWorld, true);
                                SlimeWorldFlagHandler.loadFlags(slimeWorld);
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
                        worldsInUse.remove(name);
                    }
                });
            }
            case CLONE -> {
                if (!isValid(name) || !isValid(cloneFrom)) {
                    return;
                }
                if (name.equals(cloneFrom)) {
                    Debug.echoError("Cannot clone world to itself.");
                    return;
                }
                worldsInUse.add(name);
                worldsInUse.add(cloneFrom);

                CompletableFuture.runAsync(() -> {
                    try {
                        SlimeWorld sw = ASPBridge.getWorldReadyForCloning(cloneFrom, loader, new SlimePropertyMap());
                        SlimeWorld cloned = sw.clone(name, loader);

                        ExecutorUtil.runSyncAndWait(() -> {
                            try {
                                ASPBridge.instance.loadWorld(cloned, true);
                                SlimeWorldFlagHandler.initEmpty(cloned.getName());
                                scriptEntry.saveObject("cloned_world", new SlimeWorldTag(cloned));
                            }
                            catch (IllegalArgumentException e) {
                                Debug.echoError(e);
                            }
                        });
                    }
                    catch (UnknownWorldException | CorruptedWorldException | NewerFormatException | IOException | WorldAlreadyExistsException e) {
                        Debug.echoError(e);
                    }
                    finally {
                        worldsInUse.remove(name);
                        worldsInUse.remove(cloneFrom);
                    }
                });
            }
        }
    }

    public static boolean isValid(String name) {
        if (name == null) {
            Debug.echoError("Null world name.");
            return false;
        }
        if (worldsInUse.contains(name)) {
            Debug.echoError("World " + name + " already used somewhere.");
            return false;
        }
        return true;
    }

    public static HashSet<String> worldsInUse = new HashSet<>();

}
