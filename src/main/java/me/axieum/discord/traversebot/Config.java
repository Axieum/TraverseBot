package me.axieum.discord.traversebot;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.Objects;

public class Config
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static FileConfig config;

    private static final ConfigSpec spec = new ConfigSpec();

    static {
        // General
        spec.define("token", "");
        spec.define("ip", "");
        spec.define("command.prefix", "!");
        spec.define("command.owner_id", "");

        // Minecraft
        spec.define("minecraft.directory", "/home/minecraft/servers");
        spec.define("minecraft.selected", "Minecraft");

        spec.define("minecraft.news.channel", -1L);
        spec.define("minecraft.news.frequency", 60);
        spec.define("minecraft.news.color", 1484079);
        spec.define("minecraft.news.tags", new String[]{"News", "Deep Dives", "Minecraft Builds"});
        spec.define("minecraft.news.extract_length", 300, i -> (int) i < MessageEmbed.TEXT_MAX_LENGTH);
        spec.define("minecraft.news.author.name", "Minecraft News");
        spec.define("minecraft.news.author.url", "");
        spec.define("minecraft.news.author.icon", "");
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

        LOGGER.info("Successfully loaded configuration!");
    }
}
