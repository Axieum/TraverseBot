package me.axieum.discord.traversebot.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.axieum.discord.traversebot.util.SystemUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OSFileStore;
import oshi.util.FormatUtil;
import oshi.util.Util;

import java.util.stream.DoubleStream;

public class CommandSystem extends Command
{
    public CommandSystem()
    {
        this.name = "system";
        this.aliases = new String[]{"sys"};
        this.help = "Provides information about the system";
    }

    @Override
    protected void execute(CommandEvent event)
    {
        // Let them know we're working on it
        event.getChannel().sendTyping().queue();

        final SystemInfo system = SystemUtils.getSystemInfo();

        // Prepare Discord embed message
        final EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("System Information");

        // CPU
        final CentralProcessor processor = system.getHardware().getProcessor();

        final long[] cpuPrevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000); // sleep to give time to calculate cpu load

        final double loadCpu = processor.getSystemCpuLoadBetweenTicks(cpuPrevTicks);
        final double tempCpu = system.getHardware().getSensors().getCpuTemperature();
        final long uptime = system.getOperatingSystem().getSystemUptime();

        embed.addField("Uptime", FormatUtil.formatElapsedSecs(uptime), true);
        embed.addField("CPU Load", loadCpu == 0 ? "N/A" : String.format("%.1f%%", loadCpu * 100), true);
        embed.addField("Temperature", tempCpu == 0 ? "N/A" : String.format("%.1f\u00b0C", tempCpu), true);

        // Memory
        final GlobalMemory memory = system.getHardware().getMemory();
        final long totalMem = memory.getTotal();
        final long availMem = memory.getAvailable();
        final long usedMem = totalMem - availMem;
        final double percMem = usedMem / (double) totalMem;
        final long totalSwp = memory.getVirtualMemory().getSwapTotal();
        final long usedSwp = memory.getVirtualMemory().getSwapUsed();
//        final long availSwp = totalSwp - usedSwp;
        final double percSwp = usedSwp / (double) totalSwp;

        embed.addField("Memory",
                       String.format("%s / %s (%s)",
                                     FormatUtil.formatBytes(usedMem),
                                     FormatUtil.formatBytes(totalMem),
                                     String.format("%.0f%%", percMem * 100)),
                       true);

        embed.addField("Swap",
                       String.format("%s / %s (%s)",
                                     FormatUtil.formatBytes(usedSwp),
                                     FormatUtil.formatBytes(totalSwp),
                                     String.format("%.0f%%", percSwp * 100)),
                       true);

        // Disk (only first found)
        final OSFileStore filesystem = system.getOperatingSystem().getFileSystem().getFileStores()[0];
        final long totalDsk = filesystem.getTotalSpace();
        final long availDsk = filesystem.getUsableSpace();
        final long usedDsk = totalDsk - availDsk;
        final double percDsk = usedDsk / (double) totalDsk;

        embed.addField("Filesystem",
                       String.format("%s / %s (%s)",
                                     FormatUtil.formatBytes(usedDsk),
                                     FormatUtil.formatBytes(totalDsk),
                                     String.format("%.0f%%", percDsk * 100)),
                       true);

        // Set an appropriate colour
        embed.setColor(getColor(loadCpu, percMem, percSwp, percDsk));

        // Send embed as a reply
        event.reply(embed.build());
    }

    /**
     * Returns a colour between from red through to green depending on the
     * given system usage percentages. I.e. red means something has a high
     * usage.
     *
     * @param cpu  cpu load percentage
     * @param ram  ram usage percentage
     * @param swap swap usage percentage
     * @param disk disk usage percentage
     * @return colour between red (high usage) through green (low usage)
     */
    private static int getColor(double cpu, double ram, double swap, double disk)
    {
        // Disk usage should have less weighting (not as important)
        disk = disk * 0.76;

        // Find highest load
        final double max = DoubleStream.of(cpu, ram, swap, disk).max().orElse(0);

        if (max <= 0.25)
            return 65280; // Green
        else if (max <= 0.5)
            return 16776960; // Yellow
        else if (max <= 0.75)
            return 16744448; // Orange
        else
            return 16711680; // Red
    }
}
