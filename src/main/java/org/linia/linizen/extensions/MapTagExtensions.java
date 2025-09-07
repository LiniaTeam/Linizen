package org.linia.linizen.extensions;

import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.text.StringHolder;

import java.util.HashMap;
import java.util.Map;

public class MapTagExtensions {

    public static void register() {

        // <--[tag]
        // @attribute <MapTag.collect_values>
        // @returns MapTag
        // @description
        //
        // @example
        // # Narrates a map of '[1=<a,b>;2=<c,e>;4=<d>]'
        // - narrate <map[a=1;b=1;c=2;d=4;e=2].invert>
        // -->
        MapTag.tagProcessor.registerStaticTag(MapTag.class, "collect_values", (attribute, object) -> {
            HashMap<StringHolder, ObjectTag> map = new HashMap<>(object.map.size());
            for (Map.Entry<StringHolder, ObjectTag> entry : object.map.entrySet()) {
                ListTag list = (ListTag) map.computeIfAbsent(new StringHolder(entry.getValue().identify()), (k) -> new ListTag());
                list.addObject(new ElementTag(entry.getKey().str));
            }
            return new MapTag(map);
        });
    }
}
