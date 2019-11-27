package me.axieum.discord.traversebot;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;

import java.net.URL;
import java.util.Objects;

public class Config
{
    private static FileConfig config;

    private static final ConfigSpec spec = new ConfigSpec();

    static {
        spec.define("token", "");
        spec.define("ip", "");
        spec.define("command.prefix", "!");
        spec.define("command.owner_id", "");
        spec.define("minecraft.directory", "/home/minecraft/servers");
        spec.define("minecraft.selected", "Minecraft");
    }

    /**
     * Returns the config instance.
     *
     * @return config instance
     */
    public static FileConfig getConfig()
    {
        return config;
    }

    /**
     * Loads and initialises the given config file.
     *
     * @param path config file path
     */
    public static void load(String path)
    {
        // Fetch default config resource
        URL defaultConfig = Config.class.getClassLoader().getResource("config.json");
        Objects.requireNonNull(defaultConfig);

        // Prepare config instance
        config = FileConfig.builder(path)
                           .defaultData(defaultConfig)
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
