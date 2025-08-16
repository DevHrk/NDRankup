package me.nd.rankup.comands;

import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.nd.rankup.Main;
import me.nd.rankup.plugin.SConfig;
import me.nd.rankup.utils.Helper;
import me.nd.rankup.utils.messages.MessageUtils;

public class Gamemode extends Commands {

	public Gamemode() {
		super("gm");
	}

	public void perform(CommandSender s, String lb, String[] args) {
		Player p = (Player) s;
		SConfig m = Main.get().getConfig("Mensagens");
		SConfig pp = Main.get().getConfig("Permissoes");
		if (!s.hasPermission(pp.getString("Permissoes.Gamemode"))) {
			MessageUtils.send(s, m.getString("Gamemode.SemPerm"), m.getStringList("Gamemode.SemPerm"));
			return;
		}

		if (args.length == 0 || args.length > 2) {
			MessageUtils.send(s, m.getString("Gamemode.Comando-Errado"), m.getStringList("Gamemode.Comando-Errado"));
			return;
		}

		String s1 = args[0];
		if (!Helper.isInteger(s1)) {
			MessageUtils.send(s, m.getString("Gamemode.Comando-Errado"), m.getStringList("Gamemode.Comando-Errado"));
			return;
		}

		Player targetPlayer = (args.length == 2) ? Bukkit.getPlayerExact(args[1]) : p;
		if (targetPlayer == null) {
			MessageUtils.send(s, m.getString("Gamemode.Jogador"), m.getStringList("Gamemode.Jogador"));
			return;
		}

		GameMode gameMode;
		switch (s1) {
		case "0":
			gameMode = GameMode.SURVIVAL;
			break;
		case "1":
			gameMode = GameMode.CREATIVE;
			break;
		case "2":
			gameMode = GameMode.ADVENTURE;
			break;
		case "3":
			gameMode = GameMode.SPECTATOR;
			break;
		default:
			MessageUtils.send(s, m.getString("Gamemode.Comando-Errado"), m.getStringList("Gamemode.Comando-Errado"));
			return;
		}

		targetPlayer.setGameMode(gameMode);
		String modo;

		switch (gameMode) {
		case SURVIVAL:
			modo = "Survival";
			break;
		case CREATIVE:
			modo = "Criativo";
			break;
		case ADVENTURE:
			modo = "Aventura";
			break;
		case SPECTATOR:
			modo = "Espectador";
			break;
		default:
			modo = "Desconhecido";
			break;
		}

		String messageKey = (args.length == 2) ? "Gamemode.MudouStaff" : "Gamemode.Mudou";
		MessageUtils.send(s,
				m.getString(messageKey).replace("{player}", targetPlayer.getName()).replace("{modo}", modo),
				m.getStringList(messageKey).stream()
						.map(message -> message.replace("{player}", targetPlayer.getName()).replace("{modo}", modo))
						.collect(Collectors.toList()));
	}

}
