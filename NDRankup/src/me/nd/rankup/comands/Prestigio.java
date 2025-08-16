package me.nd.rankup.comands;

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
import me.nd.rankup.plugin.SConfig;
import me.nd.rankup.utils.ItemBuilder;
import net.milkbowl.vault.economy.Economy;

public class Prestigio extends Commands {
    private static final SConfig rankConfig = Main.get().getConfig("rank");
    private static final SConfig mainConfig = Main.get().getConfig("menurankup");
    private static final Economy economy = Main.get().getEconomy();

    public Prestigio() {
        super("prestigio");
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando é apenas para jogadores!");
            return;
        }

        Player player = (Player) sender;

        // Verifica permissão
        if (!player.hasPermission("yrankup.prestigio")) {
            player.sendMessage("§cVocê não tem permissão para usar este comando!");
            return;
        }

        // Verifica se o jogador está no último rank
        int currentRank = Rankup.getPlayerRank(player.getUniqueId().toString());
        String nextRankKey = "ranks.rank" + (currentRank + 1);
        if (rankConfig.contains(nextRankKey)) {
            player.sendMessage("§cVocê precisa estar no último rank para prestigiar!");
            return;
        }

        // Abre o menu de confirmação de prestígio
        openPrestigeMenu(player);
    }

    public static void openPrestigeMenu(Player player) {
        String menuName = mainConfig.getString("prestige.name", "&8Confirmar Prestígio").replace('&', '§');
        int size = mainConfig.getInt("prestige.size", 27);
        Inventory inv = Bukkit.createInventory(null, size, menuName);

        // Obtém dados do jogador
        int currentRank = Rankup.getPlayerRank(player.getUniqueId().toString());
        int fragments = Rankup.getPlayerFragments(player.getUniqueId().toString());
        double money = economy != null ? economy.getBalance(player) : 0.0;
        int prestige = Rank.getPlayerPrestige(player.getUniqueId().toString());
        int nextPrestige = prestige + 1;
        String rankKey = "ranks.rank" + currentRank;
        String rankTag = rankConfig.getString(rankKey + ".tag", "").replace('&', '§');
        String rankName = rankConfig.getString(rankKey + ".name", "N/A");

        // Custo de prestígio
        int prestigeFragmentCost = mainConfig.getInt("prestige.cost.fragments", 0);
        int prestigeMoneyCost = mainConfig.getInt("prestige.cost.money", 0);
        boolean hasFragmentCost = prestigeFragmentCost > 0;
        boolean hasMoneyCost = prestigeMoneyCost > 0;
        boolean canPrestige = (!hasFragmentCost || fragments >= prestigeFragmentCost) &&
                             (!hasMoneyCost || money >= prestigeMoneyCost);

        // Item de confirmação
        ItemStack confirmItem = new ItemBuilder(Material.EMERALD)
                .setName(mainConfig.getString("prestige.items.confirm.name", "&aConfirmar Prestígio").replace('&', '§'))
                .setLore(generatePrestigeLore(rankTag, rankName, prestige, nextPrestige, fragments, money, prestigeFragmentCost, prestigeMoneyCost, hasFragmentCost, hasMoneyCost, canPrestige))
                .toItemStack();
        inv.setItem(mainConfig.getInt("prestige.items.confirm.slot", 11), confirmItem);

        // Item de cancelamento
        ItemStack cancelItem = new ItemBuilder(Material.REDSTONE)
                .setName(mainConfig.getString("prestige.items.cancel.name", "&cCancelar").replace('&', '§'))
                .setLore(mainConfig.getStringList("prestige.items.cancel.lore"))
                .toItemStack();
        inv.setItem(mainConfig.getInt("prestige.items.cancel.slot", 15), cancelItem);

        player.openInventory(inv);
    }

    private static List<String> generatePrestigeLore(String rankTag, String rankName, int prestige, int nextPrestige, int fragments, double money, int fragmentCost, int moneyCost, boolean hasFragmentCost, boolean hasMoneyCost, boolean canPrestige) {
        List<String> lore = new ArrayList<>();
        lore.add("§7Seu rank: " + rankName + " §7(" + rankTag + ")");
        lore.add("");
        lore.add("§7Prestígio: §e" + prestige);
        lore.add("§7Próx. Prestígio: §e" + nextPrestige);
        lore.add("");
        if (hasFragmentCost) {
            lore.add("§7Fragmentos: §e" + FormatterAPI.formatNumber(fragmentCost));
        }
        if (hasMoneyCost) {
            lore.add("§7Coins: §e" + FormatterAPI.formatNumber(moneyCost));
        }
        lore.add("");
        lore.add(canPrestige ? "§aClique para prestigiar!" : "§cRecursos insuficientes!");
        return lore;
    }

    public static void performPrestige(Player player) {
        String uuid = player.getUniqueId().toString();
        int prestige = Rank.getPlayerPrestige(uuid);
        int fragments = Rankup.getPlayerFragments(uuid);
        double money = economy != null ? economy.getBalance(player) : 0.0;
        int prestigeFragmentCost = mainConfig.getInt("prestige.cost.fragments", 0);
        int prestigeMoneyCost = mainConfig.getInt("prestige.cost.money", 0);
        boolean hasFragmentCost = prestigeFragmentCost > 0;
        boolean hasMoneyCost = prestigeMoneyCost > 0;

        // Verifica recursos
        if ((hasFragmentCost && fragments < prestigeFragmentCost) || (hasMoneyCost && money < prestigeMoneyCost)) {
            player.sendMessage("§cVocê não tem recursos suficientes para prestigiar!");
            return;
        }

        // Deduz recursos
        if (hasFragmentCost) {
            Rankup.setPlayerFragments(uuid, fragments - prestigeFragmentCost);
        }
        if (hasMoneyCost && economy != null) {
            economy.withdrawPlayer(player, prestigeMoneyCost);
        }

        // Reseta o rank e incrementa o prestígio
        Rankup.setPlayerRank(uuid, 1);
        Rank.setPlayerPrestige(uuid, prestige + 1);

        // Executa comandos de prestígio
        List<String> commands = mainConfig.getStringList("prestige.commands");
        for (String command : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.getName()));
        }

        // Envia mensagens globais
        String chatMsg = mainConfig.getString("prestige.messages.chat", "");
        if (!chatMsg.isEmpty()) {
            Bukkit.broadcastMessage(chatMsg.replace("{player}", player.getName()).replace("{prestige}", String.valueOf(prestige + 1)).replace('&', '§'));
        }
        String actionBarMsg = mainConfig.getString("prestige.messages.actionbar", "");
        if (!actionBarMsg.isEmpty()) {
            ActionbarAPI.sendActionBarMessage(player, actionBarMsg.replace("{player}", player.getName()).replace("{prestige}", String.valueOf(prestige + 1)).replace('&', '§'));
        }
        String titleMsg = mainConfig.getString("prestige.messages.title", "");
        if (!titleMsg.isEmpty()) {
            String[] titleParts = titleMsg.split("\n");
            String title = titleParts[0].replace("{player}", player.getName()).replace("{prestige}", String.valueOf(prestige + 1)).replace('&', '§');
            String subtitle = titleParts.length > 1 ? titleParts[1].replace("{player}", player.getName()).replace("{prestige}", String.valueOf(prestige + 1)).replace('&', '§') : "";
            TitleAPI.sendTitle(player, 20, 60, 20, title, subtitle);
        }

        // Envia mensagens privadas
        String privateChatMsg = mainConfig.getString("prestige.private-messages.chat", "");
        if (!privateChatMsg.isEmpty()) {
            player.sendMessage(privateChatMsg.replace("{player}", player.getName()).replace("{prestige}", String.valueOf(prestige + 1)).replace('&', '§'));
        }
        String privateActionBarMsg = mainConfig.getString("prestige.private-messages.actionbar", "");
        if (!privateActionBarMsg.isEmpty()) {
            ActionbarAPI.sendActionBarMessage(player, privateActionBarMsg.replace("{player}", player.getName()).replace("{prestige}", String.valueOf(prestige + 1)).replace('&', '§'));
        }
        String privateTitleMsg = mainConfig.getString("prestige.private-messages.title", "");
        if (!privateTitleMsg.isEmpty()) {
            String[] privateTitleParts = privateTitleMsg.split("\n");
            TitleAPI.sendTitle(player, 20, 60, 20, privateTitleParts[0].replace("{player}", player.getName()).replace("{prestige}", String.valueOf(prestige + 1)).replace('&', '§'),
                    privateTitleParts.length > 1 ? privateTitleParts[1].replace("{player}", player.getName()).replace("{prestige}", String.valueOf(prestige + 1)).replace('&', '§') : "");
        }

        player.sendMessage("§aVocê prestigiou para o nível " + (prestige + 1) + " e seu rank foi resetado para 1!");
    }

    public static double getPlayerDiscount(String uuid, int prestige) {
        double baseDiscount = mainConfig.getDouble("prestige.discount-per-level", 5.0); // 5% por nível de prestígio
        return prestige * baseDiscount;
    }
}