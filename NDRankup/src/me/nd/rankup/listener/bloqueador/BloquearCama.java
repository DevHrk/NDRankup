package me.nd.rankup.listener.bloqueador;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

import me.nd.rankup.Main;
import me.nd.rankup.plugin.SConfig;

public class BloquearCama implements Listener {

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    void aoEntrarNaCama(PlayerBedEnterEvent e) {
		SConfig per = Main.get().getConfig("Permissoes");  
		if (!(e.getPlayer().hasPermission(per.getString("ByPass.DeitaNaCama")))) {
			e.setCancelled(true);
		}
	}

}
