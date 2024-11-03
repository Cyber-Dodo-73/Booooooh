package fr.cyberdodo.booooooh.commands;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Door;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ScreamerDoorCommand implements CommandExecutor, Listener {

    private final JavaPlugin plugin;

    // Set pour stocker les joueurs ciblés
    private final Set<UUID> targetedPlayers = new HashSet<>();

    public ScreamerDoorCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // Commande /mention <joueur>
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
            sender.sendMessage("Veuillez spécifier le nom du joueur à mentionner !");
            return false;
        }

        // Obtenir le joueur ciblé
        Player targetPlayer = Bukkit.getPlayer(args[0]);

        if (targetPlayer == null) {
            sender.sendMessage("Joueur introuvable !");
            return false;
        }

        // Ajouter le joueur à la liste des joueurs ciblés
        targetedPlayers.add(targetPlayer.getUniqueId());
        sender.sendMessage("Le joueur " + targetPlayer.getName() + " sera surpris lors de sa prochaine ouverture de porte.");

        return true;
    }

    // Événement lorsqu'un joueur interagit avec un bloc
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Vérifier si le joueur est ciblé
        if (!targetedPlayers.contains(player.getUniqueId())) {
            return;
        }

        // Vérifier si le joueur a cliqué sur une porte
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null && Tag.DOORS.isTagged(clickedBlock.getType())) {

            // Obtenir l'état actuel de la porte
            Door door = (Door) clickedBlock.getBlockData();
            boolean isOpenBefore = door.isOpen();

            // Attendre un tick pour que l'état de la porte soit mis à jour
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Obtenir le nouvel état de la porte
                    Door newDoor = (Door) clickedBlock.getBlockData();
                    boolean isOpenAfter = newDoor.isOpen();

                    // Vérifier si la porte était fermée et vient d'être ouverte
                    if (!isOpenBefore && isOpenAfter) {
                        // Retirer le joueur de la liste pour que cela ne se produise qu'une fois
                        targetedPlayers.remove(player.getUniqueId());

                        // Calculer la position pour faire apparaître le screamer derrière la porte
                        Location spawnLocation = getScreamerSpawnLocation(clickedBlock, newDoor, player);

                        // Créer le screamer
                        createScreamer(player, spawnLocation);
                    }
                }
            }.runTaskLater(plugin, 1L); // Exécuter un tick plus tard
        }
    }

    // Méthode pour calculer la position du screamer
    private Location getScreamerSpawnLocation(Block doorBlock, Door doorData, Player player) {
        // Obtenir la position centrée de la porte
        Location doorLocation = doorBlock.getLocation().add(0.5, -1, 0.5); // Centrer sur le bloc et abaisser d'un bloc

        // Obtenir le vecteur de la porte vers le joueur
        Vector doorToPlayer = player.getLocation().toVector().subtract(doorLocation.toVector()).normalize();

        // Obtenir la direction dans laquelle la porte fait face
        Vector doorFacing = doorData.getFacing().getDirection();

        // Calculer le produit scalaire pour déterminer de quel côté se trouve le joueur
        double dotProduct = doorToPlayer.dot(doorFacing);

        // Si le produit scalaire est positif, le joueur est du côté vers lequel la porte fait face
        // Nous voulons que le screamer apparaisse du côté opposé
        Vector spawnDirection;
        if (dotProduct > 0) {
            // Le joueur est du côté vers lequel la porte fait face
            spawnDirection = doorFacing.clone().multiply(-1);
        } else {
            // Le joueur est du côté opposé
            spawnDirection = doorFacing.clone();
        }

        // Calculer la position du screamer
        Location spawnLocation = doorLocation.clone().add(spawnDirection.multiply(1.5)); // Ajuster la distance selon les besoins

        // Ajuster la hauteur pour être au niveau du sol
        spawnLocation.setY(doorLocation.getY());

        // Faire face au joueur
        spawnLocation.setDirection(player.getLocation().toVector().subtract(spawnLocation.toVector()));

        return spawnLocation;
    }

    // Méthode pour créer le screamer
    private void createScreamer(Player player, Location spawnLocation) {
        // Faire apparaître l'armor stand
        ArmorStand screamer = (ArmorStand) player.getWorld().spawnEntity(spawnLocation, EntityType.ARMOR_STAND);

        // Configurer l'armor stand
        screamer.setVisible(false);
        screamer.setInvisible(true);
        screamer.setInvulnerable(true);
        screamer.setGravity(false);
        screamer.setCollidable(false);
        screamer.setBasePlate(false);
        screamer.setArms(false);
        screamer.setSmall(false);
        screamer.setMetadata("screamer", new FixedMetadataValue(plugin, true));

        // Ajouter une tête effrayante (par exemple, une tête de creeper)
        ItemStack headItem = new ItemStack(Material.CREEPER_HEAD);
        screamer.getEquipment().setHelmet(headItem);

        // Appliquer l'effet de cécité au joueur
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));

        // Jouer un son effrayant
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);

        // Faire avancer le screamer en ligne droite rapidement
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!screamer.isDead() && player.isOnline()) {
                    // Calculer la direction vers le joueur
                    Vector direction = player.getLocation().toVector().subtract(screamer.getLocation().toVector()).normalize();
                    screamer.setVelocity(direction.multiply(1.5)); // Vitesse plus élevée pour un effet effrayant
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); // Exécuter à chaque tick

        // Supprimer le screamer après un certain temps
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!screamer.isDead()) {
                    screamer.remove();
                }
            }
        }.runTaskLater(plugin, 60L); // Supprimer après 3 secondes (60 ticks)
    }

    // Nettoyer lorsque le joueur se déconnecte
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        targetedPlayers.remove(player.getUniqueId());
    }
}
