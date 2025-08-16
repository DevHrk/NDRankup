package me.nd.rankup.listener.bloqueador;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class DesativarDanoDoCacto implements Listener {

	@EventHandler(ignoreCancelled = true)
	 void aoSofrerDanoParaCacto(EntityDamageByBlockEvent e) {
		if (e.getCause() == DamageCause.CONTACT) {
			e.setCancelled(true);
		}
	}

}