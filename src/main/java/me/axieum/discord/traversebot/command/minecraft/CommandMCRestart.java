package me.axieum.discord.traversebot.command.minecraft;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class CommandMCRestart extends Command
{
    CommandMCRestart()
    {
        this.name = "restart";
        this.aliases = new String[]{"reboot"};
        this.help = "Restarts the Minecraft server";
    }

    @Override
    protected void execute(CommandEvent event)
    {
        event.reply("You've reached the `restart` command.");
    }
}
