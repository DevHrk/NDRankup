package me.nd.rankup.comands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import me.nd.rankup.Main;
import me.nd.rankup.api.FormatterAPI;
import me.nd.rankup.dados.SQlite;
import me.nd.rankup.plugin.SConfig;
import me.nd.rankup.utils.ItemBuilder;
import net.milkbowl.vault.economy.Economy;

public class Rank extends Commands {
    private static final Connection connection = SQlite.getConnection();
    private static final SConfig mainConfig = Main.get().getConfig("menurankup");
    private static final SConfig rankConfig = Main.get().getConfig("rank");
    private static final Economy economy = Main.get().getEconomy();
    private static final List<Integer> topSlots = Arrays.asList(10, 11, 12, 13, 14, 15, 16);
    private static final int ITEMS_PER_PAGE = topSlots.size();

    public Rank() {
        super("rank");
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando é apenas para jogadores!");
            return;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("yrankup.use")) {
            player.sendMessage("§cVocê não tem permissão para usar este comando!");
            return;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("top") && player.hasPermission("yrankup.top")) {
                openTopMenu(player, 1, "rank");
                return;
            } else if (player.hasPermission("yrankup.look")) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null) {
                    openProfileMenu(player, target);
                } else {
                    player.sendMessage("§cJogador não encontrado!");
                }
                return;
            }
        }

        openMainMenu(player);
    }

    @SuppressWarnings("unused")
	public static void openMainMenu(Player player) {
        String menuName = mainConfig.getString("main.name", "&8Rank - Principal").replace('&', '§');
        int size = mainConfig.getInt("main.size", 27);
        Inventory inv = Bukkit.createInventory(null, size, menuName);

        int currentRank = Rankup.getPlayerRank(player.getUniqueId().toString());
        int fragments = Rankup.getPlayerFragments(player.getUniqueId().toString());
        double money = economy != null ? economy.getBalance(player) : 0.0;
        int prestige = getPlayerPrestige(player.getUniqueId().toString());
        double discount = Prestigio.getPlayerDiscount(player.getUniqueId().toString(), prestige);
        String rankKey = "ranks.rank" + currentRank;
        String nextRankKey = "ranks.rank" + (currentRank + 1);
        String rankTag = rankConfig.getString(rankKey + ".tag", "").replace('&', '§');
        String rankName = rankConfig.getString(rankKey + ".name", "N/A");
        String nextRankTag = rankConfig.getString(nextRankKey + ".tag", "").replace('&', '§');
        String nextRankName = rankConfig.getString(nextRankKey + ".name", "N/A");
        int nextRankCostFragments = Rankup.getNextRankCost(currentRank);
        int nextRankCostMoney = rankConfig.getInt(nextRankKey + ".prices.money.price", 0);
        int discountedNextRankCostFragments = (int) Math.round(nextRankCostFragments * (1 - discount / 100.0));
        int discountedNextRankCostMoney = (int) Math.round(nextRankCostMoney * (1 - discount / 100.0));
        int nextPrestige = prestige + 1;
        double prestigePercentage = calculatePrestigePercentage(prestige);
        boolean autoRankup = isAutoRankupEnabled(player.getUniqueId().toString());
        int heads = getPlayerHeads(player.getUniqueId().toString());
        double bonus = getPlayerBonus(player.getUniqueId().toString());
        String progressBar = generateProgressBar((int) money, nextRankCostMoney);
        double percentage = nextRankCostMoney > 0 ? (double) money / nextRankCostMoney * 100 : 0;

        int profileSlot = mainConfig.getInt("main.items.profile-slot", 10);
        ItemStack profileItem = new ItemBuilder(Material.SKULL_ITEM)
                .setDurability((short) 3)
                .setName(mainConfig.getString("main.items.profile.name", "&eSeu Perfil").replace('&', '§'))
                .setLore(replacePlaceholders(mainConfig.getStringList("main.items.profile.lore"), player, rankTag, rankName, nextRankTag, nextRankName, fragments, money, prestige, nextPrestige, prestigePercentage, autoRankup, heads, bonus, discount, progressBar, percentage))
                .toItemStack();
        inv.setItem(profileSlot, profileItem);

        int rankupSlot = mainConfig.getInt("main.items.rankup-slot", 12);
        boolean canRankup = nextRankCostFragments != -1 && (!rankConfig.contains(nextRankKey + ".prices.money") || money >= discountedNextRankCostMoney);
        String rankupKey = canRankup ? "main.items.rankup-can" : "main.items.rankup-cant";
        ItemStack rankupItem = new ItemBuilder(Material.DIAMOND)
                .setName(mainConfig.getString(rankupKey + ".name", "&aRankUP").replace('&', '§'))
                .setLore(replacePlaceholders(mainConfig.getStringList(rankupKey + ".lore"), player, rankTag, rankName, nextRankTag, nextRankName, fragments, money, prestige, nextPrestige, prestigePercentage, autoRankup, heads, bonus, discount, progressBar, percentage))
                .toItemStack();
        inv.setItem(rankupSlot, rankupItem);

        int ranksSlot = mainConfig.getInt("main.items.ranks-slot", 13);
        ItemStack ranksItem = new ItemBuilder(Material.BOOK)
                .setName(mainConfig.getString("main.items.ranks.name", "&aRanks").replace('&', '§'))
                .setLore(replacePlaceholders(mainConfig.getStringList("main.items.ranks.lore"), player, rankTag, rankName, nextRankTag, nextRankName, fragments, money, prestige, nextPrestige, prestigePercentage, autoRankup, heads, bonus, discount, progressBar, percentage))
                .toItemStack();
        inv.setItem(ranksSlot, ranksItem);

        int headsSlot = mainConfig.getInt("main.items.heads-slot", 14);
        ItemStack headsItem = new ItemBuilder(Material.SKULL_ITEM)
                .setDurability((short) 3)
                .setName(mainConfig.getString("main.items.heads.name", "&aHeads").replace('&', '§'))
                .setLore(replacePlaceholders(mainConfig.getStringList("main.items.heads.lore"), player, rankTag, rankName, nextRankTag, nextRankName, fragments, money, prestige, nextPrestige, prestigePercentage, autoRankup, heads, bonus, discount, progressBar, percentage))
                .toItemStack();
        inv.setItem(headsSlot, headsItem);

        int prestigeSlot = mainConfig.getInt("main.items.prestige-slot", 15);
        ItemStack prestigeItem = new ItemBuilder(Material.EMERALD)
                .setName(mainConfig.getString("main.items.prestige.name", "&aPrestígio").replace('&', '§'))
                .setLore(replacePlaceholders(mainConfig.getStringList("main.items.prestige.lore"), player, rankTag, rankName, nextRankTag, nextRankName, fragments, money, prestige, nextPrestige, prestigePercentage, autoRankup, heads, bonus, discount, progressBar, percentage))
                .toItemStack();
        inv.setItem(prestigeSlot, prestigeItem);

        int topSlot = mainConfig.getInt("main.items.top-slot", 16);
        ItemStack topItem = new ItemBuilder(Material.GOLD_INGOT)
                .setName(mainConfig.getString("main.items.top.name", "&aTOP Jogadores").replace('&', '§'))
                .setLore(replacePlaceholders(mainConfig.getStringList("main.items.top.lore"), player, rankTag, rankName, nextRankTag, nextRankName, fragments, money, prestige, nextPrestige, prestigePercentage, autoRankup, heads, bonus, discount, progressBar, percentage))
                .toItemStack();
        inv.setItem(topSlot, topItem);

        player.openInventory(inv);
    }

    @SuppressWarnings("unused")
	public static void openProfileMenu(Player viewer, Player target) {
        String menuName = mainConfig.getString("profile.name", "&8Perfil de {player}").replace("{player}", target.getName()).replace('&', '§');
        int size = mainConfig.getInt("profile.size", 27);
        Inventory inv = Bukkit.createInventory(null, size, menuName);

        int currentRank = Rankup.getPlayerRank(target.getUniqueId().toString());
        int fragments = Rankup.getPlayerFragments(target.getUniqueId().toString());
        double money = economy != null ? economy.getBalance(target) : 0.0;
        int prestige = getPlayerPrestige(target.getUniqueId().toString());
        double discount = Prestigio.getPlayerDiscount(target.getUniqueId().toString(), prestige);
        String rankKey = "ranks.rank" + currentRank;
        String nextRankKey = "ranks.rank" + (currentRank + 1);
        String rankTag = rankConfig.getString(rankKey + ".tag", "").replace('&', '§');
        String rankName = rankConfig.getString(rankKey + ".name", "N/A");
        String nextRankTag = rankConfig.getString(nextRankKey + ".tag", "").replace('&', '§');
        String nextRankName = rankConfig.getString(nextRankKey + ".name", "N/A");
        int nextRankCostFragments = Rankup.getNextRankCost(currentRank);
        int nextRankCostMoney = rankConfig.getInt(nextRankKey + ".prices.money.price", 0);
        int discountedNextRankCostFragments = (int) Math.round(nextRankCostFragments * (1 - discount / 100.0));
        int discountedNextRankCostMoney = (int) Math.round(nextRankCostMoney * (1 - discount / 100.0));
        int nextPrestige = prestige + 1;
        double prestigePercentage = calculatePrestigePercentage(prestige);
        boolean autoRankup = isAutoRankupEnabled(target.getUniqueId().toString());
        int heads = getPlayerHeads(target.getUniqueId().toString());
        double bonus = getPlayerBonus(target.getUniqueId().toString());
        String progressBar = generateProgressBar(fragments, nextRankCostFragments);
        double percentage = nextRankCostFragments > 0 ? (double) fragments / nextRankCostFragments * 100 : 0;

        int profileSlot = mainConfig.getInt("profile.items.profile-slot", 13);
        ItemStack profileItem = new ItemBuilder(Material.SKULL_ITEM)
                .setDurability((short) 3)
                .setName(mainConfig.getString("profile.items.profile.name", "&ePerfil de {player}").replace("{player}", target.getName()).replace('&', '§'))
                .setLore(replacePlaceholders(mainConfig.getStringList("profile.items.profile.lore"), target, rankTag, rankName, nextRankTag, nextRankName, fragments, money, prestige, nextPrestige, prestigePercentage, autoRankup, heads, bonus, discount, progressBar, percentage))
                .toItemStack();
        SkullMeta meta = (SkullMeta) profileItem.getItemMeta();
        meta.setOwner(target.getName());
        profileItem.setItemMeta(meta);
        inv.setItem(profileSlot, profileItem);

        viewer.openInventory(inv);
    }

    public static void openTopMenu(Player player, int page, String type) {
        String menuName = mainConfig.getString("top.name", "&8Rank - TOP").replace('&', '§');
        int size = mainConfig.getInt("top.size", 36);
        Inventory inv = Bukkit.createInventory(null, size, menuName);

        List<PlayerData> topPlayers = getTopPlayers(type);
        int totalPages = (int) Math.ceil((double) topPlayers.size() / ITEMS_PER_PAGE);
        page = Math.max(1, Math.min(page, totalPages));

        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, topPlayers.size());

        for (int i = startIndex; i < endIndex; i++) {
            PlayerData data = topPlayers.get(i);
            int slot = topSlots.get(i % ITEMS_PER_PAGE);
            String itemKey = "top.items." + type;
            String materialStr = mainConfig.getString(itemKey + ".material", "SKULL_ITEM");
            String name = mainConfig.getString(itemKey + ".name", "&7{player}").replace("{player}", data.getName()).replace('&', '§');
            List<String> lore = mainConfig.getStringList(itemKey + ".lore");
            List<String> formattedLore = new ArrayList<>();
            for (String line : lore) {
                line = line.replace("{player}", data.getName())
                          .replace("{rank}", String.valueOf(data.getRank()))
                          .replace("{prestige}", String.valueOf(data.getPrestige()))
                          .replace("{amount}", FormatterAPI.formatNumber(data.getFragments()))
                          .replace("{pos}", String.valueOf(i + 1))
                          .replace('&', '§');
                formattedLore.add(line);
            }

            ItemStack item = new ItemBuilder(Material.matchMaterial(materialStr) != null ? Material.matchMaterial(materialStr) : Material.SKULL_ITEM)
                    .setDurability((short) 3)
                    .setName(name)
                    .setLore(formattedLore)
                    .toItemStack();
            if (item.getType() == Material.SKULL_ITEM) {
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                meta.setOwner(data.getName());
                item.setItemMeta(meta);
            }
            inv.setItem(slot, item);
        }

        for (int i = 0; i < size; i++) {
            if (!topSlots.contains(i) && i != mainConfig.getInt("top.previous-slot", 9) &&
                i != mainConfig.getInt("top.next-slot", 17) && i != mainConfig.getInt("top.back-slot", 30) &&
                i != mainConfig.getInt("top.selector.slot", 31)) {
                ItemStack border = new ItemBuilder(Material.STAINED_GLASS_PANE)
                        .setDurability((short) 7)
                        .setName("§7")
                        .toItemStack();
                inv.setItem(i, border);
            }
        }

        if (page > 1) {
            ItemStack previous = new ItemBuilder(Material.ARROW)
                    .setName("§ePágina Anterior")
                    .setLore("§7Clique para ir à página " + (page - 1))
                    .toItemStack();
            inv.setItem(mainConfig.getInt("top.previous-slot", 9), previous);
        }

        if (page < totalPages) {
            ItemStack next = new ItemBuilder(Material.ARROW)
                    .setName("§ePróxima Página")
                    .setLore("§7Clique para ir à página " + (page + 1))
                    .toItemStack();
            inv.setItem(mainConfig.getInt("top.next-slot", 17), next);
        }

        ItemStack back = new ItemBuilder(Material.BARRIER)
                .setName("§cVoltar")
                .setLore("§7Clique para voltar ao menu principal")
                .toItemStack();
        inv.setItem(mainConfig.getInt("top.back-slot", 30), back);

        ItemStack selector = new ItemBuilder(Material.PAPER)
                .setName(mainConfig.getString("top.selector.name", "&aSeletor do TOP").replace('&', '§'))
                .setLore(generateSelectorLore(type))
                .toItemStack();
        inv.setItem(mainConfig.getInt("top.selector.slot", 31), selector);

        player.openInventory(inv);
    }

    private static List<String> generateSelectorLore(String currentType) {
        List<String> lore = new ArrayList<>();
        String seeingFormat = mainConfig.getString("top.selector.formats.seeing", " &f• &a{name}");
        String selectFormat = mainConfig.getString("top.selector.formats.select", " &f• &7{name}");
        for (String type : new String[]{"rank", "prestige", "coin"}) {
            if (mainConfig.getBoolean("top.selector.types." + type + ".enabled", false)) {
                String name = mainConfig.getString("top.selector.types." + type + ".name", type);
                String format = type.equals(currentType) ? seeingFormat : selectFormat;
                lore.add(format.replace("{name}", name).replace('&', '§'));
            }
        }
        return lore;
    }

    private static List<PlayerData> getTopPlayers(String type) {
        List<PlayerData> players = new ArrayList<>();
        String query;
        String column;
        switch (type) {
            case "rank":
                query = "SELECT uuid, rank FROM player_ranks ORDER BY rank DESC LIMIT 100";
                column = "rank";
                break;
            case "prestige":
                query = "SELECT uuid, prestige FROM player_prestige ORDER BY prestige DESC LIMIT 100";
                column = "prestige";
                break;
            case "coin":
                query = "SELECT uuid, fragmento FROM player_fragmento ORDER BY fragmento DESC LIMIT 100";
                column = "fragmento";
                break;
            default:
                return players;
        }

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String uuid = rs.getString("uuid");
                String name = Bukkit.getOfflinePlayer(java.util.UUID.fromString(uuid)).getName();
                if (name == null) {
                    name = getNameFromUUID(uuid); // Fallback to database lookup
                    if (name == null) continue;
                }
                int value = rs.getInt(column);
                int rank = type.equals("rank") ? value : Rankup.getPlayerRank(uuid);
                int prestige = type.equals("prestige") ? value : getPlayerPrestige(uuid);
                int fragments = type.equals("coin") ? value : Rankup.getPlayerFragments(uuid);
                players.add(new PlayerData(uuid, name, rank, prestige, fragments));
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDRankup] Erro ao consultar top jogadores: " + type);
            e.printStackTrace();
        }
        return players;
    }

    private static String getNameFromUUID(String uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT name FROM player_fragmento WHERE uuid = ?")) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDRankup] Erro ao consultar nome para UUID: " + uuid);
            e.printStackTrace();
        }
        return null;
    }

    private static List<String> replacePlaceholders(List<String> lore, Player player, String rankTag, String rankName, String nextRankTag, String nextRankName, int fragments, double money, int prestige, int nextPrestige, double prestigePercentage, boolean autoRankup, int heads, double bonus, double discount, String progressBar, double percentage) {
        List<String> newLore = new ArrayList<>();
        for (String line : lore) {
            newLore.add(line.replace("{player}", player.getName())
                    .replace("{autorankup}", autoRankup ? "Ativado" : "Desativado")
                    .replace("{coin}", FormatterAPI.formatNumber(fragments))
                    .replace("{money}", FormatterAPI.formatNumber(money))
                    .replace("{tag}", rankTag)
                    .replace("{name}", rankName)
                    .replace("{next_tag}", nextRankTag)
                    .replace("{next_name}", nextRankName)
                    .replace("{prestige}", String.valueOf(prestige))
                    .replace("{next_prestige}", String.valueOf(nextPrestige))
                    .replace("{prestige_percentage}", String.format("%.2f", prestigePercentage))
                    .replace("{amount}", String.valueOf(heads))
                    .replace("{bonus}", String.format("%.2f", bonus))
                    .replace("{discount}", String.format("%.2f", discount))
                    .replace("{progressbar}", progressBar)
                    .replace("{percentage}", String.format("%.2f%%", percentage))
                    .replace('&', '§'));
        }
        return newLore;
    }

    private static String generateProgressBar(int fragments, int nextRankCost) {
        if (nextRankCost <= 0) return "██████████";
        int progress = Math.min((fragments * 10) / nextRankCost, 10);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            bar.append(i < progress ? "§a█" : "§c█");
        }
        return bar.toString();
    }

    public static int getPlayerPrestige(String uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT prestige FROM player_prestige WHERE uuid = ?")) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("prestige");
            } else {
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO player_prestige (uuid, prestige) VALUES (?, 0)")) {
                    insert.setString(1, uuid);
                    insert.executeUpdate();
                }
                return 0;
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDRankup] Erro ao consultar prestígio para UUID: " + uuid);
            e.printStackTrace();
            return 0;
        }
    }

    public static void setPlayerPrestige(String uuid, int prestige) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE player_prestige SET prestige = ? WHERE uuid = ?")) {
            ps.setInt(1, prestige);
            ps.setString(2, uuid);
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDRankup] Erro ao atualizar prestígio para UUID: " + uuid);
            e.printStackTrace();
        }
    }

    public static boolean isAutoRankupEnabled(String uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT autorankup FROM player_settings WHERE uuid = ?")) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("autorankup");
            } else {
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO player_settings (uuid, autorankup) VALUES (?, ?)")) {
                    insert.setString(1, uuid);
                    insert.setBoolean(2, false);
                    insert.executeUpdate();
                }
                return false;
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDRankup] Erro ao consultar autorankup para UUID: " + uuid);
            e.printStackTrace();
            return false;
        }
    }

    public static void setAutoRankup(String uuid, boolean enabled) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE player_settings SET autorankup = ? WHERE uuid = ?")) {
            ps.setBoolean(1, enabled);
            ps.setString(2, uuid);
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDRankup] Erro ao atualizar autorankup para UUID: " + uuid);
            e.printStackTrace();
        }
    }

    public static int getPlayerHeads(String uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT heads FROM player_heads WHERE uuid = ?")) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("heads");
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDRankup] Erro ao consultar heads para UUID: " + uuid);
            e.printStackTrace();
        }
        return 0;
    }

    public static double getPlayerBonus(String uuid) {
        return 0.0; // Placeholder
    }

    public static double getPlayerDiscount(String uuid) {
        return Prestigio.getPlayerDiscount(uuid, getPlayerPrestige(uuid));
    }

    public static double calculatePrestigePercentage(int prestige) {
        return 0.0; // Placeholder
    }

    private static class PlayerData {
        private final String uuid;
        private final String name;
        private final int rank;
        private final int prestige;
        private final int fragments;

        public PlayerData(String uuid, String name, int rank, int prestige, int fragments) {
            this.uuid = uuid;
            this.name = name;
            this.rank = rank;
            this.prestige = prestige;
            this.fragments = fragments;
        }

        @SuppressWarnings("unused")
		public String getUuid() {
            return uuid;
        }

        public String getName() {
            return name;
        }

        public int getRank() {
            return rank;
        }

        public int getPrestige() {
            return prestige;
        }

        public int getFragments() {
            return fragments;
        }
    }
}