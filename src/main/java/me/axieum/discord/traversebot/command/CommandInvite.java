package me.axieum.discord.traversebot.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Invite;

import java.util.Optional;

public class CommandInvite extends Command
{
    public CommandInvite()
    {
        this.name = "invite";
        this.aliases = new String[]{"invitation"};
        this.help = "Fetches a public invitation to invite others to the Discord server";
    }

    @Override
    protected void execute(CommandEvent event)
    {
        event.getGuild().retrieveInvites().queue(invites -> {
            Optional<Invite> invitation = invites.stream()
                                                 .filter(invite -> !invite.isTemporary())
                                                 .findFirst();
            if (invitation.isPresent())
                event.reply(":incoming_envelope: " + invitation.get().getUrl());
            else
                event.reply(":warning: No invites could be found!");
        });
    }
}
