package me.nd.rankup.listener.bloqueador;

import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DesativarPropagacaoDoFogo implements Listener {

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	void aoPegarFogo(BlockBurnEvent e) {
		e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	void aoEspalharFogo(BlockIgniteEvent e) {
		if (e.getCause() == IgniteCause.LAVA || e.getCause() == IgniteCause.SPREAD) {
			e.setCancelled(true);
		}
	}

}