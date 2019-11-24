package me.axieum.discord.traversebot.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import pl.allegro.finance.tradukisto.ValueConverters;

public class CommandRoll extends Command
{
    public CommandRoll()
    {
        this.name = "roll";
        this.help = "Roll some dice - settle a decision";
        this.arguments = "[count] [faces]";
    }

    @Override
    protected void execute(CommandEvent event)
    {
        int dice = 1;
        int faces = 6;

        String[] args = event.getArgs().split(" ");

        // Did they specify a custom die count (roll multiple dies)?
        if (args.length > 0 && !args[0].isEmpty()) {
            try {
                dice = Integer.parseInt(args[0]);

                if (dice < 1 || dice > 100000) {
                    event.reply(":warning: '"
                                + dice
                                + "' is too "
                                + (dice < 1 ? "few" : "many")
                                + " dice! (allowed 1-100k)");
                    return;
                }
            } catch (NumberFormatException e) {
                event.reply("'" + args[0] + "' is not a valid dice count!");
                return;
            }
        }

        // Did they specify a custom faces count?
        if (args.length > 1 && !args[1].isEmpty()) {
            try {
                faces = Integer.parseInt(args[1]);

                if (faces < 1 || faces > 100000) {
                    event.reply(":warning: '"
                                + faces
                                + "' is too "
                                + (faces < 1 ? "few" : "many")
                                + " faces for a die! (allowed 1-100k)");
                    return;
                }
            } catch (NumberFormatException e) {
                event.reply("'" + args[1] + "' is not a valid faces count!");
                return;
            }
        }

        // Roll the dice!
        int value = 0;
        for (int i = 0; i < dice; i++)
            value += Math.random() * faces + 1;

        // Build the response.
        StringBuilder message = new StringBuilder(":game_die: Rolled **" + value + "**");
        if (dice != 1 || faces != 6) {
            message.append(" *using ");

            if (dice > 1)
                message.append(ValueConverters.ENGLISH_INTEGER.asWords(dice).toLowerCase());
            else
                message.append("a");

            if (faces != 6)
                message.append(dice > 1 ? ", " : " ")
                       .append(ValueConverters.ENGLISH_INTEGER.asWords(faces).toLowerCase())
                       .append("-sided");

            message.append(dice > 1 ? " dice.*" : " die.*");
        }

        event.reply(message.toString());
    }
}
