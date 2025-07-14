package org.linia.linizen.bridges.ASP;

import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.tags.PseudoObjectTagBase;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.exceptions.CorruptedWorldException;
import com.infernalsuite.asp.api.exceptions.NewerFormatException;
import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import org.linia.linizen.bridges.ASP.commands.FileLoaderCommand;
import org.linia.linizen.bridges.ASP.commands.SlimeWorldCommand;
import org.linia.linizen.bridges.ASP.objects.FileWorldLoaderTag;
import org.linia.linizen.bridges.ASP.objects.SlimeLoaderTag;
import org.linia.linizen.bridges.ASP.objects.SlimeWorldTag;
import org.linia.linizen.bridges.Bridge;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ASPBridge implements Bridge {

    public static AdvancedSlimePaperAPI instance;
    public static Loaders<FileWorldLoaderTag> fileLoaders;

    @Override
    public void init() {
        instance = AdvancedSlimePaperAPI.instance();
        /* ASP Objects */
        ObjectFetcher.registerWithObjectFetcher(FileWorldLoaderTag.class, FileWorldLoaderTag.tagProcessor).generateBaseTag();
        ObjectFetcher.registerWithObjectFetcher(SlimeWorldTag.class, SlimeWorldTag.tagProcessor).generateBaseTag();
        new ASPTagBase();
        /* ASP Commands */
        DenizenCore.commandRegistry.registerCommand(SlimeWorldCommand.class);
        DenizenCore.commandRegistry.registerCommand(FileLoaderCommand.class);
        /* ASP Utils */
        fileLoaders = new Loaders<>();
        TagManager.registerStaticTagBaseHandler(FileWorldLoaderTag.class, FileWorldLoaderTag.class, "fileworldloader", (attribute, input) -> {
            return input;
        });
    }

    static class ASPTagBase extends PseudoObjectTagBase<ASPTagBase> {

        public static ASPTagBase instance;

        public ASPTagBase() {
            instance = this;
            TagManager.registerStaticTagBaseHandler(ASPTagBase.class, "asp", (t) -> instance);
        }

        @Override
        public void register() {

            // <--[tag]
            // @attribute <asp.loaded_worlds>
            // @returns ListTag(SlimeWorldTag)
            // @plugin Linizen, ASP
            // @description
            // Returns a list of all loaded worlds.
            // -->
            tagProcessor.registerTag(ListTag.class, "loaded_worlds", (attribute, object) -> {
                return new ListTag(ASPBridge.instance.getLoadedWorlds(), SlimeWorldTag::new);
            });

            // <--[tag]
            // @attribute <asp.worlds_in_use>
            // @returns ListTag
            // @plugin Linizen, ASP
            // @description
            // Returns a list of all currently used worlds (e.g. in commands).
            // -->
            tagProcessor.registerTag(ListTag.class, "worlds_in_use", (attribute, object) -> {
                return new ListTag(SlimeWorldCommand.worldsInUse, ElementTag::new);
            });

            // <--[tag]
            // @attribute <asp.file_loaders>
            // @returns MapTag
            // @plugin Linizen, ASP
            // @description
            // Returns a map of all known file loaders with their folder names.
            // -->
            tagProcessor.registerTag(MapTag.class, "file_loaders", (attribute, object) -> {
                MapTag map = new MapTag();
                for (Map.Entry<StringHolder, FileWorldLoaderTag> entry : fileLoaders.loaders.entrySet()) {
                    map.putObject(entry.getKey(), entry.getValue());
                }
                return map;
            });
        }
    }

    public static class Loaders<T extends SlimeLoaderTag> {

        private final Map<StringHolder, T> loaders = new HashMap<>();

        public void registerLoader(String name, T loader) {
            loaders.put(new StringHolder(name), loader);
        }

        public T getLoader(String name) {
            return loaders.getOrDefault(new StringHolder(name), null);
        }

        public boolean hasLoader(String name) {
            return loaders.containsKey(new StringHolder(name));
        }

    }

    // https://github.com/InfernalSuite/AdvancedSlimePaper/blob/a192a031930f76dda4d68457e8381090b9e022bc/plugin/src/main/java/com/infernalsuite/asp/plugin/commands/SlimeCommand.java#L30
    public static SlimeWorld getWorldReadyForCloning(String name, SlimeLoader loader, SlimePropertyMap propertyMap) throws CorruptedWorldException, NewerFormatException, UnknownWorldException, IOException {
        return instance.readWorld(loader, name, false, propertyMap);
    }
}
