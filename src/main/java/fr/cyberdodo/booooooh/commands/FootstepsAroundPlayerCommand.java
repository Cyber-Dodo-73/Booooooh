package fr.cyberdodo.booooooh.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class FootstepsAroundPlayerCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public FootstepsAroundPlayerCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            String playerName = player.getName();
            if (!playerName.equalsIgnoreCase("Dodo_Report") && !playerName.equalsIgnoreCase("caat_1")) {
                player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'exécuter cette commande.");
                return true;
            }
        } else if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("Seuls Dodo_Report, caat_1 ou la console peuvent exécuter cette commande.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Veuillez spécifier le nom du joueur !");
            return false;
        }

        Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            sender.sendMessage("Joueur introuvable !");
            return false;
        }

        // Délai initial avant de commencer les sons
        int initialDelay = 0;

        // Commencer à jouer les sons de pas autour du joueur
        new BukkitRunnable() {
            double angle = 0;
            final double radius = 3; // Rayon du cercle autour du joueur
            final int totalSteps = 20; // Nombre total de pas
            int stepsPlayed = 0;

            @Override
            public void run() {
                if (stepsPlayed >= totalSteps) {
                    this.cancel();  // Arrêter la tâche une fois tous les pas joués
                    return;
                }

                // Calculer la position du son autour du joueur
                Location footstepLocation = getLocationAroundPlayer(player, radius, angle);

                // Jouer le son de pas à la position calculée
                player.playSound(footstepLocation, Sound.BLOCK_WOOD_STEP, 1.0f, 1.0f);

                stepsPlayed++;
                angle += (360.0 / totalSteps); // Avancer l'angle pour le prochain pas

                // Pas besoin de replanifier manuellement, runTaskTimer s'en charge
            }
        }.runTaskTimer(plugin, initialDelay, 5);  // Démarre après le délai initial et répète toutes les 5 ticks

        return true;
    }

    // Calcule une position à une certaine distance et angle autour du joueur
    private Location getLocationAroundPlayer(Player player, double radius, double angleDegrees) {
        Location playerLocation = player.getLocation();
        double angleRadians = Math.toRadians(angleDegrees);
        double xOffset = radius * Math.cos(angleRadians);
        double zOffset = radius * Math.sin(angleRadians);

        return playerLocation.clone().add(xOffset, 0, zOffset);
    }
}
