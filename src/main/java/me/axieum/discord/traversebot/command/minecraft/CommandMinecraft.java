package me.axieum.discord.traversebot.command.minecraft;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class CommandMinecraft extends Command
{
    public CommandMinecraft()
    {
        this.name = "minecraft";
        this.aliases = new String[]{"mc"};
        this.arguments = "<start|restart|select>";
        this.help = "Manages the Minecraft server";
        this.children = new Command[]{new CommandMCStart(), new CommandMCRestart(), new CommandMCSelect()};
    }

    @Override
    protected void execute(CommandEvent event)
    {
        event.reply("You've reached the `minecraft` command.");
    }
}
