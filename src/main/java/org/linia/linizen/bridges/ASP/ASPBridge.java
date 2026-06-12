package org.linia.linizen.bridges.ASP;

import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.PseudoObjectTagBase;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.exceptions.CorruptedWorldException;
import com.infernalsuite.asp.api.exceptions.NewerFormatException;
import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import org.linia.linizen.bridges.ASP.commands.SlimeWorldCommand;
import org.linia.linizen.bridges.ASP.objects.SlimeWorldTag;
import org.linia.linizen.bridges.ASP.worldloaders.SlimeWorldLoaderRegistry;
import org.linia.linizen.bridges.Bridge;

import java.io.IOException;

public class ASPBridge implements Bridge {

    public static AdvancedSlimePaperAPI instance;

    @Override
    public void init() {
        instance = AdvancedSlimePaperAPI.instance();
        /* ASP Objects */
        ObjectFetcher.registerWithObjectFetcher(SlimeWorldTag.class, SlimeWorldTag.tagProcessor).generateBaseTag();
        new ASPTagBase();
        /* ASP Commands */
        DenizenCore.commandRegistry.registerCommand(SlimeWorldCommand.class);
        /* ASP Utils */
        SlimeWorldLoaderRegistry.registerFileLoaders();
    }

    public static void saveAll() {
        for (SlimeWorldInstance s : instance.getLoadedWorlds()) {
            try {
                instance.saveWorld(s);
            }
            catch (IOException e) {
                Debug.echoError(e);
            }
        }
    }

    public static class ASPTagBase extends PseudoObjectTagBase<ASPTagBase> {

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
            // @attribute <asp.is_loaded[<name>]>
            // @returns ElementTag(Boolean)
            // @plugin Linizen, ASP
            // @description
            // Returns whether the world with a given name is loaded.
            // -->
            tagProcessor.registerTag(ElementTag.class, ElementTag.class, "is_loaded", (attribute, object, input) -> {
                return new ElementTag(ASPBridge.instance.getLoadedWorld(input.asString()) != null);
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
        }
    }

    // https://github.com/InfernalSuite/AdvancedSlimePaper/blob/a192a031930f76dda4d68457e8381090b9e022bc/plugin/src/main/java/com/infernalsuite/asp/plugin/commands/SlimeCommand.java#L30
    public static SlimeWorld getWorldReadyForCloning(String name, SlimeLoader loader, SlimePropertyMap propertyMap) throws CorruptedWorldException, NewerFormatException, UnknownWorldException, IOException {
        return instance.readWorld(loader, name, false, propertyMap);
    }
}
