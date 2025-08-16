package me.nd.rankup.listener.bloqueador;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class DesativarMobsNaturais implements Listener {

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void aoSpawnarMob(CreatureSpawnEvent event) {
		SpawnReason reason = event.getSpawnReason();

		// Lista de razões de spawn que devem ser canceladas
		if (reason == SpawnReason.NATURAL || reason == SpawnReason.CHUNK_GEN || reason == SpawnReason.JOCKEY
				|| reason == SpawnReason.MOUNT) {
			event.setCancelled(true);
		}
	}
}