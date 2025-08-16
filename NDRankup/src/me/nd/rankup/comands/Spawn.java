package me.nd.rankup.comands;

import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;

import me.nd.rankup.Main;
import me.nd.rankup.api.LocationsAPI;
import me.nd.rankup.plugin.SConfig;
import me.nd.rankup.utils.messages.MessageUtils;

public class Spawn extends Commands {
	
    public Spawn() {
        super("Spawn");
    }
    
    public void perform(CommandSender s, String lb, String[] args) {
        SConfig c = Main.get().getConfig("OpcoesLobby");
        SConfig m = Main.get().getConfig("Mensagens");
        SConfig per = Main.get().getConfig("Permissoes");
        SConfig so = Main.get().getConfig("Utils", "Sons");

        if (!(s instanceof Player)) {
            MessageUtils.send(s, m.getString("Geral.Console-Nao-Pode"), m.getStringList("Geral.Console-Nao-Pode"));
            return;
        }

        Player p = (Player) s;

        // Se o jogador tem permissão para teleportar outros jogadores
        if (args.length > 0 && p.hasPermission(per.getString("Spawn.Outros"))) {
            if (args.length != 2) {
                MessageUtils.send(p, m.getString("Spawn.Spawn-Comando-Incorreto"));
                return;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                MessageUtils.send(p, m.getString("Geral.Player-Offline"));
                return;
            }

            switch (args[0].toLowerCase()) {
                case "normal":
                    target.teleport(LocationsAPI.spawn, TeleportCause.COMMAND);
                    MessageUtils.send(target, m.getString("Spawn.Teleportado-Para-Spawn").replace("{player}", s.getName()),
                            m.getStringList("Spawn.Teleportado-Para-Spawn").stream()
                                    .map(message -> message.replace("{player}", s.getName()))
                                    .collect(Collectors.toList()));
                    MessageUtils.send(p, m.getString("Spawn.Teleportado-Outro-Com-Sucesso-Spawn").replace("{player}", target.getName()),
                            m.getStringList("Spawn.Teleportado-Outro-Com-Sucesso-Spawn").stream()
                                    .map(message -> message.replace("{player}", target.getName()))
                                    .collect(Collectors.toList()));
                    return;

                case "vip":
                    target.teleport(LocationsAPI.spawnVip, TeleportCause.COMMAND);
                    MessageUtils.send(target, m.getString("Spawn.Teleportado-Para-Spawn-Vip").replace("{player}", s.getName()),
                            m.getStringList("Spawn.Teleportado-Para-Spawn-Vip").stream()
                                    .map(message -> message.replace("{player}", s.getName()))
                                    .collect(Collectors.toList()));
                    MessageUtils.send(p, m.getString("Spawn.Teleportado-Outro-Com-Sucesso-Spawn-Vip").replace("{player}", target.getName()),
                            m.getStringList("Spawn.Teleportado-Outro-Com-Sucesso-Spawn-Vip").stream()
                                    .map(message -> message.replace("{player}", target.getName()))
                                    .collect(Collectors.toList()));
                    return;

                default:
                    MessageUtils.send(p, m.getString("Spawn.Spawn-Comando-Incorreto"), m.getStringList("Spawn.Spawn-Comando-Incorreto"));
                    return;
            }
        }

        // Se o jogador não tem permissão para teleportar outros jogadores
        if (!p.hasPermission(per.getString("Spawn.Spawn"))) {
            MessageUtils.send(p, m.getString("Geral.Sem-perm"), m.getStringList("Geral.Sem-perm"));
            p.playSound(p.getLocation(), Sound.valueOf(so.getString("Spawn.SemPerm")), 1.0F, 1.0F);
            return;
        }

        // Se o jogador não tem permissão para o delay de spawn
        if (!p.hasPermission(per.getString("Delay.Spawn"))) {
            MessageUtils.send(p, m.getString("Spawn.Iniciando-Teleporte-Spawn").replace("{tempo}", String.valueOf(c.getInt("Delay.Delay-Para-Teleportar"))),
                    m.getStringList("Spawn.Iniciando-Teleporte-Spawn").stream()
                            .map(message -> message.replace("{tempo}", String.valueOf(c.getInt("Delay.Delay-Para-Teleportar"))))
                            .collect(Collectors.toList()));
            new BukkitRunnable() {
                @Override
                public void run() {
                    p.teleport(LocationsAPI.spawn, TeleportCause.COMMAND);
                    MessageUtils.send(p, m.getString("Spawn.Teleportado-Com-Sucesso-Spawn"), m.getStringList("Spawn.Teleportado-Com-Sucesso-Spawn"));
                }
            }.runTaskLater(Main.get(), 20L * c.getInt("Delay.Delay-Para-Teleportar"));
            return;
        }

        p.teleport(LocationsAPI.spawn, TeleportCause.COMMAND);
        MessageUtils.send(p, m.getString("Spawn.Teleportado-Com-Sucesso-Spawn"), m.getStringList("Spawn.Teleportado-Com-Sucesso-Spawn"));
    }
}