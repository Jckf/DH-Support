package no.jckf.dhsupport.bukkit.commands;

import no.jckf.dhsupport.bukkit.DhSupportBukkitPlugin;
import no.jckf.dhsupport.core.dataobject.SectionPosition;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class DhsCommand implements CommandExecutor
{
    protected DhSupportBukkitPlugin plugin;

    public DhsCommand(DhSupportBukkitPlugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] arguments)
    {
        if (arguments.length == 0) {
            sender.sendMessage("Missing sub-command.");
            return false;
        }

        switch (arguments[0].toLowerCase()) {
            case "generate":
                return this.subGenerate(sender, Arrays.copyOfRange(arguments, 1, arguments.length));
        }

        sender.sendMessage("Unknown sub-command.");

        return false;
    }

    public boolean subGenerate(@NotNull CommandSender sender, @NotNull String[] arguments)
    {
        if (arguments.length < 2) {
            sender.sendMessage("Too few arguments.");
            return false;
        }

        int sectionX = Integer.parseInt(arguments[0]);
        int sectionZ = Integer.parseInt(arguments[1]);

        World world;

        if (arguments.length == 2) {
            if (sender instanceof Player) {
                world = ((Player) sender).getWorld();
            } else {
                sender.sendMessage("World must be specified.");
                return false;
            }
        } else {
            world = this.plugin.getServer().getWorld(arguments[2]);
        }

        if (world == null) {
            sender.sendMessage("World not found.");
            return false;
        }

        sender.sendMessage("Queuing LOD generation for section " + sectionX + " x " + sectionZ + "...");

        SectionPosition position = new SectionPosition();
        position.setDetailLevel(6);
        position.setX(sectionX);
        position.setZ(sectionZ);

        this.plugin.getDhSupport().getLodRepository().deleteLod(world.getUID(), sectionX, sectionZ);

        this.plugin.getDhSupport().getLodData(world.getUID(), position)
            .thenAccept((lod) -> {
                sender.sendMessage("LOD generation for section " + sectionX + " x " + sectionZ + " completed. Produced " + Math.round((float) lod.length / 1000) + " kB.");
            });

        return true;
    }
}
