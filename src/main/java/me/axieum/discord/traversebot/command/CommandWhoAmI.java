package me.axieum.discord.traversebot.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class CommandWhoAmI extends Command
{
    public CommandWhoAmI()
    {
        this.name = "whereami";
        this.aliases = new String[]{"whoami"};
        this.help = "Tells you who and where you are";
    }

    @Override
    protected void execute(CommandEvent event)
    {
        final String response = String.format("**%s**, you are in %s",
                                              event.getAuthor().getAsMention(),
                                              event.getTextChannel().getAsMention());
        event.reply(response);
    }
}
