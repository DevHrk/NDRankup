package me.nd.rankup.listener.bloqueador;

import java.util.ArrayList;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;

import me.nd.rankup.Main;
import me.nd.rankup.plugin.SConfig;

import org.bukkit.event.Listener;

public class BloquearAbrirContainers implements Listener {

	@SuppressWarnings("unlikely-arg-type")
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    void aoAbrirContainer(InventoryOpenEvent e) {
		SConfig per = Main.get().getConfig("Permissoes");  
		SConfig m = Main.get().getConfig("Opcoes","OpcoesMundo");
		ArrayList<String> lore2 = new ArrayList<>();
	   	 for (String lorez : m.getStringList("Bloqueadores-Containers.Bloquear-Abrir-Containers.Containers")) {lore2.add(lorez);}		
		if (lore2.contains(e.getInventory().getType())) {
			if (!(e.getPlayer().hasPermission(per.getString("ByPass.AbrirContainers")))) {
				e.setCancelled(true);
				return;
			}
		}
	}
	
}