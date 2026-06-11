package org.linia.linizen.bridges.ASP.objects;

import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.objects.Adjustable;
import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.linia.linizen.bridges.ASP.ASPBridge;
import org.linia.linizen.bridges.ASP.SlimeWorldFlagHandler;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class SlimeWorldTag implements ObjectTag, Adjustable, FlaggableObject {

    @Fetchable("sw@")
    public static SlimeWorldTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }
        if (string.startsWith("sw@")) {
            string = string.substring("sw@".length());
        }
        SlimeWorldInstance s = ASPBridge.instance.getLoadedWorld(string);
        return s == null ? null : new SlimeWorldTag(s);
    }

    public static boolean matches(String string) {
        return valueOf(string, null) != null;
    }

    private final String worldName;
    private final WeakReference<SlimeWorld> slimeWorldRef;

    public SlimeWorldTag(SlimeWorld slimeWorld) {
        this.worldName = slimeWorld.getName();
        this.slimeWorldRef = new WeakReference<>(slimeWorld);
    }

    public SlimeWorld getSlimeWorld() {
        return slimeWorldRef.get();
    }

    @Override
    public AbstractFlagTracker getFlagTracker() {
        return SlimeWorldFlagHandler.trackers.get(worldName);
    }

    @Override
    public void reapplyTracker(AbstractFlagTracker tracker) {
    }

    @Override
    public String getReasonNotFlaggable() {
        return "is the SlimeWorld loaded?";
    }

    public static void register() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);

        // <--[tag]
        // @attribute <SlimeWorldTag.get_file_loader>
        // @returns FileWorldLoaderTag
        // @plugin Linizen, ASP
        // @description
        // Returns a file loader which this world is loaded from, if any.
        // -->
        tagProcessor.registerTag(FileWorldLoaderTag.class, "get_file_loader", (attribute, object) -> {
            if (!requireLoadedWorld(object, attribute)) {
                return null;
            }
            return (object.getSlimeWorld().getLoader() instanceof FileWorldLoaderTag f) ? f : null;
        });

        // <--[tag]
        // @attribute <SlimeWorldTag.as_world>
        // @returns WorldTag
        // @plugin Linizen, ASP
        // @description
        // Returns a world representing this slimeworld.
        // -->
        tagProcessor.registerTag(WorldTag.class, "as_world", (attribute, object) -> {
            return requireLoadedWorld(object, attribute) ? new WorldTag(object.getWorld()) : null;
        });

        // <--[tag]
        // @attribute <SlimeWorldTag.name>
        // @returns ElementTag
        // @plugin Linizen, ASP
        // @description
        // Returns the name of the slimeworld. (Works even when unloaded)
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "name", (attribute, object) -> {
            return new ElementTag(object.worldName);
        });

        // <--[mechanism]
        // @object SlimeWorldTag
        // @name save
        // @plugin Linizen, ASP
        // @description
        // Saves the world.
        // -->
        tagProcessor.registerMechanism("save", false, (object, mechanism) -> {
            if (!requireLoadedWorld(object, mechanism)) {
                return;
            }
            SlimeWorld sw = object.getSlimeWorld();
            try {
                SlimeWorldFlagHandler.flushToWorld(sw);
                ASPBridge.instance.saveWorld(sw);
            } catch (IOException e) {
                mechanism.echoError("Could not save world");
            }
        });

        // <--[mechanism]
        // @object SlimeWorldTag
        // @input Boolean whether the world should save
        // @name unload
        // @plugin Linizen, ASP
        // @description
        // Unloads a world
        // -->
        tagProcessor.registerMechanism("unload", false, ElementTag.class, (object, mechanism, input) -> {
            if (!requireLoadedWorld(object, mechanism)) {
                return;
            }
            if (!input.isBoolean()) {
                mechanism.echoError("Must provide boolean");
                return;
            }
            World world = object.getWorld();
            if (!world.getPlayers().isEmpty()) {
                mechanism.echoError("Could not unload world, players are there.");
                return;
            }
            boolean inp = input.asBoolean();
            if (inp) {
                SlimeWorldFlagHandler.flushToWorld(object.getSlimeWorld());
            }
            if (!Bukkit.unloadWorld(world, inp)) {
                mechanism.echoError("Saving for SlimeWorld " + world.getName() + " refused by system.");
            }
            else {
                SlimeWorldFlagHandler.unloadFlags(object.worldName);
            }
        });
    }

    public static boolean requireLoadedWorld(SlimeWorldTag slimeWorldTag, Mechanism mechanism) {
        if (!slimeWorldTag.isLoaded()) {
            mechanism.echoError("World '" + slimeWorldTag.worldName + "' is unloaded, cannot adjust mechanism.");
            return false;
        }
        return true;
    }

    public static boolean requireLoadedWorld(SlimeWorldTag slimeWorldTag, Attribute attribute) {
        if (!slimeWorldTag.isLoaded()) {
            attribute.echoError("World '" + slimeWorldTag.worldName + "' is unloaded, cannot process tag.");
            return false;
        }
        return true;
    }

    public boolean isLoaded() {
        return getSlimeWorld() == null || !ASPBridge.instance.worldLoaded(getSlimeWorld());
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public String prefix = "SlimeWorld";

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public String identify() {
        return "sw@" + worldName;
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public ObjectTag setPrefix(String s) {
        this.prefix = s;
        return this;
    }

    @Override
    public void adjust(Mechanism mechanism) {
        tagProcessor.processMechanism(this, mechanism);
    }

    @Override
    public void applyProperty(Mechanism mechanism) {
        mechanism.echoError("Cannot apply Properties to a SlimeWorldTag!");
    }
    
    public static final ObjectTagProcessor<SlimeWorldTag> tagProcessor = new ObjectTagProcessor<>();
}
