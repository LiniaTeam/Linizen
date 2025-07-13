package org.linia.linizen.bridges.ASP.objects;

import com.denizenscript.denizen.objects.WorldTag;
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

import java.io.IOException;

public class SlimeWorldTag implements ObjectTag, Adjustable {

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

    /*
    * Maybe in the future if it becomes RAM issue, we could save only worldsName
    * and not whole instance. In which case we would need to reconstruct it each
    * time we are calling it. (With something like SlimeWorldTag#getSlimeWorld)
    */
    public SlimeWorld slimeWorld;

    public SlimeWorldTag(SlimeWorld slimeWorld) {
        this.slimeWorld = slimeWorld;
    }

    public static void register() {

        // <--[tag]
        // @attribute <SlimeWorldTag.is_loaded>
        // @returns ElementTag(Boolean)
        // @plugin Linizen, ASP
        // @description
        // Returns whether the world is actually loaded.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_loaded", (attribute, object) -> {
            try {
                return new ElementTag(object.slimeWorld.getLoader().worldExists(object.slimeWorld.getName()));
            } catch (IOException e) {
                return new ElementTag(false);
            }
        });

        // <--[tag]
        // @attribute <SlimeWorldTag.get_loader>
        // @returns FileWorldLoaderTag
        // @plugin Linizen, ASP
        // @description
        // Returns a file loader which this world is loaded from, if any.
        // -->
        tagProcessor.registerTag(FileWorldLoaderTag.class, "get_file_loader", (attribute, object) -> {
            return object.slimeWorld.getLoader() instanceof FileWorldLoaderTag f ? f : null;
        });

        // <--[tag]
        // @attribute <SlimeWorldTag.as_world>
        // @returns WorldTag
        // @plugin Linizen, ASP
        // @description
        // Returns a world representing this slimeworld.
        // -->
        tagProcessor.registerTag(WorldTag.class, "as_world", (attribute, object) -> {
            return new WorldTag(object.getWorld());
        });

        // <--[mechanism]
        // @object SlimeWorldTag
        // @name save
        // @plugin Linizen, ASP
        // @description
        // Saves the world.
        // -->
        tagProcessor.registerMechanism("save", false, (object, mechanism) -> {
            try {
                ASPBridge.instance.saveWorld(object.slimeWorld);
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
            if (!input.isBoolean()) {
                mechanism.echoError("Must provide boolean");
                return;
            }
            World world = object.getWorld();
            if (!world.getPlayers().isEmpty()) {
                mechanism.echoError("Could not unload world, players are there.");
                return;
            }
            if (!Bukkit.unloadWorld(world, input.asBoolean())) {
                mechanism.echoError("Saving for SlimeWorld " + world.getName() + " refused by system.");
            }
        });
    }

    public World getWorld() {
        return Bukkit.getWorld(slimeWorld.getName());
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
        return "sw@" + slimeWorld.getName();
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
