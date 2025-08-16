package me.nd.rankup.listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerListPingEvent;

import me.nd.rankup.Main;
import me.nd.rankup.api.LocationsAPI;
import me.nd.rankup.comands.Prestigio;
import me.nd.rankup.comands.Rank;
import me.nd.rankup.comands.Ranks;
import me.nd.rankup.comands.Rankup;
import me.nd.rankup.dados.SQlite;
import me.nd.rankup.plugin.SConfig;

public class RankupListener implements Listener {
    private static final SConfig rankConfig = Main.get().getConfig("rank");
    private static final SConfig mainConfig = Main.get().getConfig("menurankup");
    private static final Connection connection = SQlite.getConnection();
    private final Map<String, MenuState> playerMenuData = new HashMap<>();

    private static class MenuState {
        int page;
        String type;

        MenuState(int page, String type) {
            this.page = page;
            this.type = type;
        }
    }
    
    @EventHandler
    public void join(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        String uuid = player.getUniqueId().toString();
        
        // Teleport player to spawn
        player.teleport(LocationsAPI.spawn);
        
        // Check if player has a rank entry
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT rank FROM player_ranks WHERE uuid = ?")) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                // Player doesn't have a rank entry, set rank to 1
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO player_ranks (uuid, rank) VALUES (?, ?)")) {
                    insert.setString(1, uuid);
                    insert.setInt(2, 1);
                    insert.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
	  @EventHandler(priority = EventPriority.MONITOR)
	  public void ServerMotd(ServerListPingEvent evt) {
		  SConfig c = Main.get().getConfig("Motd");
		  evt.setMotd(c.getString("Motd").replace("\\n", "\n").replace("&", "§"));
	  }
    
    @EventHandler
    public void onRankupMenuClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Menu de Rankup")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot != 13) return; // Apenas o item central (slot 13) é clicável

        int currentRank = Rankup.getPlayerRank(player.getUniqueId().toString());
        int prestige = Rank.getPlayerPrestige(player.getUniqueId().toString());
        double discount = Prestigio.getPlayerDiscount(player.getUniqueId().toString(), prestige);
        String nextRankKey = "ranks.rank" + (currentRank + 1);
        int fragmentCost = rankConfig.getInt(nextRankKey + ".prices.fragmentos.price", 0);
        int moneyCost = rankConfig.getInt(nextRankKey + ".prices.money.price", 0);
        int discountedFragmentCost = (int) Math.round(fragmentCost * (1 - discount / 100.0));
        int discountedMoneyCost = (int) Math.round(moneyCost * (1 - discount / 100.0));
        boolean hasFragmentProvider = rankConfig.contains(nextRankKey + ".prices.fragmentos");
        boolean hasMoneyProvider = rankConfig.contains(nextRankKey + ".prices.money");
        int fragments = Rankup.getPlayerFragments(player.getUniqueId().toString());
        double money = Main.get().getEconomy() != null ? Main.get().getEconomy().getBalance(player) : 0.0;
        boolean canRankup = (!hasFragmentProvider || fragments >= discountedFragmentCost) &&
                            (!hasMoneyProvider || money >= discountedMoneyCost) &&
                            rankConfig.contains(nextRankKey);

        if (canRankup) {
            Rankup.performRankupActions(player, currentRank + 1);
            Rankup.setPlayerRank(player.getUniqueId().toString(), currentRank + 1);
            player.closeInventory();
            player.sendMessage("§aRank-up realizado com sucesso!");
        } else {
            player.sendMessage("§cVocê não tem recursos suficientes para subir de rank!");
        }
    }

    @EventHandler
    public void onRanksMenuClick(InventoryClickEvent event) {
        String menuName = mainConfig.getString("ranks.name", "&8Rank - Ranks").replace('&', '§');
        if (!event.getView().getTitle().equals(menuName)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        int previousSlot = mainConfig.getInt("ranks.previous-slot", 18);
        int nextSlot = mainConfig.getInt("ranks.next-slot", 26);
        int backSlot = mainConfig.getInt("ranks.back-slot", 49);

        // Determina a página atual (simplificado, idealmente armazenar em cache)
        int currentPage = 1;

        if (slot == previousSlot) {
            Ranks.openRanksMenu(player, currentPage - 1);
        } else if (slot == nextSlot) {
            Ranks.openRanksMenu(player, currentPage + 1);
        } else if (slot == backSlot) {
            Rank.openMainMenu(player);
        }
    }

    @EventHandler
    public void onMainOrProfileMenuClick(InventoryClickEvent event) {
        String mainMenuName = mainConfig.getString("main.name", "&8Rank - Principal").replace('&', '§');
        String profileMenuName = mainConfig.getString("profile.name", "&8Perfil de {player}").replace("{player}", ".*").replace('&', '§');

        if (event.getView().getTitle().equals(mainMenuName) || event.getView().getTitle().matches(profileMenuName)) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();

            if (!event.getView().getTitle().equals(mainMenuName)) return; // Apenas o menu principal é interativo

            int profileSlot = mainConfig.getInt("main.items.profile-slot", 10);
            int rankupSlot = mainConfig.getInt("main.items.rankup-slot", 12);
            int ranksSlot = mainConfig.getInt("main.items.ranks-slot", 13);
            int headsSlot = mainConfig.getInt("main.items.heads-slot", 14);
            int prestigeSlot = mainConfig.getInt("main.items.prestige-slot", 15);
            int topSlot = mainConfig.getInt("main.items.top-slot", 16);

            if (slot == profileSlot) {
                Rank.openMainMenu(player);
            } else if (slot == rankupSlot) {
                if (Rankup.getNextRankCost(Rankup.getPlayerRank(player.getUniqueId().toString())) != -1) {
                    Rankup.openRankupMenu(player);
                } else {
                    player.sendMessage("§cVocê já atingiu o último rank!");
                }
            } else if (slot == ranksSlot) {
                player.chat("/ranks");
            } else if (slot == headsSlot) {
                player.sendMessage("§cHeads desativado!");
            } else if (slot == prestigeSlot) {
                player.chat("/prestigio");
            } else if (slot == topSlot) {
            	player.chat("/rank top");
            }
        }
    }

    @EventHandler
    public void onPrestigeMenuClick(InventoryClickEvent event) {
        String menuName = mainConfig.getString("prestige.name", "&8Confirmar Prestígio").replace('&', '§');
        if (!event.getView().getTitle().equals(menuName)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        int confirmSlot = mainConfig.getInt("prestige.items.confirm.slot", 11);
        int cancelSlot = mainConfig.getInt("prestige.items.cancel.slot", 15);

        if (slot == confirmSlot) {
            Prestigio.performPrestige(player);
            player.closeInventory();
        } else if (slot == cancelSlot) {
            player.closeInventory();
            player.sendMessage("§cPrestígio cancelado.");
        }
    }
    
    @EventHandler
    public void onTopMenuClick(InventoryClickEvent event) {
        String menuName = mainConfig.getString("top.name", "&8Rank - TOP").replace('&', '§');
        if (!event.getView().getTitle().equals(menuName)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        int previousSlot = mainConfig.getInt("top.previous-slot", 9);
        int nextSlot = mainConfig.getInt("top.next-slot", 17);
        int backSlot = mainConfig.getInt("top.back-slot", 30);
        int selectorSlot = mainConfig.getInt("top.selector.slot", 31);

        String playerUUID = player.getUniqueId().toString();
        MenuState state = playerMenuData.getOrDefault(playerUUID, new MenuState(1, "rank"));
        int currentPage = state.page;
        String currentType = state.type;

        if (slot == previousSlot) {
            currentPage = Math.max(1, currentPage - 1);
            playerMenuData.put(playerUUID, new MenuState(currentPage, currentType));
            Rank.openTopMenu(player, currentPage, currentType);
        } else if (slot == nextSlot) {
            currentPage++;
            playerMenuData.put(playerUUID, new MenuState(currentPage, currentType));
            Rank.openTopMenu(player, currentPage, currentType);
        } else if (slot == backSlot) {
            playerMenuData.remove(playerUUID);
            Rank.openMainMenu(player);
        } else if (slot == selectorSlot) {
            String newType = switchType(currentType);
            playerMenuData.put(playerUUID, new MenuState(1, newType));
            Rank.openTopMenu(player, 1, newType);
        }
    }

    private String switchType(String currentType) {
        List<String> enabledTypes = Arrays.asList("rank", "prestige", "coin").stream()
                .filter(type -> mainConfig.getBoolean("top.selector.types." + type + ".enabled", false))
                .collect(Collectors.toList());

        if (enabledTypes.isEmpty()) {
            return "rank"; // Fallback if no types are enabled
        }

        int currentIndex = enabledTypes.indexOf(currentType);
        if (currentIndex == -1) {
            return enabledTypes.get(0); // If current type is invalid, start with the first enabled type
        }

        int nextIndex = (currentIndex + 1) % enabledTypes.size();
        return enabledTypes.get(nextIndex);
    }
}