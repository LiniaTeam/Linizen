package org.linia.linizen.oneblock;

import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.tags.TagManager;
import org.linia.linizen.oneblock.containers.OneBlockPhaseContainer;
import org.linia.linizen.oneblock.tags.OneBlockPhaseTag;

public class OneBlock {

    public static void init() {
        // Register tags
        ObjectFetcher.registerWithObjectFetcher(OneBlockPhaseTag.class, OneBlockPhaseTag.tagProcessor).generateBaseTag();
        TagManager.registerStaticTagBaseHandler(OneBlockPhaseTag.class, OneBlockPhaseTag.class, "oneblockphase", (attribute, input) -> input);

        // Register script containers
        ScriptRegistry._registerType("phase", OneBlockPhaseContainer.class);
    }

}
