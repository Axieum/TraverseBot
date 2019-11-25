package me.axieum.discord.traversebot.util;

import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.Arrays;

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
        final OSProcess[] processes = SYSTEM_INFO.getOperatingSystem().getProcesses(32,
                                                                                    OperatingSystem.ProcessSort.MEMORY);
        return Arrays.stream(processes)
                     .filter(process -> process.getName().toLowerCase().contains(name.toLowerCase()))
                     .filter(process -> Arrays.stream(keywords)
                                              .map(String::toLowerCase)
                                              .anyMatch(process.getCommandLine().toLowerCase()::contains))
                     .findFirst()
                     .orElse(null);
    }
}
