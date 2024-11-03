package fr.cyberdodo.booooooh;

import fr.cyberdodo.booooooh.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BoooooohPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("scaryarmorstand").setExecutor(new ScaryArmorStandCommand(this));
        getCommand("creeperspawn").setExecutor(new CreeperSpawnCommand(this));
        getCommand("scarysound").setExecutor(new ScarySoundCommand(this));
        getCommand("globalscarysound").setExecutor(new GlobalScarySoundCommand(this));
        getCommand("footstepsaround").setExecutor(new FootstepsAroundPlayerCommand(this));
        getCommand("simulateattack").setExecutor(new SimulateMobAttackCommand(this));
        getCommand("putpumpkin").setExecutor(new PutPumpkinOnHeadCommand(this));
        getCommand("skyfall").setExecutor(new SkyFallCommand(this));
        getCommand("stalkingzombie").setExecutor(new StalkingZombieCommand(this));
        getCommand("screamerdoor").setExecutor(new ScreamerDoorCommand(this));

        // Halloween Show
        getCommand("halloweenshow").setExecutor(new HalloweenShowCommand(this));

        scheduleRandomCommandTask();
        // Messages d'activation
        getLogger().info("Le plugin Booooooh est activé !");
    }



    @Override
    public void onDisable() {
        // Actions à effectuer lors de la désactivation du plugin (si nécessaire)
        getLogger().info("Le plugin Booooooh est désactivé.");
    }

    private void scheduleRandomCommandTask() {
        // Tâche répétitive qui s'exécute toutes les 30 minutes (36 000 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                // Exécuter la commande sur un joueur aléatoire
                executeRandomCommandOnRandomPlayer();
            }
        }.runTaskTimer(this, 0L, 36000L); // 36 000 ticks = 30 minutes
    }


    private void executeRandomCommandOnRandomPlayer() {
        // Obtenir la liste des joueurs en ligne
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (onlinePlayers.isEmpty()) {
            // Personne n'est en ligne, ne rien faire
            return;
        }

        // Sélectionner un joueur aléatoire
        Random random = new Random();
        Player randomPlayer = onlinePlayers.get(random.nextInt(onlinePlayers.size()));

        // Liste des commandes à exécuter
        String[] commands = {
                "scaryarmorstand",
                "creeperspawn",
                "scarysound",
                "globalscarysound",
                "footstepsaround",
                "simulateattack",
                "putpumpkin",
                "skyfall",
                "stalkingzombie",
                "screamerdoor"
        };

        // Sélectionner une commande aléatoire
        String randomCommand = commands[random.nextInt(commands.length)];

        // Préparer la commande à exécuter
        String commandToExecute;

        if (randomCommand.equalsIgnoreCase("scaryarmorstand")) {
            // Générer un nombre aléatoire entre 1 et 7
            int randomNumber = random.nextInt(7) + 1; // Génère un nombre entre 1 et 7
            // Construire la commande avec le joueur et le nombre
            commandToExecute = randomCommand + " " + randomPlayer.getName() + " " + randomNumber;
        } else if (randomCommand.equalsIgnoreCase("globalscarysound")) {
            // Pour cette commande, pas besoin de spécifier un joueur
            commandToExecute = randomCommand;
        } else {
            // Pour les autres commandes, inclure le nom du joueur
            commandToExecute = randomCommand + " " + randomPlayer.getName();
        }

        // Exécuter la commande en tant que console
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToExecute);
    }



}
