package me.nd.rankup.listener.bloqueador;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import me.nd.rankup.Main;

public class DayListener {

	public static void stopDaylightCycle() {
		new BukkitRunnable() {
			@Override
			public void run() {
				for (World w : Bukkit.getWorlds()) {
					w.setGameRuleValue("doDaylightCycle", "false");
					w.setTime(6000);
				}
			}
		}.runTaskLater(Main.get(), 600L);
	}

	public static void stopDaylightCycleOLD() {
		new BukkitRunnable() {
			@Override
			public void run() {
				for (World w : Bukkit.getWorlds()) {
					w.setTime(6000);
				}
			}
		}.runTaskTimer(Main.get(), 600L, 600L);
	}

}
