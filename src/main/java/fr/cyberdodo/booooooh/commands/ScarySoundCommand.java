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

public class ScarySoundCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public ScarySoundCommand(JavaPlugin plugin) {
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

        if (!(sender instanceof Player)) {
            sender.sendMessage("Seuls les joueurs peuvent utiliser cette commande.");
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage("Veuillez spécifier le nom du joueur !");
            return false;
        }

        // Récupérer le joueur à partir des arguments
        Player player = (Player) Bukkit.getPlayer(args[0]);

        if (player == null) {
            sender.sendMessage("Joueur introuvable !");
            return false;
        }

        // Jouer des sons au cours de 15 secondes
        new BukkitRunnable() {
            int tickCount = 0;

            @Override
            public void run() {
                // Arrêter la boucle après 15 secondes (300 ticks)
                if (tickCount >= 300) {
                    // Jouer un screamer à la fin
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 10.0f, 0.1f);  // Bruit de screamer
                    this.cancel();
                    return;
                }

                // Accélération et ralentissement des sons sur 15 secondes
                if (tickCount % 20 == 0) {
                    playScaryMelody(player, tickCount / 20);
                }

                tickCount++;
            }
        }.runTaskTimer(plugin, 0L, 1L); // Exécuter toutes les ticks (20 ticks = 1 seconde)

        player.sendMessage("Préparez-vous pour une mélodie effrayante !");
        return true;
    }

    private void playScaryMelody(Player player, int second) {
        switch (second) {
            case 0 -> player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 1.0f, 0.5f);  // Son lent
            case 1 -> player.playSound(player.getLocation(), Sound.ENTITY_WOLF_HOWL, 1.0f, 0.7f);  // Légèrement accéléré
            case 2 -> player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.0f, 0.4f);  // Son ralentit
            case 3 -> player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SCREAM, 1.0f, 1.2f);  // Accéléré
            case 4 -> player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 1.0f, 0.8f);  // Accélération lente
            case 5 -> player.playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1.0f, 0.5f);  // Grave et lent
            case 6 -> player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.5f);  // Très rapide
            case 8 -> player.playSound(player.getLocation(), Sound.ENTITY_WITCH_AMBIENT, 1.0f, 0.6f);  // Accélération légère
            case 9 -> player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 1.0f, 1.3f);  // Phantoms rapides
            case 10 -> player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 0.8f);  // Intensité moyenne
            case 11 -> player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.9f);  // Plus rapide
            case 13 -> player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_ROAR, 1.0f, 1.0f);  // Prépare pour la fin
            case 14 -> player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.6f);  // Son grave
            case 15 -> player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 10.0f, 0.1f);  // Scream final
        }
    }
}
