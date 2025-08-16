package me.nd.rankup.listener.bloqueador;

import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import me.nd.rankup.api.LocationsAPI;

public class BloquearSubirNoTetoNether implements Listener {
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	 void aoTeleportar(PlayerTeleportEvent e) {
		if (e.getTo() != null && e.getTo().getWorld().getEnvironment() == Environment.NETHER && e.getTo().getY() > 124.0D) {
			e.getPlayer().teleport(LocationsAPI.spawn, TeleportCause.PLUGIN);
			e.setCancelled(true);
		}
	}
	
}
