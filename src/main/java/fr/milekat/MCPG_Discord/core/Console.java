package fr.milekat.MCPG_Discord.core;

import fr.milekat.MCPG_Discord.Main;
import net.dv8tion.jda.api.OnlineStatus;

import java.util.Scanner;

public class Console {
    public Console() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("stop")) {
                    stopSequence();
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
        Main.log("debug: Active/Désactive le débug.");
        Main.log("stop: Stop le bot !");
    }

    private void stopSequence() {
        Main.log("Déconnexion du bot...");
        Main.getJda().getPresence().setStatus(OnlineStatus.OFFLINE);
        Main.log("Good bye!");
        System.exit(0);
    }

    private void debug() {
        Main.debugExeptions = !Main.debugExeptions;
        Main.log("Mode débug: " + Main.debugExeptions + ".");
    }
}
