package me.axieum.discord.traversebot.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.TimeUnit;

public class PurgeCommand extends Command
{
    public PurgeCommand()
    {
        this.name = "purge";
        this.aliases = new String[]{"clear"};
        this.arguments = "<count>";
        this.help = "Deletes a number of last messages from the channel";
    }

    @Override
    protected void execute(CommandEvent event)
    {
        int count; // messages to delete

        // Validation
        String[] args = event.getArgs().trim().split(" ");

        if (args.length == 1 && args[0].length() > 0) {
            try {
                count = Integer.parseInt(args[0]);
                if (count < 1 || count > 100) {
                    event.reply(":warning: You can only purge 1 through to 100 recent messages!");
                    return;
                }
            } catch (NumberFormatException e) {
                event.reply(":warning: '" + args[0] + "' is not a valid count!");
                return;
            }
        } else {
            event.reply(":warning: Missing count!");
            return;
        }

        // Handle deletions
        final TextChannel channel = event.getTextChannel();
        channel.getHistoryBefore(event.getMessage(), count)
               .queue(history -> {
                   // Delete the command itself
                   event.getMessage().delete().queue();
                   // Delete the history
                   channel.deleteMessages(history.getRetrievedHistory()).queue(success -> {
                       // Send self-deleting success message
                       channel.sendMessage("Deleted **" + history.size() + "** recent messages! :ok_hand:")
                              .queue(message -> message.delete().queueAfter(3, TimeUnit.SECONDS));
                   }, failure -> event.reply(":warning: Unable to purge messages!"));
               });
    }
}
