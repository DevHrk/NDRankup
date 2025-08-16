package me.nd.rankup.comands;

import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.nd.rankup.Main;
import me.nd.rankup.plugin.SConfig;
import me.nd.rankup.utils.messages.MessageUtils;

public class Speed extends Commands {

	public Speed() {
		super("speed");
	}

	@Override
	public void perform(CommandSender s, String label, String[] args) {
		// Verificando se o sender é um player
		SConfig m = Main.get().getConfig("Mensagens");
		SConfig pp = Main.get().getConfig("Permissoes");
		if (!(s instanceof Player)) {

			// Verificando se o sender digitou o número de argumentos correto
			if (args.length != 2) {
				MessageUtils.send(s, m.getString("Speed.Speed-Comando-Incorreto"),
						m.getStringList("Speed.Speed-Comando-Incorreto"));
				return;
			}

			// Pegando o player e verificando se ele esta online
			Player p = Bukkit.getPlayer(args[1]);
			if (p == null) {
				MessageUtils.send(s, m.getString("Speed.Jogador"), m.getStringList("Speed.Jogador"));
				return;
			}

			// Verificando se o player não quer resetar o fly
			if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("reset")) {
				p.setFlySpeed(0.1f);
				p.setWalkSpeed(0.2f);
				MessageUtils.send(s,
						m.getString("Speed.Speed-Alterado-Outro").replace("%speed%", "PADRAO").replace("%player%",
								p.getName()),
						m.getStringList("Speed.Speed-Alterado-Outro").stream()
								.map(message -> message.replace("%speed%", "PADRAO").replace("%player%", p.getName()))
								.collect(Collectors.toList()));
				return;
			}

			// Verificando se o número é um número valido
			float speed;
			try {
				speed = Float.parseFloat(args[0]);
			} catch (NumberFormatException e) {
				MessageUtils.send(s,
						m.getString("Speed.Numero-Invalido").replace("%numero%", e.getMessage().split("\"")[1]),
						m.getStringList("Speed.Numero-Invalido").stream()
								.map(message -> message.replace("%numero%", e.getMessage().split("\"")[1]))
								.collect(Collectors.toList()));
				return;
			}

			// Verificando se a velocidade é valida (necessario bukkit)
			if (speed > 1.0f || speed < -1.0f) {
				MessageUtils.send(s, m.getString("Speed.Speed-Valor-Invalido"),
						m.getStringList("Speed.Speed-Valor-Invalido"));
				return;
			}

			// Setando o speed no player e informando
			p.setFlySpeed(speed);
			p.setWalkSpeed(speed);
			MessageUtils.send(s,
					m.getString("Speed.Speed-Alterado-Outro").replace("%speed%", args[0]).replace("%player%",
							p.getName()),
					m.getStringList("Speed.Speed-Alterado-Outro").stream()
							.map(message -> message.replace("%speed%", args[0]).replace("%player%", p.getName()))
							.collect(Collectors.toList()));
			return;
		}

		// Verificando se o player digitou o número de argumentos correto
		if (args.length < 1 || args.length > 2) {
			MessageUtils.send(s, m.getString("Speed.Speed-Comando-Incorreto"),
					m.getStringList("Speed.Speed-Comando-Incorreto"));
			return;
		}

		if (args.length == 1) {
			if (!s.hasPermission(pp.getString("Permissoes.Speed"))) {
				MessageUtils.send(s, m.getString("Speed.SemPermissao"), m.getStringList("Speed.SemPermissao"));
				return;
			}
			// Verificando se o player não quer resetar o fly
			Player p = (Player) s;
			if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("reset")) {
				p.setFlySpeed(0.1f);
				p.setWalkSpeed(0.2f);
				MessageUtils.send(s,
						m.getString("Speed.Speed-Alterado-Voce").replace("%speed%", "PADRAO").replace("%player%",
								p.getName()),
						m.getStringList("Speed.Speed-Alterado-Voce").stream()
								.map(message -> message.replace("%speed%", "PADRAO").replace("%player%", p.getName()))
								.collect(Collectors.toList()));
				return;
			}

			// Pegando o player e verificando se o número é um número valido
			float speed;
			try {
				speed = Float.parseFloat(args[0]);
			} catch (NumberFormatException e) {
				MessageUtils.send(s,
						m.getString("Speed.Numero-Invalido").replace("%numero%", e.getMessage().split("\"")[1]),
						m.getStringList("Speed.Numero-Invalido").stream()
								.map(message -> message.replace("%speed%", args[0])).collect(Collectors.toList()));
				return;
			}

			// Verificando se a velocidade é valida (necessario bukkit)
			if (speed > 1.0f || speed < -1.0f) {
				MessageUtils.send(s, m.getString("Speed.Speed-Valor-Invalido"),
						m.getStringList("Speed.Speed-Valor-Invalido"));
				return;
			}

			// Setando o speed no player e informando
			p.setFlySpeed(speed);
			p.setWalkSpeed(speed);
			MessageUtils.send(s, m.getString("Speed.Speed-Alterado-Voce").replace("%speed%", args[0]),
					m.getStringList("Speed.Speed-Alterado-Voce").stream()
							.map(message -> message.replace("%speed%", args[0])).collect(Collectors.toList()));
			return;
		}

		// Se o número de argumentos é 0 então a velocidade do sender é alterada
		if (args.length == 2) {

			// Verificando se o sender tem permisssão
			if (!s.hasPermission(pp.getString("Permissoes.Speed"))) {
				MessageUtils.send(s, m.getString("Speed.Speed-Outro-Sem-Permissao"),
						m.getStringList("Speed.Speed-Outro-Sem-Permissao"));
				return;
			}

			// Pegando o player e verificando se ele esta online
			Player p = Bukkit.getPlayer(args[1]);
			if (p == null) {
				MessageUtils.send(s, m.getString("Speed.Jogador"), m.getStringList("Speed.Jogador"));
				return;
			}

			// Verificando se o player não quer resetar o fly
			if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("reset")) {
				p.setFlySpeed(0.1f);
				p.setWalkSpeed(0.2f);
				MessageUtils.send(s,
						m.getString("Speed.Speed-Alterado-Outro").replace("%speed%", "PADRAO").replace("%player%",
								p.getName()),
						m.getStringList("Speed.Speed-Alterado-Outro").stream()
								.map(message -> message.replace("%speed%", "PADRAO").replace("%player%", p.getName()))
								.collect(Collectors.toList()));
				return;
			}

			// Verificando se o número é um número valido
			float speed;
			try {
				speed = Float.parseFloat(args[0]);
			} catch (NumberFormatException e) {
				MessageUtils.send(s,
						m.getString("Speed.Numero-Invalido").replace("%numero%", e.getMessage().split("\"")[1]),
						m.getStringList("Speed.Numero-Invalido").stream()
								.map(message -> message.replace("%numero%", e.getMessage().split("\"")[1]))
								.collect(Collectors.toList()));
				return;
			}

			// Verificando se a velocidade é valida (necessario bukkit)
			if (speed > 1.0f || speed < -1.0f) {
				MessageUtils.send(s, m.getString("Speed.Speed-Valor-Invalido"),
						m.getStringList("Speed.Speed-Valor-Invalido"));
				return;
			}

			// Setando o speed no player e informando
			p.setFlySpeed(speed);
			p.setWalkSpeed(speed);
			MessageUtils.send(s,
					m.getString("Speed.Speed-Alterado-Outro").replace("%speed%", args[0]).replace("%player%",
							p.getName()),
					m.getStringList("Speed.Speed-Alterado-Outro").stream()
							.map(message -> message.replace("%speed%", args[0]).replace("%player%", p.getName()))
							.collect(Collectors.toList()));
			return;
		}
		return;

	}

}
