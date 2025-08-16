package me.nd.rankup.listener.bloqueador;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

import me.nd.rankup.Main;
import me.nd.rankup.plugin.SConfig;

@SuppressWarnings("all")
public class BloquearCrafts implements Listener {

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	 void aoPrepararCraft(PrepareItemCraftEvent e) {
		SConfig m = Main.get().getConfig("Opcoes","OpcoesMundo");
		SConfig per = Main.get().getConfig("Permissoes");  
		ArrayList<String> lore2 = new ArrayList<>();
	   	 for (String lorez : m.getStringList("Crafts.Lista-Dos-Crafts-Bloqueados")) {lore2.add(lorez);}		
		if (e.getRecipe() != null && e.getRecipe().getResult() != null) {
			int itemType = e.getRecipe().getResult().getType().getId();
			if (lore2.contains(itemType)) {
				Player p = (Player) e.getView().getPlayer();
				if (!(p.hasPermission(per.getString("ByPass.Craft"))))
					e.getInventory().setResult(new ItemStack(Material.AIR));
			}
		}
	}
	
}