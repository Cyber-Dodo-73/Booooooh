package fr.cyberdodo.booooooh.commands;

import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;

public class SkyFallCommand implements CommandExecutor, Listener {

    private final JavaPlugin plugin;

    // Pour stocker les joueurs en chute libre
    private final Set<Player> fallingPlayers = new HashSet<>();

    // Pour stocker la position initiale des joueurs
    private final Map<Player, Location> initialLocations = new HashMap<>();

    public SkyFallCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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

        // Obtenir le joueur
        Player player = (Player) sender;

        Player targetPlayer = Bukkit.getPlayer(args[0]);

        if (targetPlayer == null) {
            sender.sendMessage("Joueur introuvable !");
            return false;
        }

        // Vérifier si le joueur est déjà en chute libre
        if (fallingPlayers.contains(player)) {
            player.sendMessage("Vous êtes déjà en chute libre !");
            return true;
        }

        // Stocker la position initiale du joueur
        Location initialLocation = targetPlayer.getLocation();
        initialLocations.put(targetPlayer, initialLocation);

        // Ajouter le joueur à l'ensemble des joueurs en chute
        fallingPlayers.add(targetPlayer);

        // Obtenir le monde du joueur
        World world = targetPlayer.getWorld();

        // Obtenir les coordonnées X et Z actuelles du joueur
        double x = initialLocation.getX();
        double z = initialLocation.getZ();

        // Obtenir la hauteur maximale du monde
        int maxHeight = world.getMaxHeight();

        // Créer une nouvelle position à la hauteur maximale
        Location skyLocation = new Location(world, x, maxHeight - 1, z);

        // Téléporter le joueur à la nouvelle position
        targetPlayer.teleport(skyLocation);

        // Jouer un son pour indiquer la téléportation
        targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

        // Optionnel : Envoyer un message au joueur
        targetPlayer.sendMessage("Vous êtes téléporté dans le ciel. Bonne chute !");

        return true;
    }

    // Événement pour détecter lorsque le joueur touche le sol
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Vérifier si le joueur est dans l'ensemble des joueurs en chute libre
        if (fallingPlayers.contains(player)) {
            // Vérifier si le joueur était en l'air et vient de toucher le sol
            if (!player.isFlying() && player.isOnGround()) {
                // Annuler les dégâts de chute
                player.setFallDistance(0);

                // Récupérer la position initiale du joueur
                Location initialLocation = initialLocations.get(player);

                if (initialLocation != null) {
                    // Téléporter le joueur à sa position initiale
                    player.teleport(initialLocation);

                    // Jouer un son pour indiquer le retour
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

                    // Optionnel : Envoyer un message au joueur
                    player.sendMessage("Vous êtes de retour à votre position initiale.");
                }

                // Retirer le joueur de l'ensemble des joueurs en chute
                fallingPlayers.remove(player);
                initialLocations.remove(player);
            }
        }
    }
}
