package org.linia.linizen.bridges.ASP;

import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.PseudoObjectTagBase;
import com.denizenscript.denizencore.tags.TagManager;
import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import org.linia.linizen.bridges.ASP.loaders.FileLoader;
import org.linia.linizen.bridges.ASP.objects.SlimeWorldLoaderTag;
import org.linia.linizen.bridges.ASP.objects.SlimeWorldTag;
import org.linia.linizen.bridges.Bridge;

import java.io.File;

public class ASPBridge implements Bridge {

    public static AdvancedSlimePaperAPI instance;

    @Override
    public void init() {
        instance = AdvancedSlimePaperAPI.instance();
        /* ASP Objects */
        ObjectFetcher.registerWithObjectFetcher(SlimeWorldLoaderTag.class, SlimeWorldLoaderTag.tagProcessor).generateBaseTag();
        ObjectFetcher.registerWithObjectFetcher(SlimeWorldTag.class, SlimeWorldTag.tagProcessor).generateBaseTag();
        new ASPTagBase();
        /* ASP Commands */
        DenizenCore.commandRegistry.registerCommand(SlimeWorldCommand.class);
        /* ASP Utils */
        SlimeWorldLoaderTag.registerLoader("file_loader", new FileLoader(new File("slime_worlds")));
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
            // @attribute <asp.loaders>
            // @returns ListTag(SlimeWorldLoaderTag)
            // @plugin Linizen, ASP
            // @description
            // Returns a list of all known loaders.
            // -->
            tagProcessor.registerTag(ListTag.class, "loaders", (attribute, object) -> {
                return new ListTag(SlimeWorldLoaderTag.loaders.values());
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

}
