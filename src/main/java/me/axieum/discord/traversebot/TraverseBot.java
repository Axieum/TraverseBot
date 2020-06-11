package me.axieum.discord.traversebot;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import me.axieum.discord.traversebot.command.*;
import me.axieum.discord.traversebot.command.minecraft.*;
import me.axieum.discord.traversebot.misc.minecraft.MinecraftNews;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

public class TraverseBot extends ListenerAdapter
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static JDA api;
    private static CommandClient commands;

    /**
     * TraverseBot main entry point.
     *
     * @param args system arguments
     */
    public static void main(String[] args)
    {
        LOGGER.info("Traverse Bot starting...");

        // Initialise configuration
        Config.load("./config.json");

        // Prepare command client
        final String cmdPrefix = Config.getConfig().getOrElse("command.prefix", "!");
        final String ownerId = Config.getConfig().getOrElse("command.owner_id", "");
        try {
            commands = new CommandClientBuilder().setPrefix(cmdPrefix)
                                                 .setOwnerId(ownerId)
                                                 .setActivity(null)
                                                 .addCommands(new WhoAmICommand(),
                                                              new RollCommand(),
                                                              new InviteCommand(),
                                                              new PurgeCommand(),
                                                              new SystemCommand(),
                                                              new IPCommand(),
                                                              new MinecraftCommand(),
                                                              new MCStartCommand(),
                                                              new MCRestartCommand(),
                                                              new MCForceStopCommand(),
                                                              new MCSelectCommand())
                                                 .build();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Could not prepare commands: {}", e.getMessage());
        }

        // Prepare JDA client
        final String token = Config.getConfig().getOrElse("token", "");
        try {
            api = JDABuilder.createDefault(token)
                            .addEventListeners(new TraverseBot(), commands)
                            .build()
                            .awaitReady();
        } catch (LoginException | InterruptedException e) {
            LOGGER.error("Unable to login to Discord Bot: {}", e.getMessage());
        }
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event)
    {
        LOGGER.info("Logged into Discord Bot @{}", event.getJDA().getSelfUser().getAsTag());

        // Kick off the Minecraft News feed watcher
        MinecraftNews.init(event.getJDA());
    }

    /**
     * Returns the underlying Discord JDA API client.
     *
     * @return JDA client
     */
    public static JDA getDiscord()
    {
        return api;
    }
}
