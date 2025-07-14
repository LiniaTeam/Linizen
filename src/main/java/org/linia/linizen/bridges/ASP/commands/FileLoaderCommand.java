package org.linia.linizen.bridges.ASP.commands;

import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;
import com.denizenscript.denizencore.scripts.commands.generator.ArgPrefixed;
import com.denizenscript.denizencore.scripts.commands.generator.ArgSubType;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.linia.linizen.bridges.ASP.ASPBridge;
import org.linia.linizen.bridges.ASP.objects.FileWorldLoaderTag;

import java.io.File;
import java.util.List;

public class FileLoaderCommand extends AbstractCommand implements Holdable {

    public FileLoaderCommand() {
        setName("fileloader");
        setSyntax("fileloader [create:<name>|...]");
        isProcedural = false;
        setRequiredArguments(1, 1);
        autoCompile();
    }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("create") @ArgPrefixed @ArgSubType(ElementTag.class) List<ElementTag> names) {
        for (ElementTag e : names) {
            String folderName = e.asString();
            File folder = new File(folderName);
            if (folder.exists() && !folder.isDirectory()) {
                Debug.echoError("A file with the same name as the folder '" + folderName + "' already exists. Please delete it and try again.");
                continue;
            }
            if (!folder.exists() && !folder.mkdirs()) {
                Debug.echoError("Failed to create the folder '" + folderName + "'.");
                continue;
            }
            FileWorldLoaderTag fwl = new FileWorldLoaderTag(folderName, folder);
            ASPBridge.fileLoaders.registerLoader(folderName, fwl);
        }
    }

}
