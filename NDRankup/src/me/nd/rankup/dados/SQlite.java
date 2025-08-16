package me.nd.rankup.dados;

import org.bukkit.Bukkit;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQlite {
    private static Connection connection;

    public SQlite() {
        this.openConnection();
    }
    
    public void openConnection() {
        File file = new File("plugins/NDRankup/DataBase/database.db");
        String url = "jdbc:sqlite:" + file;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(url);
            Bukkit.getConsoleSender().sendMessage("§c[NDRankup] §fConexão com §6SQLite §faberta com sucesso");
            this.createTables();
        }
        catch (ClassNotFoundException | SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDRankup] §cHouve um erro ao tentar fazer conexão com §6SQLite");
        }
    }

    public static void closeConnection() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
            Bukkit.getConsoleSender().sendMessage("§cConexão com SQLite fechada com sucesso");
        }
        catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§cOcorreu um erro ao tentar fechar a conexão com o SQLite, erro:");
            e.printStackTrace();
        }
    }

    public static boolean executeQuery(String query, Object[] objects) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.execute();
            preparedStatement.close();
            return true;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void executeUpdate(String query, Object ... params) {
        try (PreparedStatement ps = connection.prepareStatement(query);){
            if (params != null && params.length > 0) {
                for (int index = 0; index < params.length; ++index) {
                    ps.setObject(index + 1, params[index]);
                }
            }
            ps.executeUpdate();
        }
        catch (SQLException var16) {
            var16.printStackTrace();
        }
    }

    public void createTables() {
        try {
            try (PreparedStatement ps1 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS ender_chests (" +
                    "uuid TEXT, " +
                    "chest_number INTEGER, " +
                    "slot INTEGER, " +
                    "item TEXT, " +
                    "custom_name TEXT DEFAULT 'Baú #{chest_number}', " +
                    "size INTEGER DEFAULT 27, " +
                    "icon TEXT DEFAULT 'CHEST', " +
                    "PRIMARY KEY (uuid, chest_number, slot))")) {
                ps1.execute();
            }

            try (PreparedStatement ps2 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS ender_chest_access (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "unlocked_chests INTEGER DEFAULT 1)")) {
                ps2.execute();
            }
            
            try (PreparedStatement ps3 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS ender_chest_metadata (" +
                    "uuid TEXT, " +
                    "chest_number INTEGER, " +
                    "custom_name TEXT DEFAULT 'Baú #{chest_number}', " +
                    "size INTEGER DEFAULT 27, " +
                    "icon TEXT DEFAULT 'CHEST', " +
                    "PRIMARY KEY (uuid, chest_number))")) {
                ps3.execute();
            }
            // Tabela player_cash
            try (PreparedStatement ps4 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS player_cash (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "cash INTEGER DEFAULT 0)")) {
                ps4.execute();
            }
            
            try (PreparedStatement ps5 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS player_ranks (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "rank INTEGER DEFAULT 0)")) {
                ps5.executeUpdate();
            }

            // Tabela player_prestige
            try (PreparedStatement ps6 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS player_prestige (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "prestige INTEGER DEFAULT 0)")) {
                ps6.executeUpdate();
            }

            // Tabela player_settings
            try (PreparedStatement ps7 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS player_settings (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "autorankup BOOLEAN DEFAULT FALSE)")) {
                ps7.executeUpdate();
            }

            // Tabela player_heads
            try (PreparedStatement ps8 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS player_heads (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "heads INTEGER DEFAULT 0)")) {
                ps8.executeUpdate();
            }
            try (PreparedStatement ps9 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS player_fragmento (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "fragmento INTEGER DEFAULT 0)")) {
                ps9.execute();
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao criar tabelas no SQLite:");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return connection;
    }
}
