package me.nd.rankup.listener.bloqueador;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DesativarMensagemDeSaida implements Listener {

	@EventHandler(priority = EventPriority.HIGH)
	 void aoSair(PlayerQuitEvent e) {
		e.setQuitMessage(null);
	}

	@EventHandler(priority = EventPriority.HIGH)
	 void aoSerKickado(PlayerKickEvent e) {
		e.setLeaveMessage(null);
	}
	
}