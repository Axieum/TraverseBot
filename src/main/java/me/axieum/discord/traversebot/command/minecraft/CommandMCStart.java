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
        this.help = "Starts a Minecraft server";
    }

    @Override
    protected void execute(CommandEvent event)
    {
        // Let them know we're working on it
        event.getChannel().sendTyping().queue();

        // Fetch server name and hence script name to start
        String name = event.getArgs();
        if (name == null || name.isEmpty())
            name = Config.getConfig().get("minecraft.selected");

        if (name == null || name.isEmpty()) {
            event.reply(":warning: No default Minecraft server specified!");
            return;
        }

        // Is the server already online?
        if (SystemUtils.getOSProcess("java", name) != null) {
            event.reply(":warning: The Minecraft server '**" + name + "**' is already online!");
            return;
        }

        // Fetch server directory
        String path = Config.getConfig().get("minecraft.directory");
        if (path == null || path.isEmpty()) {
            event.reply(":warning: Minecraft servers directory not configured!");
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
            final String extension, cli;

            // Determine shell and hence script extension
            OperatingSystem os = SystemUtils.getSystemInfo().getOperatingSystem();
            if (os instanceof WindowsOperatingSystem) {
                process = new ProcessBuilder("cmd", "/c");
                extension = ".bat";
                cli = "\"\"%s\" > NUL\"";
            } else if (os instanceof LinuxOperatingSystem) {
                process = new ProcessBuilder("/bin/bash");
                extension = ".sh";
                cli = "\"%s\" > /dev/null";
            } else {
                throw new Exception(":warning: Unable to start: '**" + os.getFamily() + "**' OS is not supported!");
            }

            // Fetch and assert server script exists
            File startScript = new File(directory, name + extension);
            if (!startScript.exists() || !startScript.isFile()) {
                event.reply(":warning: Unable to find '**" + name + "**' Minecraft server!");
                return;
            }

            // Set process directory, script to execute and ignore stream buffer
            process.directory(directory);
            process.command().add(String.format(cli, startScript.getAbsolutePath()));
//            process.command().add('"' + startScript.getAbsolutePath() + '"');
//            process.command().add(os instanceof WindowsOperatingSystem ? "> NUL" : "> /dev/null");

            System.out.printf("Starting process: '%s' from directory '%s'\n",
                              String.join(" ", process.command()),
                              directory.getAbsolutePath());

            // Start the process
            process.start();
            event.reply(":bulb: Starting '**" + name + "**' Minecraft server!");
        } catch (Exception e) {
            e.printStackTrace();
            event.reply(":warning: Unable to start '**" + name + "**'. " + e.getMessage());
        }
    }
}
