package tk.elektrofuchse.fox.foxguard;

import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.File;
import java.io.IOException;

/**
 * Created by Fox on 11/1/2015.
 * Project: foxguard
 */
public class FGConfigManager {

    private static FGConfigManager instance;


    public boolean forceLoad;
    public boolean purgeDatabases;


    public FGConfigManager() {
        if (instance == null) instance = this;
        load();
    }

    public void load() {
        File configFile = new File(
                FoxGuardMain.getInstance().getConfigDirectory().getPath() + "/foxguard.cfg");
        CommentedConfigurationNode root;
        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setFile(configFile).build();
        if (configFile.exists()) {
            try {
                root = loader.load();
            } catch (IOException e) {
                root = loader.createEmptyNode(ConfigurationOptions.defaults());
            }
        } else {
            root = loader.createEmptyNode(ConfigurationOptions.defaults());
        }
        //--------------------------------------------------------------------------------------------------------------

        forceLoad = root.getNode("storage", "forceLoad").getBoolean(false);
        purgeDatabases = root.getNode("storage", "purgeDatabases").getBoolean(true);

        //--------------------------------------------------------------------------------------------------------------
    }

    public void save() {
        File configFile = new File(
                FoxGuardMain.getInstance().getConfigDirectory().getPath() + "/foxguard.cfg");
        CommentedConfigurationNode root;
        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setFile(configFile).build();
        if (configFile.exists()) {
            try {
                root = loader.load();
            } catch (IOException e) {
                root = loader.createEmptyNode(ConfigurationOptions.defaults());
            }
        } else {
            root = loader.createEmptyNode(ConfigurationOptions.defaults());
        }
        //--------------------------------------------------------------------------------------------------------------

        root.getNode("storage", "forceLoad").setComment("Enables force loading of Region and FlagSet databases. Default: false\n" +
                "This allows loading of Regions and FlagSets whose metadata don't match saved records.\n" +
                "This usually occurs when a database file is replaced with another of the same name, but the internal metadata doesn't match.\n" +
                "FoxGuard will attempt to resolve these errors, however" +
                "MAY CAUSE UNPREDICTABLE RESULTS! USE WITH CAUTION!!! It is recommended to use the \"import\" feature instead.")
                .setValue(forceLoad);

        root.getNode("storage", "purgeDatabases").setComment("Sets whether to aggressively delete databases that appear corrupted or are no longer used. Default: true\n" +
                "This is meant to keep the database store clean and free of clutter. It also improves load times.\n" +
                "The caveat is that corrupted databases are deleted without warning. This normally isn't an issue, even in server crashes.\n" +
                "However, modifying databases and moving the files around triggers the cleanup.\n" +
                "If force loading is off or simply fails to load the database, it would just be discarded.\n" +
                "Setting this option to false will prevent databases from being deleted.\n" +
                "However, they will still be overwritten if a new database is made with the same name.")
                .setValue(purgeDatabases);

        //--------------------------------------------------------------------------------------------------------------
        try {
            loader.save(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FGConfigManager getInstance() {
        if(instance == null) new FGConfigManager();
        return instance;
    }
}
