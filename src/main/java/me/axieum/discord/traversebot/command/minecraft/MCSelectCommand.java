package me.axieum.discord.traversebot.command.minecraft;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.axieum.discord.traversebot.Config;

public class MCSelectCommand extends Command
{
    public MCSelectCommand()
    {
        this.name = "select";
        this.aliases = new String[]{"switch"};
        this.arguments = "[name]";
        this.help = "Sets or prints the default Minecraft server to be run";
        this.hidden = true;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        final String selection = event.getArgs();
        if (selection != null && !selection.isEmpty()) {
            // Update the config with the new selection
            Config.getConfig().set("minecraft.selected", selection);
            event.reply(":bulb: Selected '**" + selection + "**' as the default Minecraft server!");
        } else {
            String selected = Config.getConfig().get("minecraft.selected");
            if (selected != null && !selected.isEmpty())
                event.reply(":bulb: '**" + selected + "**' is currently selected as the default Minecraft server.");
            else
                event.reply(":bulb: There is no default Minecraft server set.");
        }
    }
}
