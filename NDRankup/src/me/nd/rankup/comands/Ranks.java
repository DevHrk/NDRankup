package me.nd.rankup.comands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.nd.rankup.Main;
import me.nd.rankup.api.FormatterAPI;
import me.nd.rankup.plugin.SConfig;
import me.nd.rankup.utils.ItemBuilder;
import net.milkbowl.vault.economy.Economy;

public class Ranks extends Commands {
    private static final SConfig ranksConfig = Main.get().getConfig("menurankup");
    private static final SConfig rankConfig = Main.get().getConfig("rank");
    private static final Economy economy = Main.get().getEconomy();
    private static final List<Integer> rankSlots = Arrays.asList(11, 12, 13, 14, 15, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);
    private static final int ITEMS_PER_PAGE = rankSlots.size();

    public Ranks() {
        super("ranks");
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando é apenas para jogadores!");
            return;
        }

        Player player = (Player) sender;

        // Verifica permissão
        if (!player.hasPermission("yrankup.ranks")) {
            player.sendMessage("§cVocê não tem permissão para usar este comando!");
            return;
        }

        // Abre o menu de ranks na primeira página
        openRanksMenu(player, 1);
    }

    public static void openRanksMenu(Player player, int page) {
        // Configurações do menu
        String menuName = ranksConfig.getString("ranks.name", "&8Rank - Ranks").replace('&', '§');
        int size = ranksConfig.getInt("ranks.size", 54);
        Inventory inv = Bukkit.createInventory(null, size, menuName);

        // Obtém dados do jogador
        int currentRank = Rankup.getPlayerRank(player.getUniqueId().toString());
        int fragments = Rankup.getPlayerFragments(player.getUniqueId().toString());
        int heads = Rank.getPlayerHeads(player.getUniqueId().toString());
        double money = economy != null ? economy.getBalance(player) : 0.0;
        int prestige = Rank.getPlayerPrestige(player.getUniqueId().toString());
        double discount = Prestigio.getPlayerDiscount(player.getUniqueId().toString(), prestige);

        // Lista de ranks
        List<String> rankKeys = new ArrayList<>(rankConfig.getSection("ranks").getKeys(false));
        int totalPages = (int) Math.ceil((double) rankKeys.size() / ITEMS_PER_PAGE);
        page = Math.max(1, Math.min(page, totalPages));

        // Calcula os ranks a serem exibidos na página atual
        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, rankKeys.size());

        // Preenche o inventário com itens de ranks
        for (int i = startIndex; i < endIndex; i++) {
            String rankKey = rankKeys.get(i);
            int rankOrder = Integer.parseInt(rankKey.replace("rank", ""));
            int slot = rankSlots.get(i % ITEMS_PER_PAGE);
            String rankName = rankConfig.getString("ranks." + rankKey + ".name", "&dRank " + rankKey).replace('&', '§');
            String rankTag = rankConfig.getString("ranks." + rankKey + ".tag", "").replace('&', '§');
            double fragmentCost = rankConfig.getDouble("ranks." + rankKey + ".prices.fragmentos.price", 0.0);
            double moneyCost = rankConfig.getDouble("ranks." + rankKey + ".prices.money.price", 0.0);
            double discountedFragmentCost = Math.round(fragmentCost * (1 - discount / 100.0));
            double discountedMoneyCost = Math.round(moneyCost * (1 - discount / 100.0));

            // Validação de custos
            if (fragmentCost < 0 || moneyCost < 0) {
                player.sendMessage("§cErro: Custos inválidos no rank " + rankKey);
                continue;
            }

            // Encontra a chave correspondente em ranksConfig com base no order
            String menuRankKey = findMenuRankKeyByOrder(rankOrder);
            if (menuRankKey == null) {
                continue; // Pula se não encontrar correspondência
            }

            // Determina o status do rank
            String status;
            Material material;
            short data;
            boolean canAfford = fragments >= discountedFragmentCost && money >= discountedMoneyCost;
            if (rankOrder < currentRank) {
                status = "§aConcluído";
                material = Material.STAINED_GLASS_PANE;
                data = (short) 5; // Verde (lime)
            } else if (rankOrder == currentRank) {
                status = "§eAtual";
                material = Material.SKULL_ITEM;
                data = (short) 3;
            } else {
                status = canAfford ? "§aDisponível" : "§cBloqueado";
                material = Material.STAINED_GLASS_PANE;
                data = (short) 14; // Vermelho (red)
            }

            // Cria a lore com placeholders
            List<String> lore = replacePlaceholders(
                ranksConfig.getStringList("ranks.items." + menuRankKey + ".lore"),
                player, fragments, heads, money, rankOrder, currentRank, rankTag,
                fragmentCost, moneyCost, discountedFragmentCost, discountedMoneyCost, status
            );

            // Cria o item do rank
            ItemBuilder builder = new ItemBuilder(material)
                    .setName(rankName)
                    .setDurability(data)
                    .setLore(lore);
            if (material == Material.SKULL_ITEM) {
                String textureUrl = ranksConfig.getString("ranks.items." + menuRankKey + ".url");
                if (textureUrl != null && !textureUrl.isEmpty()) {
                    builder.setSkullTexture(textureUrl);
                }
            }
            ItemStack rankItem = builder.toItemStack();
            inv.setItem(slot, rankItem);
        }

        // Preenche bordas com vidro cinza
        for (int i = 0; i < size; i++) {
            if (!rankSlots.contains(i) && i != ranksConfig.getInt("ranks.previous-slot", 18) &&
                i != ranksConfig.getInt("ranks.next-slot", 26) && i != ranksConfig.getInt("ranks.back-slot", 49)) {
                ItemStack border = new ItemBuilder(Material.STAINED_GLASS_PANE)
                        .setDurability((short) 7) // Cinza
                        .setName("§7")
                        .toItemStack();
                inv.setItem(i, border);
            }
        }

        // Item de página anterior
        if (page > 1) {
            ItemStack previous = new ItemBuilder(Material.ARROW)
                    .setName("§ePágina Anterior")
                    .setLore("§7Clique para ir à página " + (page - 1))
                    .toItemStack();
            inv.setItem(ranksConfig.getInt("ranks.previous-slot", 18), previous);
        }

        // Item de próxima página
        if (page < totalPages) {
            ItemStack next = new ItemBuilder(Material.ARROW)
                    .setName("§ePróxima Página")
                    .setLore("§7Clique para ir à página " + (page + 1))
                    .toItemStack();
            inv.setItem(ranksConfig.getInt("ranks.next-slot", 26), next);
        }

        // Item de voltar
        ItemStack back = new ItemBuilder(Material.BARRIER)
                .setName("§cVoltar")
                .setLore("§7Clique para voltar ao menu principal")
                .toItemStack();
        inv.setItem(ranksConfig.getInt("ranks.back-slot", 49), back);

        player.openInventory(inv);
    }

    private static String findMenuRankKeyByOrder(int order) {
        for (String key : ranksConfig.getSection("ranks.items").getKeys(false)) {
            int configOrder = ranksConfig.getInt("ranks.items." + key + ".order", -1);
            if (configOrder == order) {
                return key;
            }
        }
        return null;
    }

    private static List<String> replacePlaceholders(List<String> lore, Player player, int fragments, int heads, double money,
            int rankOrder, int currentRank, String rankTag,
            double fragmentCost, double moneyCost, double discountedFragmentCost, double discountedMoneyCost, String status) {
List<String> newLore = new ArrayList<>();
int prestige = Rank.getPlayerPrestige(player.getUniqueId().toString());
double discount = Prestigio.getPlayerDiscount(player.getUniqueId().toString(), prestige);

for (String line : lore) {
newLore.add(line.replace("{status}", status)
.replace("{tag}", rankTag)
.replace("{fragmentos}", FormatterAPI.formatNumber(discountedFragmentCost))
.replace("{fragmentos_has}", String.valueOf(fragments))
.replace("{heads_has}", String.valueOf(heads))
.replace("{money_has}", FormatterAPI.formatNumber(money))
.replace("{money}", FormatterAPI.formatNumber(discountedMoneyCost))
.replace("{prestige}", String.valueOf(prestige))
.replace("{discount}", String.format("%.2f", discount))
.replace('&', '§'));
}

// Adiciona informações adicionais
newLore.add("");
newLore.add("§7Tag: " + rankTag);
newLore.add("§7Fragmentos: §e" + FormatterAPI.formatNumber(discountedFragmentCost));
newLore.add("§7Coins: §e" + FormatterAPI.formatNumber(discountedMoneyCost));
newLore.add("§7Status: " + status);
if (rankOrder == currentRank + 1 && moneyCost > 0) {
int progress = (discountedMoneyCost > 0) ? Math.min((int) ((money * 10.0) / discountedMoneyCost), 10) : 0;
StringBuilder progressBar = new StringBuilder();
for (int i = 0; i < 10; i++) {
progressBar.append(i < progress ? "§a█" : "§c█");
}
newLore.add("§7Progresso: " + (int) ((money * 100.0) / discountedMoneyCost) + "%");
newLore.add("§7[" + progressBar.toString() + "§7]");
}

return newLore;
}
}