package no.jckf.dhsupport.bukkit.handler;

import no.jckf.dhsupport.bukkit.DhSupportBukkitPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerPresenceHandler implements Listener
{
    protected DhSupportBukkitPlugin plugin;

    public PlayerPresenceHandler(DhSupportBukkitPlugin plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent join)
    {

    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent quit)
    {
        this.plugin.getDhSupport().closeAndForgetByUuid(quit.getPlayer().getUniqueId());
    }
}
