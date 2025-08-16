package me.nd.rankup.listener.bloqueador;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public class ChuvaListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void aoComecarChuva(WeatherChangeEvent e) {
		// Se a mudança de clima é para chuva, cancela o evento
		if (e.toWeatherState()) {
			e.setCancelled(true);
		}

	}

}
