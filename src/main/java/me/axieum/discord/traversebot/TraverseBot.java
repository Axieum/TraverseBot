package me.axieum.discord.traversebot;

import net.dv8tion.jda.api.JDA;

public class TraverseBot
{
    private static JDA api;

    /**
     * TraverseBot main entry point.
     *
     * @param args system arguments
     */
    public static void main(String[] args)
    {
        System.out.println("TraverseBot starting...");

        // Initialise configuration
        Config.load("./config.json");
    }
}
