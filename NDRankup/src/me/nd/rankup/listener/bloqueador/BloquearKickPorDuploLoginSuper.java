package me.nd.rankup.listener.bloqueador;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

public class BloquearKickPorDuploLoginSuper implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	 void aoTentarEntrar(PlayerLoginEvent e) {
		Player p = Bukkit.getPlayerExact(e.getPlayer().getName());
		if (p != null) {
			e.setResult(Result.KICK_OTHER);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void aoSerKickado(PlayerKickEvent e) {
		if (e.getReason().contains("You logged in from another location")) {
			e.setCancelled(true);
		}
	}
	
}