package me.nd.rankup.fragmento;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.nd.rankup.dados.SQlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FragmentoManager {
    private static final Connection connection = SQlite.getConnection();

    // Adiciona fragmento ao jogador
    public static boolean addfragmento(Player player, long amount) {
        return addfragmento(player.getUniqueId().toString(), amount);
    }

    public static boolean addfragmento(String uuid, long amount) {
        if (amount < 0) {
            Bukkit.getConsoleSender().sendMessage("§c[NDRankup] Tentativa de adicionar valor negativo de fragmento: " + amount);
            return false;
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO player_fragmento (uuid, fragmento) VALUES (?, COALESCE((SELECT fragmento FROM player_fragmento WHERE uuid = ?), 0) + ?)")) {
            ps.setString(1, uuid);
            ps.setString(2, uuid);
            ps.setLong(3, amount);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDRankup] Erro ao adicionar fragmento para UUID " + uuid + ":");
            e.printStackTrace();
            return false;
        }
    }

    // Remove fragmento do jogador
    public static boolean removefragmento(Player player, long amount) {
        return removefragmento(player.getUniqueId().toString(), amount);
    }

    public static boolean removefragmento(String uuid, long amount) {
        if (amount < 0) {
            Bukkit.getConsoleSender().sendMessage("§c[NDRankup] Tentativa de remover valor negativo de fragmento: " + amount);
            return false;
        }
        // Obtém o saldo atual
        long currentfragmento = getfragmento(uuid);
        if (currentfragmento < amount) {
            // Não permite que o saldo fique negativo
            return false;
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO player_fragmento (uuid, fragmento) VALUES (?, ?)")) {
            ps.setString(1, uuid);
            ps.setLong(2, currentfragmento - amount);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDRankup] Erro ao remover fragmento para UUID " + uuid + ":");
            e.printStackTrace();
            return false;
        }
    }

    // Define o fragmento do jogador
    public static boolean setfragmento(Player player, long amount) {
        return setfragmento(player.getUniqueId().toString(), amount);
    }

    public static boolean setfragmento(String uuid, long amount) {
        if (amount < 0) {
            Bukkit.getConsoleSender().sendMessage("§c[NDRankup] Tentativa de definir valor negativo de fragmento: " + amount);
            return false;
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO player_fragmento (uuid, fragmento) VALUES (?, ?)")) {
            ps.setString(1, uuid);
            ps.setLong(2, amount);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDRankup] Erro ao definir fragmento para UUID " + uuid + ":");
            e.printStackTrace();
            return false;
        }
    }

    // Obtém o fragmento do jogador
    public static long getfragmento(Player player) {
        return getfragmento(player.getUniqueId().toString());
    }

    public static long getfragmento(String uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT fragmento FROM player_fragmento WHERE uuid = ?")) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("fragmento");
            }
            return 0L; // Retorna 0 se o jogador não tiver registro
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDRankup] Erro ao consultar fragmento para UUID " + uuid + ":");
            e.printStackTrace();
            return 0L;
        }
    }
}