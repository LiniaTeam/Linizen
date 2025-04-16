package org.linia.linizen.bridges.ASP.objects;

import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.infernalsuite.asp.api.loaders.SlimeLoader;

import java.io.IOException;
import java.util.HashMap;

public class SlimeWorldLoaderTag implements ObjectTag {

    @Fetchable("swl")
    public static SlimeWorldLoaderTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }
        if (string.startsWith("swl@")) {
            string = string.substring("swl@".length());
        }
        return loaders.getOrDefault(string, null);
    }

    public static boolean matches(String arg) {
        return valueOf(arg, null) != null;
    }

    public SlimeLoader loader;
    public String name;

    public SlimeWorldLoaderTag(String name, SlimeLoader slimeLoader) {
        this.loader = slimeLoader;
        this.name = name;
    }

    String prefix = "SlimeWorldLoader";

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
        return "swl@" + name;
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    @Override
    public ObjectTag setPrefix(String s) {
        this.prefix = s;
        return this;
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public static void register() {

        // <--[tag]
        // @attribute <SlimeWorldLoaderTag.all_worlds>
        // @returns ListTag
        // @plugin Linizen, ASP
        // @description
        // Returns a list of all known worlds loaded and unloaded.
        // -->
        tagProcessor.registerTag(ListTag.class, "all_worlds", (attribute, object) -> {
            try {
                return new ListTag(object.loader.listWorlds(), ElementTag::new);
            } catch (IOException e) {
                Debug.echoError(e);
                return null;
            }
        });

        // <--[tag]
        // @attribute <SlimeWorldLoaderTag.world_exists[<name>]>
        // @returns ElementTag(Boolean)
        // @plugin Linizen, ASP
        // @description
        // Returns whether the world exists.
        // -->
        tagProcessor.registerTag(ElementTag.class, ElementTag.class, "world_exists", (attribute, object, input) -> {
            try {
                return new ElementTag(object.loader.worldExists(input.asString()));
            } catch (IOException e) {
                Debug.echoError(e);
                return null;
            }
        });
    }

    public static HashMap<String, SlimeWorldLoaderTag> loaders = new HashMap<>();

    public static <T extends SlimeLoader> void registerLoader(String name, T loader) {
        loaders.put(name, new SlimeWorldLoaderTag(name, loader));
    }

    public static final ObjectTagProcessor<SlimeWorldLoaderTag> tagProcessor = new ObjectTagProcessor<>();
}
