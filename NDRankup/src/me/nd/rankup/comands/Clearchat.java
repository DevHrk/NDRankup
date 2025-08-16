package me.nd.rankup.comands;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import me.nd.rankup.Main;
import me.nd.rankup.plugin.SConfig;
import me.nd.rankup.utils.StringUtils;
import me.nd.rankup.utils.messages.MessageUtils;

public class Clearchat extends Commands {
	private static final String CLEAR_CHAT_PERMISSION = "Permissoes.ClearChat";
	private static final String CLEAR_CHAT_MESSAGE = "ClearChat.SemPerm";
	@SuppressWarnings("unused")
	private static final String CLEAR_CHAT_NOTICE = "ClearChat.Aviso-Que-O-Chat-Limpo-Global";
	private static final String CLEAR_CHAT_GLOBAL_NOTICE = "ClearChat.Aviso-Que-O-Chat-Limpo-Global";

	public Clearchat() {
		super("clearchat");
	}

	@Override
	public void perform(CommandSender sender, String label, String[] args) {
		if (!hasPermission(sender)) {
			sendNoPermissionMessage(sender);
			return;
		}

		clearChat();
		if (shouldNotifyChatCleared()) {
			sendChatClearedNotification(sender);
		}
	}

	private boolean hasPermission(CommandSender sender) {
		SConfig permissionsConfig = Main.get().getConfig("Permissoes");
		return sender.hasPermission(permissionsConfig.getString(CLEAR_CHAT_PERMISSION));
	}

	private void sendNoPermissionMessage(CommandSender sender) {
		SConfig messagesConfig = Main.get().getConfig("Mensagens");
		MessageUtils.send(sender, messagesConfig.getString(CLEAR_CHAT_MESSAGE),
				messagesConfig.getStringList(CLEAR_CHAT_MESSAGE));
	}

	private void clearChat() {
		String clearChatMessage = StringUtils.repeat(" §c \n §c ", 100);
		Bukkit.broadcastMessage(clearChatMessage);
	}

	private boolean shouldNotifyChatCleared() {
		SConfig messagesConfig = Main.get().getConfig("Mensagens");
		return messagesConfig.getBoolean("ClearChat.Avisar-Que-O-Chat-Foi-Limpo");
	}

	private void sendChatClearedNotification(CommandSender sender) {
		SConfig messagesConfig = Main.get().getConfig("Mensagens");
		String notificationMessage = messagesConfig.getString(CLEAR_CHAT_GLOBAL_NOTICE).replace("%player%",
				sender.getName());
		List<String> notificationList = messagesConfig.getStringList(CLEAR_CHAT_GLOBAL_NOTICE).stream()
				.map(message -> message.replace("%player%", sender.getName())).collect(Collectors.toList());
		MessageUtils.send(sender, notificationMessage, notificationList);
	}
}