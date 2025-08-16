package me.nd.rankup.comands;

import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import me.nd.rankup.Main;
import me.nd.rankup.menus.MenuController;

public class MenuReload extends Commands {

    public MenuReload() {
        super("mr");
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        // Check if sender has permission
        if (!sender.hasPermission("ndrankup.reload")) {
            sender.sendMessage(ChatColor.RED + "Você não tem permissão para executar esté comando!");
            return;
        }

        
        // Get the menu controller
        MenuController controller = Main.controller;
        
        // Clear existing menus
        controller.getMenus().clear();
        
        // Reload menus from configuration files
        controller.loadMenus();
        
        // Send success message
        sender.sendMessage(ChatColor.GREEN + "Menu Recarregado com sucesso!");
    }
}