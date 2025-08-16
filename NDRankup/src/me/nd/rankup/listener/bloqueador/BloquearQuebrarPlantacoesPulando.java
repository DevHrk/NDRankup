package me.nd.rankup.listener.bloqueador;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class BloquearQuebrarPlantacoesPulando implements Listener {
	
	@EventHandler(ignoreCancelled = true)
	 void aoQuebrarPlantacao(EntityChangeBlockEvent e) {
		if (e.getBlock().getType() == Material.SOIL)
			e.setCancelled(true);
	}
	
}