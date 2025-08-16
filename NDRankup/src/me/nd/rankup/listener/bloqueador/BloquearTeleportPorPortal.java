package me.nd.rankup.listener.bloqueador;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class BloquearTeleportPorPortal implements Listener {

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	 static void aoTeleportaPorPortal(PlayerPortalEvent e) {
		if (!(e.getPlayer().hasPermission("portal.bypass"))) {

			if (e.getCause() == TeleportCause.NETHER_PORTAL) {
					e.setCancelled(true);
					return;
			}

			if (e.getCause() == TeleportCause.END_PORTAL) {
					e.setCancelled(true);
					return;
			}
		}
	}
	
}