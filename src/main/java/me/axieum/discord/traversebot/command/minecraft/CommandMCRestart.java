package me.axieum.discord.traversebot.command.minecraft;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.axieum.discord.traversebot.Config;
import me.axieum.discord.traversebot.util.SystemUtils;
import oshi.software.os.OSProcess;
import oshi.util.Util;

public class CommandMCRestart extends Command
{
    public CommandMCRestart()
    {
        this.name = "restart";
        this.aliases = new String[]{"reboot"};
        this.arguments = "[name]";
        this.help = "Restarts a Minecraft server";
        this.hidden = true;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        // Let them know we're working on it
        event.getChannel().sendTyping().queue();

        // Fetch server name to match on
        String name = event.getArgs().isEmpty() ? Config.getConfig().get("minecraft.selected") : event.getArgs();
        if (name == null || name.isEmpty()) {
            event.reply(":warning: No default Minecraft server specified!");
            return;
        }

        // Is the process already stopped?
        final OSProcess process = SystemUtils.getOSProcess("java", name);

        if (process == null) {
            new CommandMCStart().execute(event);
            return;
        }

        // Wait for the process to terminate, then start it.
        new Thread(() -> {
            // Check every 3 seconds, for 60secs
            int tries = 0;
            while (tries++ < 20 && process.updateAttributes(false)) {
                event.getChannel().sendTyping().queue(); // Keep user informed something is happening
                Util.sleep(3000);
            }

            // If the process stopped, off-load to start command, else report failure
            if (tries <= 20)
                new CommandMCStart().execute(event);
            else
                event.reply(":warning: The '**" + name + "**' Minecraft server didn't stop in time!");
        }).start();
    }
}
