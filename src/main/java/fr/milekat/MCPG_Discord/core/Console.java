package fr.milekat.MCPG_Discord.core;

import fr.milekat.MCPG_Discord.Main;
import fr.milekat.MCPG_Discord.bot.BotManager;
import net.dv8tion.jda.api.OnlineStatus;

import java.util.Scanner;

public class Console {
    public Console() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("help")) {
                    sendHelp();
                } else if (input.equalsIgnoreCase("stop")) {
                    stopSequence();
                } else if (input.equalsIgnoreCase("reload messages")) {
                    BotManager.reloadMsg();
                } else if (input.equalsIgnoreCase("reload channel")) {
                    BotManager.reloadCh();
                } else if (input.equalsIgnoreCase("debug")) {
                    debug();
                } else {
                    Main.log("Commande inconnue");
                    sendHelp();
                }
            }
        }
    }

    /**
     * Liste des commandes dispo pour la console du bot
     */
    private void sendHelp() {
        Main.log("help: Envoi ce message.");
        Main.log("reload messages: Update les messages du bot.");
        Main.log("reload channel: Update les id de channels pour le bot.");
        Main.log("debug: Active/Désactive le débug.");
        Main.log("stop: Stop le bot !");
    }

    /**
     * Disconnect the bot
     */
    private void stopSequence() {
        Main.log("Déconnexion du bot...");
        Main.getJda().getPresence().setStatus(OnlineStatus.OFFLINE);
        Main.log("Good bye!");
        System.exit(0);
    }

    /**
     * Passe en mode debug (throwable Java)
     */
    private void debug() {
        Main.debugExeptions = !Main.debugExeptions;
        Main.log("Mode debug: " + Main.debugExeptions + ".");
    }
}
