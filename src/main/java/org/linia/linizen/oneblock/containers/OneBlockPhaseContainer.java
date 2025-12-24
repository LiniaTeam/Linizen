package org.linia.linizen.oneblock.containers;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.linia.linizen.utils.WeightedRandomList;

import java.util.Map;

public class OneBlockPhaseContainer extends ScriptContainer {

    public OneBlockPhaseContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        canRunScripts = false;
    }

    public PhaseData loadPhase(TagContext context) {
        String display = null;
        int numOfBlocks = -1;
        WeightedRandomList<MaterialTag> blocks = null;
        if (contains("name")) {
            display = TagManager.tag(getString("name"), context);
        }
        if (contains("number of blocks")) {
            numOfBlocks = Integer.parseInt(TagManager.tag(getString("number of blocks"), context));
        }
        if (contains("blocks", Map.class)) {
            blocks = new WeightedRandomList<>();
            YamlConfiguration section = getConfigurationSection("blocks");
            for (StringHolder material : section.getKeys(false)) {
                MaterialTag mat = MaterialTag.valueOf(material.str, context);
                if (mat == null) {
                    Debug.echoError("Material '" + material.str + "' not found. Skipping...");
                    continue;
                }
                int weight = Integer.parseInt(section.getString(material.str));
                blocks.add(mat, weight);
            }
        }
        return new PhaseData(display, numOfBlocks, blocks);
    }

    public record PhaseData(String display, int numOfBlocks, WeightedRandomList<MaterialTag> blocks) {
    }
}
