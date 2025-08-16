package me.nd.rankup.comands;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.nd.rankup.Main;
import me.nd.rankup.api.LocationsAPI;
import me.nd.rankup.dados.DateManager;
import me.nd.rankup.plugin.SConfig;
import me.nd.rankup.utils.messages.MessageUtils;

public class SetSpawn extends Commands {
	
    public SetSpawn() {
        super("SetSpawn");
    }

    public void perform(CommandSender s, String lb, String[] args) {
        SConfig m = Main.get().getConfig("Mensagens");
        SConfig per = Main.get().getConfig("Permissoes");  
        SConfig so = Main.get().getConfig("Utils","Sons");    
    	Player p = (Player)s;
	if (!(s instanceof Player)) {
		MessageUtils.send(p, m.getString("Geral.Console-Nao-Pode"), m.getStringList("Geral.Console-Nao-Pode"));
		return;
	}
	if(!p.hasPermission(per.getString("SetSpawn.Permissao"))) {
		MessageUtils.send(p, m.getString("Geral.Sem-perm"), m.getStringList("Geral.Sem-perm"));
		p.playSound(p.getLocation(), Sound.valueOf(so.getString("SetSpawn.SemPerm")), 1.0F, 1.0F);
		return;
	}
	
	if (args.length != 1) {
		MessageUtils.send(p, m.getString("SetSpawn.SetSpawn-Comando-Incorreto"), m.getStringList("SetSpawn.SetSpawn-Comando-Incorreto"));
		return;
	}

	
	Location loc = p.getLocation(); 
	File file = DateManager.getFile("locations");
	FileConfiguration config = DateManager.getConfiguration(file);
	
	if (args[0].equals("normal")) {
		LocationsAPI.spawn = loc;
		p.getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		config.set("Spawn", loc);
		config.set("Spawn.world", loc.getWorld().getName());
		config.set("Spawn.x", Double.valueOf(loc.getX()));
		config.set("Spawn.y", Double.valueOf(loc.getY()));
		config.set("Spawn.z", Double.valueOf(loc.getZ()));
		config.set("Spawn.yaw", Float.valueOf(loc.getYaw()));
		config.set("Spawn.pitch", Float.valueOf(loc.getPitch()));
		try {
			config.save(file);
			MessageUtils.send(p, m.getString("SetSpawn.SetSpawn-Normal-Definido"), m.getStringList("SetSpawn.SetSpawn-Normal-Definido"));
		} catch (IOException e) {
			Bukkit.getConsoleSender().sendMessage(m.getString("Geral.Falha-Ao-Salvar").replace("&", "§").replace("{arquivo}", "locations.yml"));
		}
		return;
		} 
	if (args[0].equals("vip")) {
		p.getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		LocationsAPI.spawnVip = loc;
		config.set("SpawnVip", loc);
		config.set("SpawnVip.world", loc.getWorld().getName());
		config.set("SpawnVip.x", Double.valueOf(loc.getX()));
		config.set("SpawnVip.y", Double.valueOf(loc.getY()));
		config.set("SpawnVip.z", Double.valueOf(loc.getZ()));
		config.set("SpawnVip.yaw", Float.valueOf(loc.getYaw()));
		config.set("SpawnVip.pitch", Float.valueOf(loc.getPitch()));
		try {
			config.save(file);
		MessageUtils.send(p, m.getString("SetSpawn.SetSpawn-Vip-Definido"), m.getStringList("SetSpawn.SetSpawn-Vip-Definido"));
		} catch (IOException e) {
			Bukkit.getConsoleSender().sendMessage(m.getString("Geral.Falha-Ao-Salvar").replace("&", "§").replace("{arquivo}", "locations.yml"));
		}
		return;
	}
	MessageUtils.send(p, m.getString("SetSpawn.SetSpawn-Comando-Incorreto"), m.getStringList("SetSpawn.SetSpawn-Comando-Incorreto"));
	return;
    
    }
   
}

