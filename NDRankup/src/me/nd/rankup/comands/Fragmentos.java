package me.nd.rankup.comands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.nd.rankup.api.FormatterAPI;
import me.nd.rankup.dados.SQlite;
import me.nd.rankup.fragmento.FragmentoManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Fragmentos extends Commands {
    private static final Connection connection = SQlite.getConnection();

    public Fragmentos() {
        super("fragmento");
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            // /fragmento - Mostra o próprio saldo
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cEste comando é apenas para jogadores!");
                return;
            }
            Player player = (Player) sender;
            long fragmento = FragmentoManager.getfragmento(player);
            sender.sendMessage("§eSaldo: §6" + FormatterAPI.formatNumber(fragmento));
            return;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "enviar":
                handleSendfragmento(sender, args);
                break;
            case "top":
                handleTopfragmento(sender);
                break;
            case "ajuda":
                handleHelp(sender);
                break;
            case "add":
                handleAddfragmento(sender, args);
                break;
            case "set":
                handleSetfragmento(sender, args);
                break;
            case "remove":
                handleRemovefragmento(sender, args);
                break;
            case "reset":
                handleResetfragmento(sender, args);
                break;
            case "check":
                handleCheck(sender);
                break;
            default:
                // /fragmento <jogador> - Mostra o saldo de outro jogador
                String targetName = args[0];
                String targetUUID = getUUIDFromName(targetName);
                if (targetUUID == null) {
                    sender.sendMessage("§cJogador '" + targetName + "' não encontrado!");
                    return;
                }
                long fragmento = FragmentoManager.getfragmento(targetUUID);
                sender.sendMessage("§aSaldo de fragmento de " + targetName + ": §6" + FormatterAPI.formatNumber(fragmento));
        }
    }

    private void handleSendfragmento(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nd.command.pay")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando é apenas para jogadores!");
            return;
        }
        if (args.length != 3) {
            sender.sendMessage("§cUso: /fragmento enviar <jogador> <quantia>");
            return;
        }
        String targetName = args[1];
        String targetUUID = getUUIDFromName(targetName);
        if (targetUUID == null) {
            sender.sendMessage("§cJogador '" + targetName + "' não encontrado!");
            return;
        }
        if (targetUUID.equals(((Player) sender).getUniqueId().toString())) {
            sender.sendMessage("§cVocê não pode enviar fragmento para si mesmo!");
            return;
        }
        long amount;
        try {
            amount = Long.parseLong(args[2]);
            if (amount <= 0) {
                sender.sendMessage("§cA quantia deve ser um número positivo!");
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cA quantia deve ser um número válido!");
            return;
        }
        Player player = (Player) sender;
        long playerfragmento = FragmentoManager.getfragmento(player);
        if (playerfragmento < amount) {
            sender.sendMessage("§cVocê não tem fragmento suficiente! Seu saldo: §6" + playerfragmento);
            return;
        }
        if (FragmentoManager.removefragmento(player, amount) && FragmentoManager.addfragmento(targetUUID, amount)) {
            sender.sendMessage("§aVocê enviou §6" + FormatterAPI.formatNumber(amount) + " fragmento §apara " + targetName + "!");
            Player target = Bukkit.getPlayer(UUID.fromString(targetUUID));
            if (target != null && target.isOnline()) {
                target.sendMessage("§aVocê recebeu §6" + FormatterAPI.formatNumber(amount) + " fragmento §ade " + player.getName() + "!");
            }
        } else {
            sender.sendMessage("§cErro ao processar a transação!");
        }
    }

    private void handleTopfragmento(CommandSender sender) {
        if (!sender.hasPermission("nd.command.top")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return;
        }
        sender.sendMessage("§8=== §eTop 10 Jogadores com Mais fragmento §8===");
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT uuid, fragmento FROM player_fragmento ORDER BY fragmento DESC LIMIT 10")) {
            ResultSet rs = ps.executeQuery();
            int rank = 1;
            while (rs.next()) {
                String uuid = rs.getString("uuid");
                long fragmento = rs.getLong("fragmento");
                String name = getNameFromUUID(uuid);
                sender.sendMessage("§f" + rank + "º §f" + (name != null ? name : uuid) + ": §e" + FormatterAPI.formatNumber(fragmento) + " fragmento");
                rank++;
            }
        } catch (SQLException e) {
            sender.sendMessage("§cErro ao consultar o ranking de fragmento!");
            Bukkit.getConsoleSender().sendMessage("§c[NDRankup] Erro ao consultar top fragmento:");
            e.printStackTrace();
        }
    }

    private void handleHelp(CommandSender sender) {
        if (!sender.hasPermission("nd.command.help")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return;
        }
        sender.sendMessage("§a=== Comandos do Sistema de fragmento ===");
        sender.sendMessage("§6/fragmento §f- Veja seu saldo de fragmento.");
        sender.sendMessage("§6/fragmento <jogador> §f- Veja o saldo de outro jogador.");
        if (sender.hasPermission("nd.command.pay")) {
            sender.sendMessage("§6/fragmento enviar <jogador> <quantia> §f- Envie fragmento para outro jogador.");
        }
        if (sender.hasPermission("nd.command.top")) {
            sender.sendMessage("§6/fragmento top §f- Veja os 10 jogadores com mais fragmento.");
        }
        if (sender.hasPermission("nd.command.add")) {
            sender.sendMessage("§6/fragmento add <jogador> <quantia> §f- Adicione fragmento a um jogador.");
        }
        if (sender.hasPermission("nd.command.set")) {
            sender.sendMessage("§6/fragmento set <jogador> <quantia> §f- Defina o saldo de um jogador.");
        }
        if (sender.hasPermission("nd.command.remove")) {
            sender.sendMessage("§6/fragmento remove <jogador> <quantia> §f- Remova fragmento de um jogador.");
        }
        if (sender.hasPermission("nd.command.reset")) {
            sender.sendMessage("§6/fragmento reset <jogador> §f- Zere o saldo de um jogador.");
        }
        if (sender.hasPermission("nd.command.check")) {
            sender.sendMessage("§6/fragmento check §f- Veja informações sobre o sistema de cheques.");
        }
    }

    private void handleAddfragmento(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nd.command.add")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return;
        }
        if (args.length != 3) {
            sender.sendMessage("§cUso: /fragmento add <jogador> <quantia>");
            return;
        }
        String targetName = args[1];
        String targetUUID = getUUIDFromName(targetName);
        if (targetUUID == null) {
            sender.sendMessage("§cJogador '" + targetName + "' não encontrado!");
            return;
        }
        long amount;
        try {
            amount = Long.parseLong(args[2]);
            if (amount <= 0) {
                sender.sendMessage("§cA quantia deve ser um número positivo!");
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cA quantia deve ser um número válido!");
            return;
        }
        if (FragmentoManager.addfragmento(targetUUID, amount)) {
            sender.sendMessage("§aAdicionado §6" + FormatterAPI.formatNumber(amount) + " fragmento §apara " + targetName + "!");
        } else {
            sender.sendMessage("§cErro ao adicionar fragmento!");
        }
    }

    private void handleSetfragmento(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nd.command.set")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return;
        }
        if (args.length != 3) {
            sender.sendMessage("§cUso: /fragmento set <jogador> <quantia>");
            return;
        }
        String targetName = args[1];
        String targetUUID = getUUIDFromName(targetName);
        if (targetUUID == null) {
            sender.sendMessage("§cJogador '" + targetName + "' não encontrado!");
            return;
        }
        long amount;
        try {
            amount = Long.parseLong(args[2]);
            if (amount < 0) {
                sender.sendMessage("§cA quantia não pode ser negativa!");
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cA quantia deve ser um número válido!");
            return;
        }
        if (FragmentoManager.setfragmento(targetUUID, amount)) {
            sender.sendMessage("§aSaldo de " + targetName + " definido para §6" + FormatterAPI.formatNumber(amount) + " fragmento!");
        } else {
            sender.sendMessage("§cErro ao definir o fragmento!");
        }
    }

    private void handleRemovefragmento(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nd.command.remove")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return;
        }
        if (args.length != 3) {
            sender.sendMessage("§cUso: /fragmento remove <jogador> <quantia>");
            return;
        }
        String targetName = args[1];
        String targetUUID = getUUIDFromName(targetName);
        if (targetUUID == null) {
            sender.sendMessage("§cJogador '" + targetName + "' não encontrado!");
            return;
        }
        long amount;
        try {
            amount = Long.parseLong(args[2]);
            if (amount <= 0) {
                sender.sendMessage("§cA quantia deve ser um número positivo!");
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cA quantia deve ser um número válido!");
            return;
        }
        if (FragmentoManager.removefragmento(targetUUID, amount)) {
            sender.sendMessage("§aRemovido §6" + FormatterAPI.formatNumber(amount) + " fragmento §ade " + targetName + "!");
        } else {
            sender.sendMessage("§cErro ao remover fragmento!");
        }
    }

    private void handleResetfragmento(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nd.command.reset")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return;
        }
        if (args.length != 2) {
            sender.sendMessage("§cUso: /fragmento reset <jogador>");
            return;
        }
        String targetName = args[1];
        String targetUUID = getUUIDFromName(targetName);
        if (targetUUID == null) {
            sender.sendMessage("§cJogador '" + targetName + "' não encontrado!");
            return;
        }
        if (FragmentoManager.setfragmento(targetUUID, 0)) {
            sender.sendMessage("§aSaldo de " + targetName + " zerado!");
        } else {
            sender.sendMessage("§cErro ao zerar o saldo!");
        }
    }

    private void handleCheck(CommandSender sender) {
        if (!sender.hasPermission("nd.command.check")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return;
        }
        sender.sendMessage("§a=== Sistema de Cheques ===");
        sender.sendMessage("§fO sistema de cheques ainda não está implementado.");
        sender.sendMessage("§fUse '/cheque' para criar e gerenciar cheques (em desenvolvimento).");
        sender.sendMessage("§fEntre em contato com a administração para mais informações.");
    }

    // Método auxiliar para obter UUID a partir do nome
    private String getUUIDFromName(String name) {
        Player target = Bukkit.getPlayerExact(name);
        if (target != null && target.isOnline()) {
            return target.getUniqueId().toString();
        }
        // Consulta offline (assumindo que o servidor armazena UUIDs em algum lugar)
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT uuid FROM player_fragmento WHERE uuid IN (SELECT uuid FROM player_fragmento)")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String uuid = rs.getString("uuid");
                String playerName = getNameFromUUID(uuid);
                if (playerName != null && playerName.equalsIgnoreCase(name)) {
                    return uuid;
                }
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDRankup] Erro ao consultar UUID para " + name + ":");
            e.printStackTrace();
        }
        return null;
    }

    // Método auxiliar para obter nome a partir do UUID
    private String getNameFromUUID(String uuid) {
        Player player = Bukkit.getPlayer(UUID.fromString(uuid));
        if (player != null && player.isOnline()) {
            return player.getName();
        }
        // Tenta obter o nome offline (pode ser implementado com um sistema de cache ou API externa)
        return null; // Retorna null se não encontrar o nome
    }
}