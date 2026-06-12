package org.linia.linizen.bridges.ASP.worldloaders;

import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.linia.linizen.utils.LinizenConfig;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SlimeWorldLoaderRegistry {

    public static final HashMap<StringHolder, FileSlimeWorldLoader> fileLoaders = new HashMap<>();

    public static void registerFileLoaders() {
        ObjectFetcher.registerWithObjectFetcher(FileSlimeWorldLoader.class, FileSlimeWorldLoader.tagProcessor);
        TagManager.registerStaticTagBaseHandler(FileSlimeWorldLoader.class, FileSlimeWorldLoader.class, "fileworldloader", (attribute, input) -> input);

        for (Map.Entry<String, String> entry : LinizenConfig.getFileLoaders().entrySet()) {
            String name = entry.getKey();
            String path = entry.getValue();
            File folder = new File(path);
            if (!folder.exists() && !folder.mkdirs()) {
                Debug.echoError("Failed to create loader folder '" + path + "'.");
                continue;
            }
            if (CoreConfiguration.debugVerbose) {
                Debug.echoApproval("Created loader '" + name + "' for folder '" + path + "'");
            }
            fileLoaders.put(new StringHolder(name), new FileSlimeWorldLoader(name, folder));
        }
        Debug.log("Registered <A>" + fileLoaders.size() + "<W> file world loaders.");
    }
}
