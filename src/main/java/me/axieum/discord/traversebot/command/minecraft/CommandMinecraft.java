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
        this.children = new Command[]{new CommandMCStart(),
                                      new CommandMCRestart(),
                                      new CommandMCForceStop(),
                                      new CommandMCSelect()};
    }

    @Override
    protected void execute(CommandEvent event)
    {
        // If an invalid sub-command made it here, simply ignore it
        // NB: Useful if another bot is made to handle that command
        if (event.getArgs().isEmpty())
            event.reply(":information_source: `!" + this.name + " " + this.arguments + "` - " + this.help);
    }
}
