package me.axieum.discord.traversebot.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.axieum.discord.traversebot.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class CommandIP extends Command
{
    public CommandIP()
    {
        this.name = "ip";
        this.help = "Provides the server's IP address";
    }

    @Override
    protected void execute(CommandEvent event)
    {
        // Fetch ip from config
        String ip = Config.getConfig().get("ip");

        // If no ip configured, then fetch public IP
        if (ip == null || ip.isEmpty()) {
            try {
                event.getChannel().sendTyping().queue(); // Let them know we're fetching the IP
                ip = new BufferedReader(new InputStreamReader(new URL("http://checkip.amazonaws.com").openStream()))
                        .readLine();
            } catch (IOException ignored) {}
        }

        // Return it
        if (ip != null && !ip.isEmpty())
            event.reply(":zap: **" + ip + "**");
        else
            event.reply(":warning: Something went wrong while fetching the server's IP!");
    }
}
