package org.linia.linizen.oneblock.tags;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.linia.linizen.oneblock.containers.OneBlockPhaseContainer;

import java.util.Collection;

public class OneBlockPhaseTag implements ObjectTag {

    public final OneBlockPhaseContainer.PhaseData phaseData;
    public final String containerName;

    @Fetchable("obp")
    public static OneBlockPhaseTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }
        if (string.startsWith("obp@")) {
            string = string.substring("obp@".length());
        }
        if (!ScriptRegistry.containsScript(string, OneBlockPhaseContainer.class)) {
            return null;
        }
        return new OneBlockPhaseTag(string, ScriptRegistry.getScriptContainerAs(string, OneBlockPhaseContainer.class).loadPhase(context));
    }

    public static boolean matches(String string) {
        return valueOf(string, null) != null;
    }

    public OneBlockPhaseTag(String containerName, OneBlockPhaseContainer.PhaseData phaseData) {
        this.containerName = containerName;
        this.phaseData = phaseData;
    }

    public static void register() {

        tagProcessor.registerStaticTag(ListTag.class, "all_blocks", (attribute, object) -> {
            return new ListTag((Collection<? extends ObjectTag>) object.phaseData.blocks().getKeys());
        });

        tagProcessor.registerTag(MaterialTag.class, "random", (attribute, object) -> {
            return object.phaseData.blocks().getRandom();
        });

        tagProcessor.registerTag(ListTag.class, ElementTag.class, "random_blocks", (attribute, object, input) -> {
            if (!input.isInt()) {
                Debug.echoError("Input must be an integer.");
                return null;
            }
            if (input.asInt() <= 0) {
                return new ListTag();
            }
            return new ListTag(object.phaseData.blocks().getRandom(input.asInt()));
        });

        tagProcessor.registerStaticTag(ElementTag.class, "name", (attribute, object) -> {
            return new ElementTag(object.phaseData.display());
        });

        tagProcessor.registerStaticTag(ElementTag.class, "size", ((attribute, object) -> {
            return new ElementTag(object.phaseData.numOfBlocks());
        }));
    }

    public String prefix = "OneBlockPhase";

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
        return "obp@" + containerName;
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    @Override
    public ObjectTag setPrefix(String s) {
        prefix = s;
        return this;
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public static final ObjectTagProcessor<OneBlockPhaseTag> tagProcessor = new ObjectTagProcessor<>();
}
