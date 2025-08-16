package me.nd.rankup.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import me.nd.rankup.Main;
import me.nd.rankup.listener.bloqueador.BloquearAbrirContainers;
import me.nd.rankup.listener.bloqueador.BloquearCairNoVoid;
import me.nd.rankup.listener.bloqueador.BloquearCama;
import me.nd.rankup.listener.bloqueador.BloquearCongelarAgua;
import me.nd.rankup.listener.bloqueador.BloquearCriarPortal;
import me.nd.rankup.listener.bloqueador.BloquearDerreterGeloENeve;
import me.nd.rankup.listener.bloqueador.BloquearExplodirItens;
import me.nd.rankup.listener.bloqueador.BloquearKickPorDuploLogin;
import me.nd.rankup.listener.bloqueador.BloquearKickPorDuploLoginSuper;
import me.nd.rankup.listener.bloqueador.BloquearMobsDePegaremFogoParaOSol;
import me.nd.rankup.listener.bloqueador.BloquearMobsDePegaremItensDoChao;
import me.nd.rankup.listener.bloqueador.BloquearNameTag;
import me.nd.rankup.listener.bloqueador.BloquearPassarDaBorda;
import me.nd.rankup.listener.bloqueador.BloquearQuebrarPlantacoesPulando;
import me.nd.rankup.listener.bloqueador.BloquearSubirEmVeiculos;
import me.nd.rankup.listener.bloqueador.BloquearSubirNoTetoNether;
import me.nd.rankup.listener.bloqueador.BloquearTeleportPorPortal;
import me.nd.rankup.listener.bloqueador.ChuvaListener;
import me.nd.rankup.listener.bloqueador.DayListener;
import me.nd.rankup.listener.bloqueador.DesativarDanoDoCacto;
import me.nd.rankup.listener.bloqueador.DesativarFomeNosMundos;
import me.nd.rankup.listener.bloqueador.DesativarMensagemDeEntrada;
import me.nd.rankup.listener.bloqueador.DesativarMensagemDeMorte;
import me.nd.rankup.listener.bloqueador.DesativarMensagemDeSaida;
import me.nd.rankup.listener.bloqueador.DesativarMobsNaturais;
import me.nd.rankup.listener.bloqueador.DesativarMobsNaturaisOLD;
import me.nd.rankup.listener.bloqueador.DesativarPropagacaoDoFogo;
import me.nd.rankup.listener.bloqueador.DesativarQuedaDaAreia;
import me.nd.rankup.listener.bloqueador.DesativarQuedaDaBigorna;
import me.nd.rankup.listener.bloqueador.DesativarQuedaDasFolhas;

public class Listenners {
	public static void setupListeners() {
		DayListener.stopDaylightCycle();
		DayListener.stopDaylightCycleOLD();
	    try {
	        PluginManager pm = Bukkit.getPluginManager();

	        Listener[] listeners = {
	            new RankupListener(),
	            new BloquearAbrirContainers(),
	            new BloquearCairNoVoid(),
	            new BloquearCama(),
	            new BloquearCongelarAgua(),
	            new BloquearCriarPortal(),
	            new BloquearCriarPortal(),
	            new BloquearDerreterGeloENeve(),
	            new BloquearExplodirItens(),
	            new BloquearKickPorDuploLogin(),
	            new BloquearKickPorDuploLoginSuper(),
	            new BloquearMobsDePegaremFogoParaOSol(),
	            new BloquearMobsDePegaremItensDoChao(),
	            new BloquearNameTag(),
	            new BloquearPassarDaBorda(),
	            new BloquearQuebrarPlantacoesPulando(),
	            new BloquearSubirEmVeiculos(),
	            new BloquearSubirNoTetoNether(),
	            new BloquearTeleportPorPortal(),
	            new ChuvaListener(),
	            new DesativarDanoDoCacto(),
	            new DesativarFomeNosMundos(),
	            new DesativarMensagemDeEntrada(),
	            new DesativarMensagemDeMorte(),
	            new DesativarMensagemDeSaida(),
	            new DesativarMobsNaturais(),
	            new DesativarMobsNaturaisOLD(),
	            new DesativarPropagacaoDoFogo(),
	            new DesativarQuedaDaAreia(),
	            new DesativarQuedaDaBigorna(),
	            new DesativarQuedaDasFolhas()
	            
	        };

	        for (Listener listener : listeners) {
	            registerEvent(pm, listener);
	        }
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	}

	private static void registerEvent(PluginManager pm, Listener listener) {
	    try {
	        pm.getClass().getDeclaredMethod("registerEvents", Listener.class, Plugin.class).invoke(pm, listener, Main.get());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
