package me.nd.rankup.listener.bloqueador;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class DesativarQuedaDaAreia implements Listener {

	@EventHandler(ignoreCancelled = true)
	void aoCair(EntityChangeBlockEvent e) {
		if (e.getEntityType() == EntityType.FALLING_BLOCK && e.getTo() == Material.AIR) {
			if (e.getBlock().getType() == Material.SAND || e.getBlock().getType() == Material.GRAVEL) {
				e.setCancelled(true);
				e.getBlock().getState().update(false, false);
			}
		}
	}

}