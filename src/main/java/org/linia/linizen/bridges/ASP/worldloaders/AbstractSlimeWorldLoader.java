package org.linia.linizen.bridges.ASP.worldloaders;

import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.infernalsuite.asp.api.loaders.SlimeLoader;

import java.io.IOException;

public abstract class AbstractSlimeWorldLoader implements SlimeLoader, ObjectTag {

    private String prefix;

    public AbstractSlimeWorldLoader(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public AbstractSlimeWorldLoader setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
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
    public String identifySimple() {
        return identify();
    }

    @Override
    public String toString() {
        return identify();
    }

    public static <T extends AbstractSlimeWorldLoader> void registerTags(ObjectTagProcessor<T> tagProcessor) {

        // <--[tag]
        // @attribute <WorldLoaderTag.all_worlds>
        // @returns ListTag
        // @plugin Linizen, ASP
        // @description
        // Returns a list of all known worlds loaded and unloaded.
        // -->
        tagProcessor.registerTag(ListTag.class, "all_worlds", (attribute, object) -> {
            try {
                return new ListTag(object.listWorlds(), ElementTag::new);
            }
            catch (IOException e) {
                attribute.echoError(e);
                return null;
            }
        });

        // <--[tag]
        // @attribute <WorldLoaderTag.world_exists[<name>]>
        // @returns ElementTag(Boolean)
        // @plugin Linizen, ASP
        // @description
        // Returns whether the world exists.
        // -->
        tagProcessor.registerTag(ElementTag.class, ElementTag.class, "world_exists", (attribute, object, input) -> {
            try {
                return new ElementTag(object.worldExists(input.asString()));
            }
            catch (IOException e) {
                attribute.echoError("Could not check if world exists.");
                return null;
            }
        });
    }

}
