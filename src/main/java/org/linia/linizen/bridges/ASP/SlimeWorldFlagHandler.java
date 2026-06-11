package org.linia.linizen.bridges.ASP;

import com.denizenscript.denizencore.flags.SavableMapFlagTracker;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;

import java.util.HashMap;
import java.util.Map;

public class SlimeWorldFlagHandler {

    static final String EXTRA_DATA_KEY = "denizen_flags";

    public static final HashMap<String, SavableMapFlagTracker> trackers = new HashMap<>();

    public static void loadFlags(SlimeWorld world) {
        BinaryTag raw = world.getExtraData().get(EXTRA_DATA_KEY);
        SavableMapFlagTracker tracker = (raw instanceof StringBinaryTag s) ? new SavableMapFlagTracker(s.value()) : new SavableMapFlagTracker();
        tracker.doTotalClean();
        trackers.put(world.getName(), tracker);
    }

    public static void initEmpty(String worldName) {
        trackers.put(worldName, new SavableMapFlagTracker());
    }

    public static void flushToWorld(SlimeWorld world) {
        SavableMapFlagTracker tracker = trackers.get(world.getName());
        if (tracker == null || !tracker.modified) {
            return;
        }
        world.getExtraData().put(EXTRA_DATA_KEY, StringBinaryTag.stringBinaryTag(tracker.toString()));
        tracker.modified = false;
    }

    public static void flushAll() {
        for (Map.Entry<String, SavableMapFlagTracker> entry : trackers.entrySet()) {
            SlimeWorldInstance sw = ASPBridge.instance.getLoadedWorld(entry.getKey());
            if (sw != null) {
                flushToWorld(sw);
            }
        }
    }

    public static void unloadFlags(String worldName) {
        trackers.remove(worldName);
    }
}
