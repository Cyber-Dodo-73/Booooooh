package fr.cyberdodo.booooooh.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class GlobalScarySoundCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final Random random = new Random();

    public GlobalScarySoundCommand(JavaPlugin plugin) {
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

        // Liste des sons effrayants disponibles (sans disques)
        Sound[] scarySounds = {
                Sound.AMBIENT_CAVE,
                Sound.ENTITY_WOLF_HOWL,
                Sound.ENTITY_WARDEN_HEARTBEAT,
                Sound.ENTITY_GHAST_SCREAM,
                Sound.ENTITY_PHANTOM_SWOOP,
                Sound.ENTITY_ZOMBIE_AMBIENT,
                Sound.ENTITY_CREEPER_PRIMED,
                Sound.ENTITY_WITHER_SPAWN,
                Sound.ENTITY_ENDER_DRAGON_GROWL
        };

        // Récupérer tous les joueurs connectés
        List<Player> players = (List<Player>) Bukkit.getOnlinePlayers();
        if (players.isEmpty()) {
            return false;
        }

        // Sélectionner un son aléatoire unique
        Sound randomSound = scarySounds[random.nextInt(scarySounds.length)];

        // Générer un pitch aléatoire entre 0.5 (lent) et 1.5 (rapide)
        float randomPitch = 0.5f + random.nextFloat() * (1.5f - 0.5f);

        // Exécuter un son effrayant aléatoire avec une durée modifiée pour chaque joueur
        for (Player player : players) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Jouer le son aléatoire avec une hauteur (pitch) modifiée
                    player.playSound(player.getLocation(), randomSound, 1.0f, randomPitch);
                }
            }.runTaskLater(plugin, random.nextInt(60)); // Délai aléatoire avant que le son ne joue (entre 0 et 3 secondes)
        }

        return true;
    }
}
