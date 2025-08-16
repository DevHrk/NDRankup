package me.nd.rankup.comands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.nd.rankup.Main;
import me.nd.rankup.api.ActionbarAPI;
import me.nd.rankup.api.FormatterAPI;
import me.nd.rankup.api.TitleAPI;
import me.nd.rankup.dados.SQlite;
import me.nd.rankup.plugin.SConfig;
import me.nd.rankup.utils.ItemBuilder;
import net.milkbowl.vault.economy.Economy;

public class Rankup extends Commands {
    private static final Connection connection = SQlite.getConnection();
    private static final SConfig rankConfig = Main.get().getConfig("rank");
    private static final Economy economy = Main.get().getEconomy();

    public Rankup() {
        super("rankup");
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando é apenas para jogadores!");
            return;
        }

        Player player = (Player) sender;

        // Verifica permissão
        if (!player.hasPermission("yrankup.rankup")) {
            player.sendMessage("§cVocê não tem permissão para usar este comando!");
            return;
        }

        // Verifica se é /rankup max
        if (args.length > 0 && args[0].equalsIgnoreCase("max") && player.hasPermission("yrankup.rankup.max")) {
            performMaxRankup(player);
            return;
        }

        // Abre o menu de rankup
        openRankupMenu(player);
    }

    public static void openRankupMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Menu de Rankup");

        // Obtém o rank atual e recursos do jogador
        int currentRank = getPlayerRank(player.getUniqueId().toString());
        int fragments = getPlayerFragments(player.getUniqueId().toString());
        double money = economy != null ? economy.getBalance(player) : 0.0;
        int prestige = Rank.getPlayerPrestige(player.getUniqueId().toString());
        String nextRankKey = "ranks.rank" + (currentRank + 1);
        String nextRankName = rankConfig.getString(nextRankKey + ".name", "N/A").replace("&", "§");
        String nextRankTag = rankConfig.getString(nextRankKey + ".tag", "").replace('&', '§');

        // Obtém custos do próximo rank com desconto de prestígio
        double discount = Prestigio.getPlayerDiscount(player.getUniqueId().toString(), prestige);
        int fragmentCost = rankConfig.getInt(nextRankKey + ".prices.fragmentos.price", 0);
        int moneyCost = rankConfig.getInt(nextRankKey + ".prices.money.price", 0);
        int discountedFragmentCost = (int) Math.round(fragmentCost * (1 - discount / 100.0));
        int discountedMoneyCost = (int) Math.round(moneyCost * (1 - discount / 100.0));
        boolean hasFragmentProvider = rankConfig.contains(nextRankKey + ".prices.fragmentos");
        boolean hasMoneyProvider = rankConfig.contains(nextRankKey + ".prices.money");
        boolean canRankup = (!hasFragmentProvider || fragments >= discountedFragmentCost) &&
                            (!hasMoneyProvider || money >= discountedMoneyCost) &&
                            rankConfig.contains(nextRankKey);

        // Item representando o próximo rank
        ItemStack rankItem = new ItemBuilder(Material.DIAMOND)
                .setName("§aPróximo Rank: " + nextRankName.replace("&", "§"))
                .setLore(generateLore(player,nextRankTag, fragmentCost, moneyCost, discountedFragmentCost, discountedMoneyCost, fragments, money, prestige, hasFragmentProvider, hasMoneyProvider, canRankup))
                .toItemStack();
        inv.setItem(13, rankItem);

        player.openInventory(inv);
    }

    private static List<String> generateLore(Player player,String nextRankTag, int fragmentCost, int moneyCost, int discountedFragmentCost, int discountedMoneyCost, int fragments, double money, int prestige, boolean hasFragmentProvider, boolean hasMoneyProvider, boolean canRankup) {
        List<String> lore = new ArrayList<>();
        double discount = Prestigio.getPlayerDiscount(player.getUniqueId().toString(), prestige);
        lore.add("§7Tag: " + nextRankTag);
        lore.add("");
        lore.add("§fCusto:");
        if (hasFragmentProvider) {
            if (discount > 0) {
                lore.add(" §7* §e" + FormatterAPI.formatNumber(discountedFragmentCost) + " §ffragmentos");
            } else {
                lore.add(" §7* §e" + FormatterAPI.formatNumber(fragmentCost) + " §ffragmentos");
            }
        }
        if (hasMoneyProvider) {
            if (discount > 0) {
                lore.add(" §7* §a" + FormatterAPI.formatNumber(discountedMoneyCost) + " §fcoins");
            } else {
                lore.add(" §7* §a" + FormatterAPI.formatNumber(moneyCost) + " §fcoins");
            }
        }
        lore.add("");
        lore.add(canRankup ? "§aClique para subir de rank!" : "§cRecursos insuficientes!");
        return lore;
    }

    public static void performMaxRankup(Player player) {
        String uuid = player.getUniqueId().toString();
        int currentRank = getPlayerRank(uuid);
        int fragments = getPlayerFragments(uuid);
        double money = economy != null ? economy.getBalance(player) : 0.0;
        int prestige = Rank.getPlayerPrestige(uuid);
        int ranksPurchased = 0;

        while (true) {
            String nextRankKey = "ranks.rank" + (currentRank + 1);
            double discount = Prestigio.getPlayerDiscount(uuid, prestige);
            int fragmentCost = rankConfig.getInt(nextRankKey + ".prices.fragmentos.price", 0);
            int moneyCost = rankConfig.getInt(nextRankKey + ".prices.money.price", 0);
            int discountedFragmentCost = (int) Math.round(fragmentCost * (1 - discount / 100.0));
            int discountedMoneyCost = (int) Math.round(moneyCost * (1 - discount / 100.0));
            boolean hasFragmentProvider = rankConfig.contains(nextRankKey + ".prices.fragmentos");
            boolean hasMoneyProvider = rankConfig.contains(nextRankKey + ".prices.money");
            boolean canRankup = (!hasFragmentProvider || fragments >= discountedFragmentCost) &&
                                (!hasMoneyProvider || money >= discountedMoneyCost) &&
                                rankConfig.contains(nextRankKey);

            if (!canRankup) {
                break;
            }

            performRankupActions(player, currentRank + 1);
            if (hasFragmentProvider) fragments -= discountedFragmentCost;
            if (hasMoneyProvider && economy != null) economy.withdrawPlayer(player, discountedMoneyCost);
            currentRank++;
            ranksPurchased++;
            money = economy != null ? economy.getBalance(player) : 0.0; // Atualiza saldo
        }

        if (ranksPurchased > 0) {
            setPlayerRank(uuid, currentRank);
            setPlayerFragments(uuid, fragments);
            player.sendMessage("§aVocê subiu " + ranksPurchased + " ranks! Novo rank: " + currentRank);
        } else {
            player.sendMessage("§cVocê não tem recursos suficientes para subir de rank!");
        }
    }

    public static void performRankupActions(Player player, int newRank) {
        String rankKey = "ranks.rank" + newRank;
        if (!rankConfig.contains(rankKey)) return;

        // Deduz recursos com desconto de prestígio
        int fragments = getPlayerFragments(player.getUniqueId().toString());
        int prestige = Rank.getPlayerPrestige(player.getUniqueId().toString());
        double discount = Prestigio.getPlayerDiscount(player.getUniqueId().toString(), prestige);
        int fragmentCost = rankConfig.getInt(rankKey + ".prices.fragmentos.price", 0);
        int moneyCost = rankConfig.getInt(rankKey + ".prices.money.price", 0);
        int discountedFragmentCost = (int) Math.round(fragmentCost * (1 - discount / 100.0));
        int discountedMoneyCost = (int) Math.round(moneyCost * (1 - discount / 100.0));
        boolean hasFragmentProvider = rankConfig.contains(rankKey + ".prices.fragmentos");
        boolean hasMoneyProvider = rankConfig.contains(rankKey + ".prices.money");

        if (hasFragmentProvider) {
            setPlayerFragments(player.getUniqueId().toString(), fragments - discountedFragmentCost);
        }
        if (hasMoneyProvider && economy != null) {
            economy.withdrawPlayer(player, discountedMoneyCost);
        }

        // Executa comandos
        List<String> commands = rankConfig.getStringList(rankKey + ".rankup.commands");
        for (String command : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.getName()));
        }

        // Envia mensagens globais
        String chatMsg = rankConfig.getString(rankKey + ".rankup.messages.chat", "");
        if (!chatMsg.isEmpty()) {
            Bukkit.broadcastMessage(chatMsg.replace("{player}", player.getName()).replace('&', '§'));
        }

        String actionBarMsg = rankConfig.getString(rankKey + ".rankup.messages.actionbar", "");
        if (!actionBarMsg.isEmpty()) {
            ActionbarAPI.sendActionBarMessage(player, actionBarMsg.replace("{player}", player.getName()).replace('&', '§'));
        }

        String titleMsg = rankConfig.getString(rankKey + ".rankup.messages.title", "");
        String subtitleMsg = rankConfig.getString(rankKey + ".rankup.messages.subtitle", "");
        if (!titleMsg.isEmpty() || !subtitleMsg.isEmpty()) {
            TitleAPI.sendTitle(
                player, 20, 60, 20,
                titleMsg.replace("{player}", player.getName()).replace('&', '§'),
                subtitleMsg.replace("{player}", player.getName()).replace('&', '§')
            );
        }

        // Envia mensagens privadas
        String privateChatMsg = rankConfig.getString(rankKey + ".rankup.private-messages.chat", "");
        if (!privateChatMsg.isEmpty()) {
            player.sendMessage(privateChatMsg.replace("{player}", player.getName()).replace('&', '§'));
        }

        String privateActionBarMsg = rankConfig.getString(rankKey + ".rankup.private-messages.actionbar", "");
        if (!privateActionBarMsg.isEmpty()) {
            ActionbarAPI.sendActionBarMessage(player, privateActionBarMsg.replace("{player}", player.getName()).replace('&', '§'));
        }

        String privateTitleMsg = rankConfig.getString(rankKey + ".rankup.private-messages.title", "");
        String privateSubtitleMsg = rankConfig.getString(rankKey + ".rankup.private-messages.subtitle", "");
        if (!privateTitleMsg.isEmpty() || !privateSubtitleMsg.isEmpty()) {
            TitleAPI.sendTitle(
                player, 20, 60, 20,
                privateTitleMsg.replace("{player}", player.getName()).replace('&', '§'),
                privateSubtitleMsg.replace("{player}", player.getName()).replace('&', '§')
            );
        }

        // Envia mensagem de saldo
        String balanceMsg = rankConfig.getString(rankKey + ".rankup.private-messages.balance", "");
        if (!balanceMsg.isEmpty()) {
            String nextRankKey = "ranks.rank" + (newRank + 1);
            int nextFragmentCost = rankConfig.getInt(nextRankKey + ".prices.fragmentos.price", -1);
            int nextMoneyCost = rankConfig.getInt(nextRankKey + ".prices.money.price", -1);
            int discountedNextFragmentCost = (int) Math.round(nextFragmentCost * (1 - discount / 100.0));
            int discountedNextMoneyCost = (int) Math.round(nextMoneyCost * (1 - discount / 100.0));
            int currentFragments = getPlayerFragments(player.getUniqueId().toString());
            double currentMoney = economy != null ? economy.getBalance(player) : 0.0;
            player.sendMessage(balanceMsg
                    .replace("{player}", player.getName())
                    .replace("{fragmentos_has}", FormatterAPI.formatNumber(currentFragments))
                    .replace("{fragmentos_need}", FormatterAPI.formatNumber(discountedNextFragmentCost))
                    .replace("{money_has}", FormatterAPI.formatNumber(currentMoney))
                    .replace("{money_need}", FormatterAPI.formatNumber(discountedNextMoneyCost))
                    .replace('&', '§'));
        }
    }


    public static int getPlayerRank(String uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT rank FROM player_ranks WHERE uuid = ?")) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("rank");
            } else {
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO player_ranks (uuid, rank) VALUES (?, 0)")) {
                    insert.setString(1, uuid);
                    insert.executeUpdate();
                }
                return 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void setPlayerRank(String uuid, int rank) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE player_ranks SET rank = ? WHERE uuid = ?")) {
            ps.setInt(1, rank);
            ps.setString(2, uuid);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getPlayerFragments(String uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT fragmento FROM player_fragmento WHERE uuid = ?")) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("fragmento");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void setPlayerFragments(String uuid, int fragments) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE player_fragmento SET fragmento = ? WHERE uuid = ?")) {
            ps.setInt(1, fragments);
            ps.setString(2, uuid);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getNextRankCost(int currentRank) {
        String rankKey = "ranks.rank" + (currentRank + 1);
        if (rankConfig.contains(rankKey)) {
            return rankConfig.getInt(rankKey + ".prices.fragmentos.price", -1);
        }
        return -1;
    }
}