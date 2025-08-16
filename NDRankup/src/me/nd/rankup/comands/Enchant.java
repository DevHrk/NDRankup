package me.nd.rankup.comands;

import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Enchant extends Commands {

    public Enchant() {
        super("enchant");
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        // Verifica se o remetente é um jogador
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando só pode ser usado por jogadores!");
            return;
        }

        Player player = (Player) sender;

        // Verifica permissão
        if (!player.hasPermission("yourplugin.enchant")) {
            player.sendMessage(ChatColor.RED + "Você não tem permissão para usar este comando!");
            return;
        }

        // Verifica se há argumentos suficientes
        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Uso: /" + label + " <encantamento> <nível>");
            return;
        }

        // Obtém o item na mão do jogador
        ItemStack item = player.getInventory().getItemInHand();
        if (item == null || item.getType().isTransparent()) {
            player.sendMessage(ChatColor.RED + "Você precisa estar segurando um item!");
            return;
        }

        // Obtém o encantamento
        Enchantment enchantment = Enchantment.getByName(args[0].toUpperCase());
        if (enchantment == null) {
            player.sendMessage(ChatColor.RED + "Encantamento inválido! Use o nome em inglês (ex: SHARPNESS)");
            return;
        }

        // Obtém o nível
        int level;
        try {
            level = Integer.parseInt(args[1]);
            if (level < 0) {
                player.sendMessage(ChatColor.RED + "O nível deve ser um número positivo!");
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "O nível deve ser um número válido!");
            return;
        }

        // Verifica se o encantamento pode ser aplicado ao item
        if (!enchantment.canEnchantItem(item)) {
            player.sendMessage(ChatColor.RED + "Este encantamento não pode ser aplicado a este item!");
            return;
        }

        // Aplica o encantamento
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(enchantment, level, true); // true permite níveis acima do padrão
        item.setItemMeta(meta);

        player.sendMessage(ChatColor.GREEN + "Item encantado com " + enchantment.getName() + " nível " + level + "!");
    }
}