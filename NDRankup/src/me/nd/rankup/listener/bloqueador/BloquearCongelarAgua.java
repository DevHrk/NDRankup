package me.nd.rankup.listener.bloqueador;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;

public class BloquearCongelarAgua implements Listener {

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	void aoCongelar(BlockFormEvent e) {
		if (e.getBlock().getType() == Material.WATER || e.getBlock().getType() == Material.STATIONARY_WATER) {
			e.setCancelled(true);
		}
	}

}