package me.nd.rankup.comands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.nd.rankup.Main;
import me.nd.rankup.api.FormatterAPI;
import me.nd.rankup.cash.CashManager;
import me.nd.rankup.dados.SQlite;
import me.nd.rankup.utils.ItemBuilder;
import net.milkbowl.vault.economy.Economy;

public class Bau extends Commands implements Listener {

    private static final Connection connection = SQlite.getConnection();
    private final Map<UUID, Integer> openVaults = new HashMap<>();
    private final Map<UUID, Integer> managingChest = new HashMap<>();
    private final Map<UUID, Integer> expandingChest = new HashMap<>();
    private final Map<UUID, Integer> renamingChest = new HashMap<>();
    private final Economy economy = Main.get().getEconomy();

    public Bau() {
        super("bau");
        Main.get().getServer().getPluginManager().registerEvents(this, Main.get());
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return;
        }
        Player player = (Player) sender;
        openChestMenu(player);
    }

    // Initialize chest metadata for a new Bau
    private void initializeChestMetadata(Player player, int chestNumber) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR IGNORE INTO ender_chest_metadata (uuid, chest_number, custom_name, size, icon) " +
                "VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setInt(2, chestNumber);
            ps.setString(3, "Ec #" + chestNumber);
            ps.setInt(4, 27);
            ps.setString(5, "CHEST");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Calculate discount percentage based on player permissions
    private int getDiscount(Player player) {
        if (player.hasPermission("nd.bau.discount.vip")) {
            return 20; // 20% discount for VIP
        } else if (player.hasPermission("nd.bau.discount.regular")) {
            return 10; // 10% discount for regular players
        }
        return 0; // No discount
    }

    // Abre o menu principal de Baus
    private void openChestMenu(Player player) {
        int unlockedChests = getUnlockedChests(player);
        Inventory inv = Bukkit.createInventory(null, 54, "Menu de Bau");

        // Adiciona os Baus desbloqueados
        for (int i = 1; i <= unlockedChests; i++) {
            initializeChestMetadata(player, i); // Ensure metadata exists
            int slot = (i - 1) % 45; // Slots 0 a 44 para Baus
            String customName = getChestCustomName(player, i);
            int usedSlots = getUsedSlots(player, i);
            int totalSlots = getChestSize(player, i);
            String icon = getChestIcon(player, i);
            Material iconMaterial = Material.getMaterial(icon != null ? icon : "CHEST");

            ItemStack chest = new ItemBuilder(iconMaterial != null ? iconMaterial : Material.CHEST)
                    .setName("§e" + customName)
                    .setLore(
                            "",
                            "§f > §bBau #" + i + "§f.",
                            "§f > §7Slots: §c" + usedSlots + "§7/§a" + totalSlots + "§f.",
                            "",
                            "§7Botão §fesquerdo§7 para abrir o Bau.",
                            "§7Botão §fdireito§7 para gerenciar o Bau."
                    )
                    .toItemStack();
            inv.setItem(slot, chest);
        }

        // Item para comprar novo Bau
        int baseMoney = 1000000000;
        int baseCash = 140;
        int desconto = getDiscount(player);
        double discountMultiplier = (100.0 - desconto) / 100.0;
        int finalMoney = (int) (baseMoney * discountMultiplier);
        int finalCash = (int) (baseCash * discountMultiplier);

        ItemStack buy = new ItemBuilder(Material.CHEST)
                .setName("§6Comprar Bau")
                .setLore(
                        "§7Clique para comprar",
                        "§7um novo Bau.",
                        "",
                        "§7Preços:",
                        "§f > §a" + FormatterAPI.formatNumber(finalMoney) + " coins (Botão Esquerdo).",
                        "§f > §6" + FormatterAPI.formatNumber(finalCash) + " pontos (Botão Direito).",
                        "",
                        "§fSeu desconto: §a" + desconto + "%§f."
                )
                .toItemStack();
        inv.setItem(30, buy);

        player.openInventory(inv);
    }

    // Menu de gerenciamento do Bau
    private void openManageMenu(Player player, int chestNumber) {
        Inventory inv = Bukkit.createInventory(null, 27, "Bau #" + chestNumber + " - Ações");
        String currentName = getChestCustomName(player, chestNumber);
        int currentLines = getChestSize(player, chestNumber) / 9;

        // Item para renomear
        ItemStack nameItem = new ItemBuilder(Material.NAME_TAG)
                .setName("§aNome do Bau")
                .setLore(
                        "§7Nome do Bau que ficará",
                        "§7no /ec.",
                        "",
                        "§7 Atual: " + currentName,
                        "",
                        "§7Clique para renomear."
                )
                .toItemStack();
        inv.setItem(11, nameItem);

        // Item para mudar ícone
        ItemStack iconItem = new ItemBuilder(Material.WRITTEN_BOOK)
                .setName("§aÍcone do Bau")
                .setLore(
                        "§7Mude o ícone que ficará",
                        "§7no /ec.",
                        "",
                        "§7Clique para alterar o ícone."
                )
                .toItemStack();
        inv.setItem(13, iconItem);

        // Item para expandir
        int baseMoney = 500000000;
        int baseCash = 40;
        int desconto = getDiscount(player);
        double discountMultiplier = (100.0 - desconto) / 100.0;
        int finalMoney = (int) (baseMoney * discountMultiplier);
        int finalCash = (int) (baseCash * discountMultiplier);

        ItemStack expandItem = new ItemBuilder(Material.SKULL_ITEM, 1, (short) 3)
                .setSkullTexture("3857d6b17901fd3f0109bd9bdcc28021b65947fce0a958327247d26d92915b85")
                .setName("§aExpandir Bau")
                .setLore(
                        "§7Expanda a quantia de",
                        "§7linhas do Bau.",
                        "",
                        "§7 Atual: §b" + currentLines + "§7.",
                        "",
                        "§7Preços:",
                        "§f > §a" + FormatterAPI.formatNumber(finalMoney) + " coins (Botão Esquerdo).",
                        "§f > §6" + FormatterAPI.formatNumber(finalCash) + " pontos (Botão Direito).",
                        "",
                        "§fSeu desconto: §a" + desconto + "%§f.",
                        "",
                        "§7Clique para expandir."
                )
                .toItemStack();
        inv.setItem(15, expandItem);

        managingChest.put(player.getUniqueId(), chestNumber);
        player.openInventory(inv);
    }

    // Menu de confirmação para expansão
    private void openExpandMenu(Player player, int chestNumber, boolean useMoney) {
        Inventory inv = Bukkit.createInventory(null, 27, "Expandir Bau #" + chestNumber);
        int baseMoney = 500000000;
        int baseCash = 40;
        int desconto = getDiscount(player);
        double discountMultiplier = (100.0 - desconto) / 100.0;
        int finalMoney = (int) (baseMoney * discountMultiplier);
        int finalCash = (int) (baseCash * discountMultiplier);

        // Item para confirmar
        ItemStack confirm = new ItemBuilder(Material.WOOL, 1, (short) 5)
                .setName("§aConfirmar")
                .setLore("§7Clique para confirmar e expandir.")
                .toItemStack();
        inv.setItem(11, confirm);

        // Item para cancelar
        ItemStack cancel = new ItemBuilder(Material.WOOL, 1, (short) 14)
                .setName("§cCancelar")
                .setLore("§7Clique para cancelar e retornar.")
                .toItemStack();
        inv.setItem(15, cancel);

        // Item de expansão
        ItemStack expand = new ItemBuilder(Material.CHEST)
                .setName("§aExpansão")
                .setLore(
                        "",
                        "§7Preço:",
                        useMoney ? "§f > §a" + FormatterAPI.formatNumber(finalMoney) + " coins." : "§f > §6" + FormatterAPI.formatNumber(finalCash) + " pontos.",
                        "",
                        "§fSeu desconto: §a" + desconto + "%§f."
                )
                .toItemStack();
        inv.setItem(13, expand);

        expandingChest.put(player.getUniqueId(), chestNumber);
        player.openInventory(inv);
    }

    // Menu de seleção de ícones
    private void openIconMenu(Player player, int chestNumber) {
        Inventory inv = Bukkit.createInventory(null, 27, "Selecionar Ícone - Bau #" + chestNumber);
        Material[] icons = {
                Material.DIAMOND,
                Material.EMERALD,
                Material.GOLD_INGOT,
                Material.IRON_INGOT,
                Material.REDSTONE,
                Material.LAPIS_BLOCK,
                Material.CHEST,
                Material.ENDER_CHEST
        };

        for (int i = 0; i < icons.length; i++) {
            ItemStack icon = new ItemBuilder(icons[i])
                    .setName("§aÍcone: " + icons[i].name())
                    .setLore("§7Clique para selecionar este ícone.")
                    .toItemStack();
            inv.setItem(i + 9, icon); // Slots 9 a 16
        }

        managingChest.put(player.getUniqueId(), chestNumber);
        player.openInventory(inv);
    }

    // Verifica se o jogador desbloqueou o Bau
    private boolean hasChestUnlocked(Player player, int chestNumber) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT unlocked_chests FROM ender_chest_access WHERE uuid = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int unlockedChests = rs.getInt("unlocked_chests");
                return chestNumber <= unlockedChests;
            } else {
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT OR IGNORE INTO ender_chest_access (uuid, unlocked_chests) VALUES (?, 1)")) {
                    insert.setString(1, player.getUniqueId().toString());
                    insert.executeUpdate();
                }
                return chestNumber == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cErro ao verificar Baus desbloqueados!");
            return false;
        }
    }

    // Obtém o número de Baus desbloqueados
    private int getUnlockedChests(Player player) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT unlocked_chests FROM ender_chest_access WHERE uuid = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("unlocked_chests");
            }
            return 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        }
    }

    // Obtém o nome personalizado do Bau
    private String getChestCustomName(Player player, int chestNumber) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT custom_name FROM ender_chest_metadata WHERE uuid = ? AND chest_number = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setInt(2, chestNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String name = rs.getString("custom_name");
                return name != null ? name : "Ec #" + chestNumber;
            }
            initializeChestMetadata(player, chestNumber);
            return "Ec #" + chestNumber;
        } catch (SQLException e) {
            e.printStackTrace();
            return "Ec #" + chestNumber;
        }
    }

    // Define o nome personalizado do Bau
    private void setChestCustomName(Player player, int chestNumber, String name) {
        initializeChestMetadata(player, chestNumber);
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE ender_chest_metadata SET custom_name = ? WHERE uuid = ? AND chest_number = ?")) {
            ps.setString(1, name);
            ps.setString(2, player.getUniqueId().toString());
            ps.setInt(3, chestNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cErro ao renomear o Bau!");
        }
        // Update ender_chests table for consistency
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE ender_chests SET custom_name = ? WHERE uuid = ? AND chest_number = ?")) {
            ps.setString(1, name);
            ps.setString(2, player.getUniqueId().toString());
            ps.setInt(3, chestNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Obtém o tamanho do Bau
    private int getChestSize(Player player, int chestNumber) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT size FROM ender_chest_metadata WHERE uuid = ? AND chest_number = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setInt(2, chestNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("size");
            }
            initializeChestMetadata(player, chestNumber);
            return 27;
        } catch (SQLException e) {
            e.printStackTrace();
            return 27;
        }
    }

    // Define o tamanho do Bau
    private void setChestSize(Player player, int chestNumber, int size) {
        initializeChestMetadata(player, chestNumber);
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE ender_chest_metadata SET size = ? WHERE uuid = ? AND chest_number = ?")) {
            ps.setInt(1, size);
            ps.setString(2, player.getUniqueId().toString());
            ps.setInt(3, chestNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cErro ao expandir o Bau!");
        }
        // Update ender_chests table for consistency
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE ender_chests SET size = ? WHERE uuid = ? AND chest_number = ?")) {
            ps.setInt(1, size);
            ps.setString(2, player.getUniqueId().toString());
            ps.setInt(3, chestNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Obtém o ícone do Bau
    private String getChestIcon(Player player, int chestNumber) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT icon FROM ender_chest_metadata WHERE uuid = ? AND chest_number = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setInt(2, chestNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("icon");
            }
            initializeChestMetadata(player, chestNumber);
            return "CHEST";
        } catch (SQLException e) {
            e.printStackTrace();
            return "CHEST";
        }
    }

    // Define o ícone do Bau
    private void setChestIcon(Player player, int chestNumber, String icon) {
        initializeChestMetadata(player, chestNumber);
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE ender_chest_metadata SET icon = ? WHERE uuid = ? AND chest_number = ?")) {
            ps.setString(1, icon);
            ps.setString(2, player.getUniqueId().toString());
            ps.setInt(3, chestNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cErro ao alterar o ícone do Bau!");
        }
        // Update ender_chests table for consistency
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE ender_chests SET icon = ? WHERE uuid = ? AND chest_number = ?")) {
            ps.setString(1, icon);
            ps.setString(2, player.getUniqueId().toString());
            ps.setInt(3, chestNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Obtém slots usados
    private int getUsedSlots(Player player, int chestNumber) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM ender_chests WHERE uuid = ? AND chest_number = ? AND item IS NOT NULL")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setInt(2, chestNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Abre o inventário do Bau
    private void openBau(Player player, int chestNumber) {
        int size = getChestSize(player, chestNumber);
        Inventory Bau = Bukkit.createInventory(null, size, "Ec #" + chestNumber);
        loadBauContents(player, chestNumber, Bau);
        openVaults.put(player.getUniqueId(), chestNumber);
        player.openInventory(Bau);
    }

    // Carrega os itens do Bau
    private void loadBauContents(Player player, int chestNumber, Inventory Bau) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT slot, item FROM ender_chests WHERE uuid = ? AND chest_number = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setInt(2, chestNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int slot = rs.getInt("slot");
                String itemData = rs.getString("item");
                if (itemData != null && !itemData.isEmpty()) {
                    ItemStack item = itemFromString(itemData);
                    if (item != null && slot >= 0 && slot < Bau.getSize()) {
                        Bau.setItem(slot, item);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cErro ao carregar o conteúdo do Bau!");
        }
    }

    // Salva os itens do Bau
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();
        if (!title.startsWith("Ec #")) return;

        int chestNumber = openVaults.remove(player.getUniqueId());
        if (chestNumber == 0) return;

        try {
            // Limpa o conteúdo anterior do Bau
            try (PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM ender_chests WHERE uuid = ? AND chest_number = ?")) {
                delete.setString(1, player.getUniqueId().toString());
                delete.setInt(2, chestNumber);
                delete.executeUpdate();
            }

            // Salva os itens atuais
            Inventory inventory = event.getInventory();
            String customName = getChestCustomName(player, chestNumber);
            int size = inventory.getSize();
            String icon = getChestIcon(player, chestNumber);
            for (int slot = 0; slot < size; slot++) {
                ItemStack item = inventory.getItem(slot);
                if (item != null && item.getType() != Material.AIR) {
                    try (PreparedStatement insert = connection.prepareStatement(
                            "INSERT INTO ender_chests (uuid, chest_number, slot, item, custom_name, size, icon) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                        insert.setString(1, player.getUniqueId().toString());
                        insert.setInt(2, chestNumber);
                        insert.setInt(3, slot);
                        insert.setString(4, itemToString(item));
                        insert.setString(5, customName);
                        insert.setInt(6, size);
                        insert.setString(7, icon);
                        insert.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cErro ao salvar o conteúdo do Bau!");
        }
    }

    // Gerencia cliques nos menus
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        int slot = event.getSlot();

        // Menu principal de Baus
        if (title.equals("Menu de Bau")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            int chestNumber = slot + 1;
            if (hasChestUnlocked(player, chestNumber)) {
                if (event.isLeftClick()) {
                    openBau(player, chestNumber);
                } else if (event.isRightClick()) {
                    openManageMenu(player, chestNumber);
                }
            } else if (slot == 30) {
                // Comprar novo Bau
                int baseMoney = 1000000000;
                int baseCash = 140;
                int desconto = getDiscount(player);
                double discountMultiplier = (100.0 - desconto) / 100.0;
                int finalMoney = (int) (baseMoney * discountMultiplier);
                int finalCash = (int) (baseCash * discountMultiplier);

                if (event.isLeftClick()) {
                    // Pagar com money
                    if (economy.getBalance(player) < finalMoney) {
                        player.sendMessage("§cVocê não tem coins suficientes! Necessário: " + FormatterAPI.formatNumber(finalMoney) + " coins.");
                        player.closeInventory();
                        return;
                    }

                    // Deduzir coins
                    economy.withdrawPlayer(player, finalMoney);

                    try (PreparedStatement ps = connection.prepareStatement(
                            "UPDATE ender_chest_access SET unlocked_chests = unlocked_chests + 1 WHERE uuid = ?")) {
                        ps.setString(1, player.getUniqueId().toString());
                        ps.executeUpdate();
                        initializeChestMetadata(player, getUnlockedChests(player)); // Initialize new Bau
                        player.sendMessage("§aNovo Bau comprado com sucesso por " + finalMoney + " coins!");
                        openChestMenu(player);
                    } catch (SQLException e) {
                        // Reembolsar se houver erro no banco
                        economy.depositPlayer(player, finalMoney);
                        e.printStackTrace();
                        player.sendMessage("§cErro ao comprar novo Bau!");
                        player.closeInventory();
                    }
                } else if (event.isRightClick()) {
                    // Pagar com cash
                    if (CashManager.getCash(player) < finalCash) {
                        player.sendMessage("§cVocê não tem pontos suficientes! Necessário: " + FormatterAPI.formatNumber(finalCash) + " pontos.");
                        player.closeInventory();
                        return;
                    }

                    // Deduzir cash
                    if (!CashManager.removeCash(player, finalCash)) {
                        player.sendMessage("§cErro ao processar a compra de pontos!");
                        player.closeInventory();
                        return;
                    }

                    try (PreparedStatement ps = connection.prepareStatement(
                            "UPDATE ender_chest_access SET unlocked_chests = unlocked_chests + 1 WHERE uuid = ?")) {
                        ps.setString(1, player.getUniqueId().toString());
                        ps.executeUpdate();
                        initializeChestMetadata(player, getUnlockedChests(player)); // Initialize new Bau
                        player.sendMessage("§aNovo Bau comprado com sucesso por " + finalCash + " pontos!");
                        openChestMenu(player);
                    } catch (SQLException e) {
                        // Reembolsar se houver erro no banco
                        CashManager.addCash(player, finalCash);
                        e.printStackTrace();
                        player.sendMessage("§cErro ao comprar novo Bau!");
                        player.closeInventory();
                    }
                }
            }
        }
        // Menu de gerenciamento
        else if (title.startsWith("Bau #") && title.contains(" - Ações")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            int chestNumber = managingChest.getOrDefault(player.getUniqueId(), 0);
            if (chestNumber == 0) return;

            if (slot == 11) {
                renamingChest.put(player.getUniqueId(), chestNumber);
                player.sendMessage("§aDigite o novo nome do Bau no chat (máximo 32 caracteres):");
                player.closeInventory();
            } else if (slot == 13) {
                openIconMenu(player, chestNumber);
            } else if (slot == 15) {
                openExpandMenu(player, chestNumber, true); // Default to money for left-click
            }
        }
        // Menu de expansão
        else if (title.startsWith("Expandir Bau #")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            int chestNumber = expandingChest.getOrDefault(player.getUniqueId(), 0);
            if (chestNumber == 0) return;

            if (slot == 11) {
                // Confirmar expansão
                int currentSize = getChestSize(player, chestNumber);
                if (currentSize >= 54) {
                    player.sendMessage("§cO Bau já está no tamanho máximo (54 slots)!");
                    player.closeInventory();
                    return;
                }
                int baseMoney = 500000000;
                int baseCash = 40;
                int desconto = getDiscount(player);
                double discountMultiplier = (100.0 - desconto) / 100.0;
                int finalMoney = (int) (baseMoney * discountMultiplier);
                int finalCash = (int) (baseCash * discountMultiplier);

                // Format numbers for comparison and display
                String formattedFinalMoney = FormatterAPI.formatNumber(finalMoney);
                String formattedFinalCash = FormatterAPI.formatNumber(finalCash);

                // Check lore for payment method
                boolean useMoney = event.getInventory().getItem(13).getItemMeta().getLore().contains("§f > §a" + formattedFinalMoney + " coins.");

                if (useMoney) {
                    // Pagar com money
                    if (economy.getBalance(player) < finalMoney) {
                        player.sendMessage("§cVocê não tem coins suficientes! Necessário: " + formattedFinalMoney + " coins.");
                        player.closeInventory();
                        return;
                    }

                    // Deduzir coins
                    economy.withdrawPlayer(player, finalMoney);

                    int newSize = currentSize + 9;
                    try {
                        setChestSize(player, chestNumber, newSize);
                        player.sendMessage("§aBau #" + chestNumber + " expandido para " + newSize + " slots por " + formattedFinalMoney + " coins!");
                        openManageMenu(player, chestNumber);
                    } catch (Exception e) {
                        // Reembolsar se houver erro
                        economy.depositPlayer(player, finalMoney);
                        e.printStackTrace();
                        player.sendMessage("§cErro ao expandir o Bau!");
                        player.closeInventory();
                    }
                } else {
                    // Pagar com cash
                    if (CashManager.getCash(player) < finalCash) {
                        player.sendMessage("§cVocê não tem pontos suficientes! Necessário: " + formattedFinalCash + " pontos.");
                        player.closeInventory();
                        return;
                    }

                    // Deduzir cash
                    if (!CashManager.removeCash(player, finalCash)) {
                        player.sendMessage("§cErro ao processar a expansão de pontos!");
                        player.closeInventory();
                        return;
                    }

                    int newSize = currentSize + 9;
                    try {
                        setChestSize(player, chestNumber, newSize);
                        player.sendMessage("§aBau #" + chestNumber + " expandido para " + newSize + " slots por " + formattedFinalCash + " pontos!");
                        openManageMenu(player, chestNumber);
                    } catch (Exception e) {
                        // Reembolsar se houver erro
                        CashManager.addCash(player, finalCash);
                        e.printStackTrace();
                        player.sendMessage("§cErro ao expandir o Bau!");
                        player.closeInventory();
                    }
                }
            } else if (slot == 15) {
                openManageMenu(player, chestNumber);
            }
        }
        // Menu de ícones
        else if (title.startsWith("Selecionar Ícone - Bau #")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            int chestNumber = managingChest.getOrDefault(player.getUniqueId(), 0);
            if (chestNumber == 0) return;

            Material selectedIcon = event.getCurrentItem().getType();
            setChestIcon(player, chestNumber, selectedIcon.name());
            player.sendMessage("§aÍcone do Bau #" + chestNumber + " alterado para " + selectedIcon.name() + "!");
            openManageMenu(player, chestNumber);
        }
    }

    // Captura o nome do Bau no chat
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!renamingChest.containsKey(uuid)) return;

        event.setCancelled(true);
        int chestNumber = renamingChest.remove(uuid);
        String newName = event.getMessage().trim();
        if (newName.length() > 32) {
            player.sendMessage("§cO nome do Bau não pode exceder 32 caracteres!");
            return;
        }

        // Atualiza o nome no banco
        setChestCustomName(player, chestNumber, newName);
        Bukkit.getScheduler().runTask(Main.get(), () -> {
            player.sendMessage("§aBau #" + chestNumber + " renomeado para: " + newName);
            openManageMenu(player, chestNumber);
        });
    }

    // Converte ItemStack para String
    private String itemToString(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        return item.getType().name() + ";" + item.getAmount();
    }

    // Converte String para ItemStack
    private ItemStack itemFromString(String data) {
        if (data == null || data.isEmpty()) return null;
        String[] parts = data.split(";");
        try {
            Material material = Material.valueOf(parts[0]);
            int amount = Integer.parseInt(parts[1]);
            return new ItemStack(material, amount);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}