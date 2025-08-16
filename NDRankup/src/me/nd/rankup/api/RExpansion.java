package me.nd.rankup.api;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.mysql.jdbc.PreparedStatement;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.nd.rankup.Main;
import me.nd.rankup.cash.CashManager;
import me.nd.rankup.comands.Prestigio;
import me.nd.rankup.comands.Rank;
import me.nd.rankup.comands.Rankup;
import me.nd.rankup.dados.SQlite;
import me.nd.rankup.plugin.SConfig;
import me.nd.rankup.utils.StringUtils;

public class RExpansion extends PlaceholderExpansion {

    private static final Connection connection = SQlite.getConnection();
    private static final SConfig rankConfig = Main.get().getConfig("rank");

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return "NinjaDark99";
    }

    @Override
    public String getIdentifier() {
        return "nd";
    }

    @Override
    public String getVersion() {
        return Main.get().getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return "";
        }

        String uuid = player.getUniqueId().toString();

        if (params.startsWith("rankup_")) {
            return handleRankupPlaceholder(player, uuid, params);
        }

        return null;
    }

    private String handleRankupPlaceholder(Player player, String uuid, String params) {
        String value = params.replace("rankup_", "").toLowerCase();

        switch (value) {
            case "rank":
                return String.valueOf(Rankup.getPlayerRank(uuid));
            case "prestige":
                return String.valueOf(Rank.getPlayerPrestige(uuid));
            case "cash":
                return FormatterAPI.formatNumber(CashManager.getCash(uuid));
            case "fragments":
                return FormatterAPI.formatNumber(Rankup.getPlayerFragments(uuid));
            case "heads":
                return StringUtils.formatNumber(Rank.getPlayerHeads(uuid));
            case "discount":
                return String.format("%.2f", Prestigio.getPlayerDiscount(uuid, Rank.getPlayerPrestige(uuid)));
            case "next_rank_fragments_cost": {
                int currentRank = Rankup.getPlayerRank(uuid);
                int prestige = Rank.getPlayerPrestige(uuid);
                double discount = Prestigio.getPlayerDiscount(uuid, prestige);
                String nextRankKey = "ranks.rank" + (currentRank + 1);
                int fragmentCost = rankConfig.getInt(nextRankKey + ".prices.fragmentos.price", 0);
                int discountedFragmentCost = (int) Math.round(fragmentCost * (1 - discount / 100.0));
                return StringUtils.formatNumber(discountedFragmentCost);
            }
            case "rank_tag": {
                int currentRank = Rankup.getPlayerRank(uuid);
                String currentRankKey = "ranks.rank" + currentRank;
                return rankConfig.getString(currentRankKey + ".tag", "").replace('&', '§');
            }
            case "next_rank_money_cost": {
                int currentRank = Rankup.getPlayerRank(uuid);
                int prestige = Rank.getPlayerPrestige(uuid);
                double discount = Prestigio.getPlayerDiscount(uuid, prestige);
                String nextRankKey = "ranks.rank" + (currentRank + 1);
                int moneyCost = rankConfig.getInt(nextRankKey + ".prices.money.price", 0);
                int discountedMoneyCost = (int) Math.round(moneyCost * (1 - discount / 100.0));
                return StringUtils.formatNumber(discountedMoneyCost);
            }
            case "next_rank_tag": {
                int currentRank = Rankup.getPlayerRank(uuid);
                String nextRankKey = "ranks.rank" + (currentRank + 1);
                return rankConfig.getString(nextRankKey + ".tag", "").replace('&', '§');
            }
            case "next_rank_name": {
                int currentRank = Rankup.getPlayerRank(uuid);
                String nextRankKey = "ranks.rank" + (currentRank + 1);
                return rankConfig.getString(nextRankKey + ".name", "N/A");
            }
            case "autorankup": {
                try (PreparedStatement ps = (PreparedStatement) connection.prepareStatement(
                        "SELECT autorankup FROM player_settings WHERE uuid = ?")) {
                    ps.setString(1, uuid);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        return rs.getBoolean("autorankup") ? "Sim" : "Não";
                    }
                    return "Não";
                } catch (SQLException e) {
                    e.printStackTrace();
                    return "Erro";
                }
            }
            case "can_rankup": {
                int currentRank = Rankup.getPlayerRank(uuid);
                int fragments = Rankup.getPlayerFragments(uuid);
                double money = Main.get().getEconomy() != null ? Main.get().getEconomy().getBalance(player) : 0.0;
                int prestige = Rank.getPlayerPrestige(uuid);
                double discount = Prestigio.getPlayerDiscount(uuid, prestige);
                String nextRankKey = "ranks.rank" + (currentRank + 1);
                int fragmentCost = rankConfig.getInt(nextRankKey + ".prices.fragmentos.price", 0);
                int moneyCost = rankConfig.getInt(nextRankKey + ".prices.money.price", 0);
                int discountedFragmentCost = (int) Math.round(fragmentCost * (1 - discount / 100.0));
                int discountedMoneyCost = (int) Math.round(moneyCost * (1 - discount / 100.0));
                boolean hasFragmentProvider = rankConfig.contains(nextRankKey + ".prices.fragmentos");
                boolean hasMoneyProvider = rankConfig.contains(nextRankKey + ".prices.money");
                boolean canRankup = (!hasFragmentProvider || fragments >= discountedFragmentCost) &&
                                    (!hasMoneyProvider || money >= discountedMoneyCost) &&
                                    rankConfig.contains(nextRankKey);
                return canRankup ? "Sim" : "Não";
            }
            default:
                return null;
        }
    }

}
