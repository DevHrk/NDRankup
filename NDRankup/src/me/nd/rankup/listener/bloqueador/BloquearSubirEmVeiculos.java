package me.nd.rankup.listener.bloqueador;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class BloquearSubirEmVeiculos implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	 void aoEntrarNoVeiculo(VehicleEnterEvent e) {
		if (e.getEntered() instanceof Player) {
			Player p = (Player) e.getEntered();
			if (!(p.hasPermission("veiculo.bypass"))) {
				e.setCancelled(true);
			}
		}
	}
	
}