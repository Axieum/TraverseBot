package me.axieum.discord.traversebot;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;

import javax.annotation.Nullable;

public class Config
{
    private static FileConfig config;

    private static final ConfigSpec spec = new ConfigSpec();

    static {
        spec.define("bot.token", "");
        spec.define("commands.where.enabled", true);
    }

    /**
     * Returns the value in the config under the given key.
     *
     * @return value of the key in the config or null if not found
     */
    @Nullable
    public static Object get(String key)
    {
        return config == null ? null : config.get(key);
    }

    /**
     * Loads and initialises the given config file.
     *
     * @param path config file path
     */
    public static void load(String path)
    {
        // Prepare config instance
        config = FileConfig.builder(path)
                           .sync()
                           .autosave()
                           .autoreload()
                           .writingMode(WritingMode.REPLACE)
                           .build();

        // Load and correct the config
        config.load();
        spec.correct(config);

        System.out.println("Successfully loaded configuration!");
    }
}
