package org.linia.linizen.bridges.ASP.worldloaders;

import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import com.infernalsuite.asp.api.exceptions.UnknownWorldException;

import java.io.*;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.List;

public class FileSlimeWorldLoader extends AbstractSlimeWorldLoader {

    private static final String FILE_EXTENSION = ".slime";
    private static final FilenameFilter WORLD_FILE_FILTER = (dir, name) -> name.endsWith(FILE_EXTENSION);

    @Fetchable("fwl")
    public static FileSlimeWorldLoader valueOf(String name, TagContext context) {
        if (name == null) {
            return null;
        }
        if (name.startsWith("fwl@")) {
            name = name.substring("fwl@".length());
        }
        FileSlimeWorldLoader result = SlimeWorldLoaderRegistry.fileLoaders.getOrDefault(new StringHolder(name), null);
        if (result == null) {
            Debug.echoError("File world loader '" + name + "' not found. Make sure it is in config file!");
            return null;
        }
        return result;
    }

    public static boolean matches(String name) {
        if (name == null) {
            return false;
        }
        if (name.startsWith("fwl@")) {
            name = name.substring("fwl@".length());
        }
        return SlimeWorldLoaderRegistry.fileLoaders.containsKey(new StringHolder(name));
    }

    private final String name;
    private final File worldDir;

    public FileSlimeWorldLoader(String name, File worldDir) {
        super("FileWorldLoader");
        this.worldDir = worldDir;
        this.name = name;
    }

    ///////////////
    /// Object handling
    /////////////

    public static final ObjectTagProcessor<FileSlimeWorldLoader> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public static void register() {
        AbstractSlimeWorldLoader.registerTags(tagProcessor);

        // <--[tag]
        // @attribute <FileWorldLoader.folder_name>
        // @returns ElementTag
        // @plugin Linizen, ASP
        // @description
        // Returns a name of this folder.
        // -->
        tagProcessor.registerStaticTag(ElementTag.class, "folder_name", (attribute, object) -> {
            return new ElementTag(object.worldDir.getName());
        });
    }

    @Override
    public String identify() {
        return "fwl@" + name;
    }

    ///////////////
    /// Loader handling
    /////////////

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
        List<String> result = new ArrayList<>(worlds.length);
        for (String world : worlds) {
            result.add(world.substring(0, world.length() - FILE_EXTENSION.length()));
        }
        return result;
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
        if (!new File(worldDir, worldName + FILE_EXTENSION).delete()) {
                throw new IOException("Failed to delete the world file. File#delete() returned false.");
        }
    }
}
