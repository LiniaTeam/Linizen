package org.linia.linizen.bridges.ASP.objects;

import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.loaders.SlimeLoader;

import java.io.*;
import java.nio.file.NotDirectoryException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class FileWorldLoaderTag implements ObjectTag, SlimeLoader {

    private static final String FILE_EXTENSION = ".slime";
    private static final FilenameFilter WORLD_FILE_FILTER = (dir, name) -> name.endsWith(FILE_EXTENSION);
    private static final HashMap<String, FileWorldLoaderTag> loaders = new HashMap<>();

    @Fetchable("fwl")
    public static FileWorldLoaderTag valueOf(String folderName, TagContext context) {
        if (folderName == null) {
            return null;
        }
        if (folderName.startsWith("fwl@")) {
            folderName = folderName.substring("fwl@".length());
        }
        if (loaders.containsKey(folderName)) {
            return loaders.get(folderName);
        }
        File folder = new File(folderName);
        if (folder.exists() && !folder.isDirectory()) {
            Debug.echoError("A file with the same name as the folder '" + folderName + "' already exists. Please delete it and try again.");
            return null;
        }
        if (!folder.exists() && !folder.mkdirs()) {
            Debug.echoError("Failed to create the folder '" + folderName + "'.");
            return null;
        }
        return loaders.put(folderName, new FileWorldLoaderTag(folderName, folder));
    }

    public static boolean matches(String string) {
        return valueOf(string, null) != null;
    }

    public FileWorldLoaderTag(String name, File worldDir) {
        this.worldDir = worldDir;
        this.name = name;
    }

    ///////////////
    /// Object handling
    /////////////

    public static final ObjectTagProcessor<FileWorldLoaderTag> tagProcessor = new ObjectTagProcessor<>();

    public String prefix = "FileWorldLoader";

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
        return "fwl@" + name;
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
    public String toString() {
        return identify();
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public static void register() {

        // <--[tag]
        // @attribute <FileWorldLoaderTag.folder_name>
        // @returns ElementTag
        // @plugin Linizen, ASP
        // @description
        // Returns a name of this folder.
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "folder_name", (attribute, object) -> {
            return new ElementTag(object.name);
        });

        // <--[tag]
        // @attribute <FileWorldLoaderTag.all_worlds>
        // @returns ListTag
        // @plugin Linizen, ASP
        // @description
        // Returns a list of all known worlds loaded and unloaded.
        // -->
        tagProcessor.registerTag(ListTag.class, "all_worlds", (attribute, object) -> {
            try {
                return new ListTag(object.listWorlds(), ElementTag::new);
            } catch (IOException e) {
                Debug.echoError(e);
                return null;
            }
        });

        // <--[tag]
        // @attribute <FileWorldLoaderTag.world_exists[<name>]>
        // @returns ElementTag(Boolean)
        // @plugin Linizen, ASP
        // @description
        // Returns whether the world exists.
        // -->
        tagProcessor.registerTag(ElementTag.class, ElementTag.class, "world_exists", (attribute, object, input) -> {
            return new ElementTag(object.worldExists(input.asString()));
        });
    }

    ///////////////
    /// Loader handling
    /////////////

    private final String name;
    private final File worldDir;

    @Override
    public byte[] readWorld(String worldName) throws UnknownWorldException, IOException {
        if (!worldExists(worldName)) {
            throw new UnknownWorldException(worldName);
        }

        try (FileInputStream fis = new FileInputStream(new File(worldDir, worldName + FILE_EXTENSION))) {
            return fis.readAllBytes();
        }
    }

    @Override
    public boolean worldExists(String worldName) {
        return new File(worldDir, worldName + FILE_EXTENSION).exists();
    }

    @Override
    public List<String> listWorlds() throws NotDirectoryException {
        String[] worlds = worldDir.list(WORLD_FILE_FILTER);

        if (worlds == null) {
            throw new NotDirectoryException(worldDir.getPath());
        }

        return Arrays.stream(worlds).map((c) -> c.substring(0, c.length() - FILE_EXTENSION.length())).collect(Collectors.toList());
    }

    @Override
    public void saveWorld(String worldName, byte[] serializedWorld) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(new File(worldDir, worldName + FILE_EXTENSION))) {
            fos.write(serializedWorld);
        }
    }

    @Override
    public void deleteWorld(String worldName) throws UnknownWorldException, IOException {
        if (!worldExists(worldName)) {
            throw new UnknownWorldException(worldName);
        }
        else {
            if (!new File(worldDir, worldName + FILE_EXTENSION).delete()) {
                throw new IOException("Failed to delete the world file. File#delete() returned false.");
            }
        }
    }

}
