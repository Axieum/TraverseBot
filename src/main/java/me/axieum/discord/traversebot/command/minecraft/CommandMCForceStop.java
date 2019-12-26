package me.axieum.discord.traversebot.command.minecraft;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.axieum.discord.traversebot.Config;
import me.axieum.discord.traversebot.util.SystemUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import oshi.software.os.OSProcess;
import oshi.software.os.windows.WindowsOperatingSystem;
import oshi.util.Util;

import java.io.IOException;

public class CommandMCForceStop extends Command
{
    public CommandMCForceStop()
    {
        this.name = "forcestop";
        this.arguments = "[name]";
        this.help = "Force stops a Minecraft server";
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
            event.reply(":warning: The '**" + name + "**' Minecraft server is already offline!");
            return;
        }

        // Attempt to terminate the process.
        final int pid = process.getProcessID();
        final String killCmd;

        if (SystemUtils.getSystemInfo().getOperatingSystem() instanceof WindowsOperatingSystem)
            killCmd = String.format("taskkill /F /PID %d", pid);
        else
            killCmd = String.format("kill -9 %d", pid);

        System.out.printf("Terminating process with PID '%d' via: '%s'\n", pid, killCmd);

        new Thread(() -> {
            // Try to terminate every 3 seconds, for 60secs
            int tries = 0;
            while (tries++ < 20 && process.updateAttributes(false)) {
                try {
                    event.getChannel().sendTyping().queue(); // Keep user informed something is happening
                    Runtime.getRuntime().exec(killCmd);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Util.sleep(3000);
                }
            }

            // If the process stopped, report successful terminate, else report failure
            if (tries <= 20)
                event.reply(String.format(":no_entry: Force stopped the '**%s**' Minecraft server (process uptime %s)",
                                          name,
                                          DurationFormatUtils.formatDurationWords(process.getUpTime(), true, true)));
            else
                event.reply(":warning: Could not force stop the '**" + name + "**' Minecraft server!");
        }).start();
    }
}
