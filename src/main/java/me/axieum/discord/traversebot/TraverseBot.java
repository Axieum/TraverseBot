package me.axieum.discord.traversebot;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import me.axieum.discord.traversebot.command.CommandInvite;
import me.axieum.discord.traversebot.command.CommandPurge;
import me.axieum.discord.traversebot.command.CommandRoll;
import me.axieum.discord.traversebot.command.CommandWhoAmI;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

public class TraverseBot extends ListenerAdapter
{
    private static JDA api;
    private static CommandClient commands;

    /**
     * TraverseBot main entry point.
     *
     * @param args system arguments
     */
    public static void main(String[] args)
    {
        System.out.println("Traverse Bot starting...");

        // Initialise configuration
        Config.load("./config.json");

        // Prepare command client
        final String cmdPrefix = Config.getConfig().getOrElse("command.prefix", "!");
        final String ownerId = Config.getConfig().getOrElse("command.owner_id", "");
        try {
            commands = new CommandClientBuilder().setPrefix(cmdPrefix)
                                                 .setOwnerId(ownerId)
                                                 .addCommands(new CommandWhoAmI(),
                                                              new CommandRoll(),
                                                              new CommandInvite(),
                                                              new CommandPurge())
                                                 .build();
        } catch (IllegalArgumentException e) {
            System.out.println("Could not prepare commands: " + e.getMessage());
        }

        // Prepare JDA client
        final String token = Config.getConfig().getOrElse("token", "");
        try {
            api = new JDABuilder(AccountType.BOT).setToken(token)
                                                 .addEventListeners(new TraverseBot(), commands)
                                                 .build()
                                                 .awaitReady();
        } catch (LoginException | InterruptedException e) {
            System.out.println("Unable to login to Discord Bot: " + e.getMessage());
        }
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event)
    {
        System.out.println("Logged into Discord Bot @" + event.getJDA().getSelfUser().getAsTag());
    }

    @Override
    public void onDisconnect(@Nonnull DisconnectEvent event)
    {
        System.out.println("Logged out of Discord Bot @" + event.getJDA().getSelfUser().getAsTag());
    }
}
