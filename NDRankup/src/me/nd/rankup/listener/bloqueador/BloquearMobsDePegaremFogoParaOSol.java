package me.nd.rankup.listener.bloqueador;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;

public class BloquearMobsDePegaremFogoParaOSol implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	 void aoPegarFogo(EntityCombustEvent e) {
		if (!(e instanceof EntityCombustByEntityEvent) && !(e instanceof EntityCombustByBlockEvent)) {
			e.setCancelled(true);
		}
	}
	
}