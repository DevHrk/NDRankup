package me.nd.rankup.listener.bloqueador;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import me.nd.rankup.api.LocationsAPI;


public class BloquearPassarDaBorda implements Listener {
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	 void aopassar(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if (p.getWorld().getWorldBorder() != null) {
			double worldborder = p.getWorld().getWorldBorder().getSize() / 2.0D; 
			Location center = p.getWorld().getWorldBorder().getCenter();
			Location to = e.getTo();
			 if (center.getX() + worldborder < to.getX()) {
	               p.teleport(LocationsAPI.spawn, TeleportCause.PLUGIN);
	               p.teleport(LocationsAPI.spawn, TeleportCause.SPECTATE);
	               p.teleport(LocationsAPI.spawn, TeleportCause.UNKNOWN);
	               e.setCancelled(true);
				}else if (center.getX() - worldborder > to.getX()) {
               p.teleport(LocationsAPI.spawn, TeleportCause.PLUGIN);
               p.teleport(LocationsAPI.spawn, TeleportCause.SPECTATE);
               p.teleport(LocationsAPI.spawn, TeleportCause.UNKNOWN);
               e.setCancelled(true);
			}else if (center.getZ() + worldborder < to.getZ()) {
	               p.teleport(LocationsAPI.spawn, TeleportCause.PLUGIN);
	               p.teleport(LocationsAPI.spawn, TeleportCause.SPECTATE);
	               p.teleport(LocationsAPI.spawn, TeleportCause.UNKNOWN);
	               e.setCancelled(true);
			}else if (center.getZ() - worldborder > to.getZ()) {
	               p.teleport(LocationsAPI.spawn, TeleportCause.PLUGIN);
	               p.teleport(LocationsAPI.spawn, TeleportCause.SPECTATE);
	               p.teleport(LocationsAPI.spawn, TeleportCause.UNKNOWN);
	               e.setCancelled(true);
			}
		}
	}
	
  
@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 void aoLancarEnderPearl(PlayerTeleportEvent e) {
	Player p = e.getPlayer();
	if (e.getCause() == TeleportCause.ENDER_PEARL && p.getWorld().getWorldBorder() != null) {
		double worldborder = p.getWorld().getWorldBorder().getSize() / 2.0D; 
		Location center = p.getWorld().getWorldBorder().getCenter();
		Location to = e.getTo();
		if (center.getX() + worldborder < to.getX()) {
			p.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
			e.setCancelled(true);
		} else if (center.getX() - worldborder > to.getX()) {
			p.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
			e.setCancelled(true);
		} else if (center.getZ() + worldborder < to.getZ()) {
			p.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
			e.setCancelled(true);
		} else if (center.getZ() - worldborder > to.getZ()) {
			p.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
			e.setCancelled(true);

		}
	}
}
	
}