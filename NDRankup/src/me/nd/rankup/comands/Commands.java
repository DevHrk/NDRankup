package me.nd.rankup.comands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;

public abstract class Commands extends Command {
	
	  public Commands(String name, String... aliases) {
		    super(name);
		    setAliases(Arrays.asList(aliases));
		    
		    try {
		      SimpleCommandMap simpleCommandMap = (SimpleCommandMap) Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap").invoke(Bukkit.getServer());
		      simpleCommandMap.register(getName(), "ndrankup", this);
		    } catch (ReflectiveOperationException ex) {
		    	
		    }
		  }
	  
	  public static void setupCommands() {
		  new Cash();
		  new Kit();
		  new Gamemode();
		  new Speed();
		  new Clearchat();
		  new Rankup();
		  new Rank();
		  new Fragmentos();
		  new Ranks();
		  new Prestigio();
		  new SetSpawn();
		  new Spawn();
		  new Enchant();
		  new MenuReload();
	  }
	  
	  public abstract void perform(CommandSender sender, String label, String[] args);
	  
	  @Override
	  public boolean execute(CommandSender sender, String commandLabel, String[] args) {
	    perform(sender, commandLabel, args);
	    return true;
	  }
	
}
