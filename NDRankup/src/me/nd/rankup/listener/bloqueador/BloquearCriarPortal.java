package me.nd.rankup.listener.bloqueador;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;

public class BloquearCriarPortal implements Listener {

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	 void aoCriarPortal(PortalCreateEvent e) {
		e.setCancelled(true);
	}
	
}