package me.nd.rankup.listener.bloqueador;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class DesativarMensagemDeEntrada implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	 void aoEntrar(PlayerJoinEvent e) {
		e.setJoinMessage(null);
	}
	
}