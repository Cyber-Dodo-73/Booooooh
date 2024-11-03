package fr.cyberdodo.booooooh.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class CreeperSpawnCommand implements CommandExecutor, Listener {

    private final JavaPlugin plugin;

    public CreeperSpawnCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        // Enregistrer cet objet en tant que Listener pour capturer les événements d'explosion
        Bukkit.getPluginManager().registerEvents(this, plugin);
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

        //get the player from args 0
        Player player = (Player) Bukkit.getPlayer(args[0]);

        if (player == null) {
            sender.sendMessage("Joueur introuvable !");
            return false;
        }

        Location playerLoc = player.getLocation();

        // Faire apparaître 10 creepers autour du joueur
        for (int i = 0; i < 10; i++) {
            Location spawnLocation = playerLoc.clone().add(
                    Math.random() * 6 - 3, // Position X autour du joueur
                    0,
                    Math.random() * 6 - 3  // Position Z autour du joueur
            );
            Creeper creeper = (Creeper) player.getWorld().spawnEntity(spawnLocation, EntityType.CREEPER);
            // Ajout d'une méta-donnée pour identifier ces creepers
            creeper.setMetadata("noDamage", new FixedMetadataValue(plugin, true));
        }
        return true;
    }

    // Gérer l'événement d'explosion des creepers pour éviter les dégâts
    @EventHandler
    public void onCreeperExplode(ExplosionPrimeEvent event) {
        if (event.getEntity() instanceof Creeper) {
            Creeper creeper = (Creeper) event.getEntity();
            // Vérifie si le creeper a la méta-donnée "noDamage"
            if (creeper.hasMetadata("noDamage")) {
                event.setCancelled(true);  // Annule les dégâts d'explosion seulement pour ces creepers
            }
        }
    }
}
