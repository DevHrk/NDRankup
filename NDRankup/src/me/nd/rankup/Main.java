package me.nd.rankup;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.clip.placeholderapi.PlaceholderAPI;
import me.nd.rankup.api.LocationsAPI;
import me.nd.rankup.api.RExpansion;
import me.nd.rankup.comands.Commands;
import me.nd.rankup.dados.DateManager;
import me.nd.rankup.dados.SQlite;
import me.nd.rankup.listener.Listenners;
import me.nd.rankup.menus.Menu;
import me.nd.rankup.menus.MenuController;
import me.nd.rankup.plugin.SConfig;
import me.nd.rankup.utils.FileUtils;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {
	
	private static Economy economy;
	private SQlite sqlite;
	private FileUtils fileUtils;
	public static File menusDirectory;
	public static MenuController controller;
	
	@Override
	public void onEnable() {
		Bukkit.getServer().getConsoleSender().sendMessage("§e[NDRankup] Plugin iniciado com sucesso!");
		saveDefaultConfig();
		fileUtils = new FileUtils(this);
		setupSQL();
		if (!setupEconomy()) {
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		KitSetup();
		Commands.setupCommands();
		Listenners.setupListeners();
		LocationsAPI.loadLocations();
		Bukkit.getScheduler().runTaskLater(this, Menu::setupMenu, 10L);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PlaceholderAPI.registerExpansion(new RExpansion());
        }
	}
	
	@Override
	public void onDisable() {
		SQlite.closeConnection();
		Bukkit.getServer().getConsoleSender().sendMessage("§e[NDRankup] Plugin desativado com sucesso!");
	}
	
	public void setupSQL() {
        DateManager.createFolder("database");
        sqlite = new SQlite();
	}
	
	public void KitSetup() {
        File kitsFolder = new File(getDataFolder(), "Kits");
        if (!kitsFolder.exists()) {
            kitsFolder.mkdirs();
        }
	}
	
	public boolean setupEconomy() {
		// Verifica se o Vault está presente
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		
		// Tenta registrar o provedor de economia
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
		if (economyProvider == null) {
			return false;
		}
		
		economy = economyProvider.getProvider();
		if (economy == null) {
			return false;
		}
		
		Bukkit.getServer().getConsoleSender().sendMessage("§a[NDRankup] Economia do Vault inicializada com sucesso: " + economy.getName());
		return true;
	}
	
	public static Main get() {
		return getPlugin(Main.class);
	}
	
	public SConfig getConfig(String name) {
		return getConfig("", name);
	}
	
	public SConfig getConfig(String path, String name) {
		return SConfig.getConfig(this, "plugins/" + getName() + "/" + path, name);
	}
	
	public FileUtils getFileUtils() {
		return this.fileUtils;
	}
	
	public File getMenusDirectory() {
		return menusDirectory;
	}
	
	public MenuController getController() {
		return controller;
	}
	
	public SQlite getSqlite() {
		return this.sqlite;
	}
	
	public Economy getEconomy() {
		return economy;
	}
}