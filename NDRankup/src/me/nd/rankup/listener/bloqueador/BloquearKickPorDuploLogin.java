package me.nd.rankup.listener.bloqueador;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

public class BloquearKickPorDuploLogin implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	 void aoTentarEntrar(AsyncPlayerPreLoginEvent e) {
		Player p = Bukkit.getPlayerExact(e.getName());
		if (p != null) {
			e.setLoginResult(Result.KICK_OTHER);
		}
	}
	
}