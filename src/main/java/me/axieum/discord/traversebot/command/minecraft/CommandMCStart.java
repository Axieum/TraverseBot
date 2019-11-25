package me.axieum.discord.traversebot.command.minecraft;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.axieum.discord.traversebot.Config;
import me.axieum.discord.traversebot.util.SystemUtils;
import oshi.software.os.OperatingSystem;
import oshi.software.os.linux.LinuxOperatingSystem;
import oshi.software.os.windows.WindowsOperatingSystem;

import java.io.File;

public class CommandMCStart extends Command
{
    CommandMCStart()
    {
        this.name = "start";
        this.aliases = new String[]{"boot"};
        this.arguments = "[name]";
        this.help = "Starts the/a Minecraft server";
    }

    @Override
    protected void execute(CommandEvent event)
    {
        // Fetch server name to start
        String toStart = event.getArgs();
        if (toStart == null || toStart.isEmpty())
            toStart = Config.getConfig().get("minecraft.selected");

        if (toStart == null || toStart.isEmpty()) {
            event.reply(":warning: No server specified!");
            return;
        }

        // Is the server already online?
        if (SystemUtils.getOSProcess("java", toStart) != null) {
            event.reply(":warning: The Minecraft server '**" + toStart + "**' is already online!");
            return;
        }

        // Fetch server directory
        String path = Config.getConfig().get("minecraft.directory");
        if (path == null || path.isEmpty()) {
            event.reply(":warning: Server directory not configured!");
            return;
        }

        File directory = new File(path);
        if (!directory.exists() || !directory.isDirectory()) {
            event.reply(":warning: Minecraft servers directory not configured!");
            return;
        }

        // Build the process invocation
        try {
            final ProcessBuilder process;
            final String extension;

            // Determine shell and hence script extension
            OperatingSystem os = SystemUtils.getSystemInfo().getOperatingSystem();
            if (os instanceof WindowsOperatingSystem) {
                process = new ProcessBuilder("cmd", "/c");
                extension = ".bat";
            } else if (os instanceof LinuxOperatingSystem) {
                process = new ProcessBuilder("/bin/bash");
                extension = ".sh";
            } else {
                throw new Exception(":warning: Unable to start: '**" + os.getFamily() + "**' OS is not supported!");
            }

            // Fetch and assert server script exists
            File startScript = new File(directory, toStart + extension);
            if (!startScript.exists() || !startScript.isFile()) {
                event.reply(":warning: Unable to find '**" + toStart + "**' Minecraft server!");
                return;
            }

            // Set process directory, script to execute and ignore stream buffer
            process.directory(directory);
            process.command().add(startScript.getAbsolutePath());
            process.command().add(os instanceof WindowsOperatingSystem ? "> NUL" : "> /dev/null");

            // Start the process
            process.start();
            event.reply(":bulb: Starting '**" + toStart + "**' Minecraft server!");
        } catch (Exception e) {
            e.printStackTrace();
            event.reply(":warning: Unable to start '**" + toStart + "**'. " + e.getMessage());
        }
    }
}
