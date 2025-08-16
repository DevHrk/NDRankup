package me.nd.rankup.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import me.nd.rankup.comands.Kit;
import me.nd.rankup.menus.item.MItem;

public class MenuAdapter {

    private final Kit kit;

    public MenuAdapter(Kit kit) {
        this.kit = kit;
    }

    public Menu read(FileConfiguration config, Player player) {
        Menu menu = new Menu(config.getString("name"), config.getString("inventoryTitle"), config.getInt("size"), config.getBoolean("cancelClick"), config.getString("command"), config.getString("permission"));
        for (String string : config.getConfigurationSection("items").getKeys(false)) {
            if (string == null)
                continue;
            int id = Integer.parseInt(config.getString("items." + string + ".id"));
            int amount = Integer.parseInt(config.getString("items." + string + ".amount"));
            ItemStack item = new ItemStack(Material.getMaterial(id), amount);
            ItemMeta meta = item.getItemMeta();
            if (Integer.parseInt(config.getString("items." + string + ".data")) != 0)
                item.setDurability(Short.parseShort(config.getString("items." + string + ".data")));
            String name = ChatColor.translateAlternateColorCodes('&', config.getString("items." + string + ".name"));
            if (!name.equalsIgnoreCase(""))
                meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            for (String s : config.getStringList("items." + string + ".lore")) {
                String processedLore = player != null ? processPlaceholders(s, player) : s;
                lore.add(ChatColor.translateAlternateColorCodes('&', processedLore));
            }
            if (!lore.isEmpty())
                meta.setLore(lore);
            List<String> enchantments = config.getStringList("items." + string + ".enchantments");
            if (!enchantments.isEmpty())
                for (String enchants : enchantments) {
                    if (!enchants.contains(";"))
                        continue;
                    String[] split = enchants.split(";");
                    meta.addEnchant(Enchantment.getById(Integer.parseInt(split[0])), Integer.parseInt(split[1]), true);
                }
            // Handle custom skull texture for SKULL_ITEM
            if (item.getType() == Material.SKULL_ITEM) {
                String textureUrl = config.getString("items." + string + ".url");
                if (textureUrl != null && !textureUrl.isEmpty()) {
                    setSkullTexture((SkullMeta) meta, textureUrl);
                }
            }
            item.setItemMeta(meta);
            int costCash = (config.getString("items." + string + ".cash") != null) ? config.getInt("items." + string + ".cash") : 0;
            boolean consoleRight, consoleLeft;
            if (config.getString("items." + string + ".console") == null) {
                consoleRight = config.getBoolean("items." + string + ".consoleRightClick");
                consoleLeft = config.getBoolean("items." + string + ".consoleLeftClick");
            } else {
                consoleRight = config.getBoolean("items." + string + ".console");
                consoleLeft = config.getBoolean("items." + string + ".console");
            }
            MItem mItem = MItem.builder().item(item).cost(config.getInt("items." + string + ".preco")).costCash(costCash).commandRight(config.getString("items." + string + ".commandRightClick")).commandLeft(config.getString("items." + string + ".commandLeftClick")).consoleLeft(consoleLeft).consoleRight(consoleRight).slot(config.getInt("items." + string + ".slot")).build();
            menu.setItem(mItem, mItem.getSlot());
        }
        return menu;
    }

    public Menu createPlayerSpecificMenu(Menu baseMenu, Player player) {
        Menu playerMenu = new Menu(baseMenu.getName(), baseMenu.getInventoryTitle(), baseMenu.getSize(), baseMenu.isCancelClick(), baseMenu.getCommand(), baseMenu.getPermission());
        for (int slot = 0; slot < baseMenu.getSize(); slot++) {
            MItem baseItem = baseMenu.getItem(slot);
            if (baseItem == null)
                continue;
            ItemStack item = baseItem.getItem().clone();
            ItemMeta meta = item.getItemMeta();
            if (meta.hasLore()) {
                List<String> lore = new ArrayList<>();
                for (String s : meta.getLore()) {
                    String strippedLore = ChatColor.stripColor(s);
                    String processedLore = processPlaceholders(strippedLore, player);
                    lore.add(ChatColor.translateAlternateColorCodes('&', processedLore));
                }
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
            MItem playerItem = MItem.builder()
                    .item(item)
                    .cost(baseItem.getCost())
                    .costCash(baseItem.getCostCash())
                    .commandRight(baseItem.getCommandRight())
                    .commandLeft(baseItem.getCommandLeft())
                    .consoleLeft(baseItem.isConsoleLeft())
                    .consoleRight(baseItem.isConsoleRight())
                    .slot(baseItem.getSlot())
                    .build();
            playerMenu.setItem(playerItem, playerItem.getSlot());
        }
        return playerMenu;
    }

    private String processPlaceholders(String input, Player player) {
        String result = input;

        Pattern delayPattern = Pattern.compile("\\{kitdelay\\.([^}]+)\\}");
        Matcher delayMatcher = delayPattern.matcher(result);
        StringBuffer buffer = new StringBuffer();
        while (delayMatcher.find()) {
            String kitName = delayMatcher.group(1);
            String replacement = kit.getKitCooldown(player, kitName);
            delayMatcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        delayMatcher.appendTail(buffer);
        result = buffer.toString();

        Pattern permPattern = Pattern.compile("\\{kitperm\\.([^}]+)\\}");
        Matcher permMatcher = permPattern.matcher(result);
        buffer = new StringBuffer();
        while (permMatcher.find()) {
            String kitName = permMatcher.group(1);
            String replacement = kit.getKitPermissionStatus(player, kitName);
            permMatcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        permMatcher.appendTail(buffer);
        result = buffer.toString();

        return result;
    }

    private void setSkullTexture(SkullMeta meta, String textureUrl) {
        // Check if the server version supports GameProfile (1.8 uses different method)
        try {
            java.util.UUID uuid = java.util.UUID.randomUUID();
            com.mojang.authlib.GameProfile profile = new com.mojang.authlib.GameProfile(uuid, null);
            com.mojang.authlib.properties.Property textures = new com.mojang.authlib.properties.Property("textures", textureUrl);
            profile.getProperties().put("textures", textures);
            java.lang.reflect.Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}