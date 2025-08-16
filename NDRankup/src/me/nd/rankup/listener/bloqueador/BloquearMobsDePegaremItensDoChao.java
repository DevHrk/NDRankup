package me.nd.rankup.listener.bloqueador;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class BloquearMobsDePegaremItensDoChao implements Listener {
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	 void aoNascer(CreatureSpawnEvent e) {
		e.getEntity().setCanPickupItems(false);
	}

}