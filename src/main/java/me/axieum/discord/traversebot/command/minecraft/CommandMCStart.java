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
    public CommandMCStart()
    {
        this.name = "start";
        this.aliases = new String[]{"boot"};
        this.arguments = "[name]";
        this.help = "Starts a Minecraft server";
        this.hidden = true;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        // Let them know we're working on it
        event.getChannel().sendTyping().queue();

        // Fetch server name and hence script name to start
        String name = event.getArgs().isEmpty() ? Config.getConfig().get("minecraft.selected") : event.getArgs();
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
            final File script;

            // Determine shell and hence script extension
            OperatingSystem os = SystemUtils.getSystemInfo().getOperatingSystem();
            if (os instanceof WindowsOperatingSystem) {
                process = new ProcessBuilder("cmd", "/c");
                script = new File(directory, name + ".bat");
            } else if (os instanceof LinuxOperatingSystem) {
                process = new ProcessBuilder("/bin/bash");
                script = new File(directory, name + ".sh");
            } else {
                event.reply(":warning: Unable to start: '**" + os.getFamily() + "**' OS is not supported!");
                return;
            }

            // Fetch and assert server script exists
            if (!script.exists() || !script.isFile()) {
                System.out.println("Script file: '" + script.getAbsolutePath() + "' does not exist!");
                event.reply(":warning: Unable to find '**" + name + "**' Minecraft server!");
                return;
            }

            // Set process directory, script to execute and ignore stream buffer
            process.directory(directory);
            if (os instanceof WindowsOperatingSystem) {
                // Windows is strange, and requires output redirect inline with script
                process.command().add('"' + script.getAbsolutePath() + "\" > NUL");
            } else {
                process.command().add(script.getAbsolutePath());
                process.command().add("> /dev/null");
            }
//            process.command().add(script.getAbsolutePath());
//            process.command().add(os instanceof WindowsOperatingSystem ? "> NUL" : "> /dev/null");

            System.out.printf("Starting process: '%s' from directory '%s'\n",
                              String.join(" ", process.command()),
                              directory.getAbsolutePath());

            // Start the process
//            process.redirectOutput(ProcessBuilder.Redirect.INHERIT);
//            process.redirectError(ProcessBuilder.Redirect.INHERIT);
            process.start();
            event.reply(":bulb: Starting '**" + name + "**' Minecraft server!");
        } catch (Exception e) {
            e.printStackTrace();
            event.reply(":warning: Unable to start '**" + name + "**'. " + e.getMessage());
        }
    }
}
