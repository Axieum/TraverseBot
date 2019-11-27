package me.axieum.discord.traversebot.util;

import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SystemUtils
{
    private static final SystemInfo SYSTEM_INFO = new SystemInfo();

    /**
     * Returns a System Info instance.
     *
     * @return system info instance
     */
    public static SystemInfo getSystemInfo()
    {
        return SYSTEM_INFO;
    }

    /**
     * Finds an operating system process instance that matches any of the given
     * keywords.
     *
     * @param name     keyword to look for in process name
     * @param keywords keyword(s) to look for in process command line
     * @return a process matching any keyword or null if none found
     */
    public static OSProcess getOSProcess(String name, String... keywords)
    {
        final OperatingSystem os = SYSTEM_INFO.getOperatingSystem();

        // Transform inputs to lowercase
        final String target = name.toLowerCase();
        final List<String> cli = Arrays.stream(keywords).map(String::toLowerCase).collect(Collectors.toList());

        // First, fetch all processes quickly (ignoring "slow" fields/information)
        return Arrays.stream(os.getProcesses(0, OperatingSystem.ProcessSort.MEMORY, false))
                     // Filter on processes that match the given name
                     .filter(process -> process.getName().toLowerCase().contains(target))
                     // Fetch the process detailed fields/information
                     .peek(OSProcess::updateAttributes)
                     // Filter on processes that match one of the cli keywords
                     .filter(process -> cli.stream().anyMatch(process.getCommandLine().toLowerCase()::contains))
                     // Return the first process that matches, else null
                     .findFirst()
                     .orElse(null);
    }
}
