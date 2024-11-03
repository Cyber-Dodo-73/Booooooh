package fr.cyberdodo.booooooh.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StalkingZombieCommand implements CommandExecutor, Listener {

    private final JavaPlugin plugin;

    // Map pour stocker le zombie associé à chaque joueur
    private final Map<Player, ZombieData> playerZombies = new HashMap<>();

    public StalkingZombieCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // Classe pour stocker le zombie et son état de visibilité
    private static class ZombieData {
        Zombie zombie;
        boolean isHidden;

        public ZombieData(Zombie zombie) {
            this.zombie = zombie;
            this.isHidden = false;
        }
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

        // Obtenir le joueur à partir des arguments
        Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            sender.sendMessage("Joueur introuvable !");
            return false;
        }

        // Vérifier si le zombie existe déjà pour ce joueur
        if (playerZombies.containsKey(player) && !playerZombies.get(player).zombie.isDead()) {
            sender.sendMessage("Le zombie suit déjà ce joueur !");
            return true;
        }

        // Obtenir le monde du joueur
        World world = player.getWorld();

        // Trouver une position sécurisée derrière le joueur
        Location spawnLocation = findSafeSpawnLocation(player);

        if (spawnLocation == null) {
            sender.sendMessage("Impossible de trouver un endroit sûr pour faire apparaître le zombie.");
            return true;
        }

        // Faire apparaître le zombie
        Zombie stalkingZombie = (Zombie) world.spawnEntity(spawnLocation, EntityType.ZOMBIE);

        // Configurer le zombie
        stalkingZombie.setSilent(true);
        stalkingZombie.setBaby(false);
        stalkingZombie.setAI(true); // Activer l'IA pour le pathfinding
        stalkingZombie.setPersistent(true);
        stalkingZombie.setCollidable(false);

        // Ajouter une métadonnée pour identifier le zombie
        stalkingZombie.setMetadata("noBurn", new FixedMetadataValue(plugin, true));
        stalkingZombie.setMetadata("targetPlayer", new FixedMetadataValue(plugin, player.getName()));

        // Configurer les attributs du zombie
        stalkingZombie.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.35);
        stalkingZombie.setTarget(player);
        stalkingZombie.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(100);
        stalkingZombie.setVisualFire(false);
        stalkingZombie.setCanBreakDoors(true);
        stalkingZombie.setCanPickupItems(false);
        stalkingZombie.setFireTicks(0);

        // Créer l'objet ZombieData pour stocker le zombie et son état de visibilité
        ZombieData zombieData = new ZombieData(stalkingZombie);
        playerZombies.put(player, zombieData);

        // Récupérer l'UUID du joueur "JaviRAZ" et équiper le zombie avec sa tête
        String skullOwner = "JaviRAZ";
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String uuid = getUUIDFromAPI(skullOwner);
                    if (uuid == null) {
                        Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage("Impossible de trouver le joueur : " + skullOwner));
                        return;
                    }

                    // Équiper le zombie avec la tête du joueur
                    Bukkit.getScheduler().runTask(plugin, () -> equipZombie(stalkingZombie, uuid));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);

        // Tâche répétitive pour gérer la visibilité du zombie
        new BukkitRunnable() {
            @Override
            public void run() {
                // Vérifier si le zombie ou le joueur n'existent plus
                if (stalkingZombie == null || stalkingZombie.isDead() || !player.isOnline()) {
                    if (stalkingZombie != null && !stalkingZombie.isDead()) {
                        stalkingZombie.remove();
                    }
                    playerZombies.remove(player);
                    this.cancel();
                    return;
                }

                // Vérifier si le joueur regarde le zombie
                if (isPlayerLookingAtEntity(player, stalkingZombie)) {
                    // Si le zombie est visible au joueur, le cacher
                    if (!zombieData.isHidden) {
                        player.hideEntity(plugin, stalkingZombie);
                        zombieData.isHidden = true;
                        stalkingZombie.getWorld().playSound(stalkingZombie.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                        stalkingZombie.setTarget(null); // Arrêter d'attaquer le joueur

                        // Téléporter le zombie derrière le joueur
                        Location newLocation = findSafeSpawnLocation(player);
                        if (newLocation != null) {
                            stalkingZombie.teleport(newLocation);
                        }
                    }
                } else {
                    // Si le zombie est caché du joueur, le montrer
                    if (zombieData.isHidden) {
                        player.showEntity(plugin, stalkingZombie);
                        zombieData.isHidden = false;
                        stalkingZombie.getWorld().playSound(stalkingZombie.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                        stalkingZombie.setTarget(player); // Reprendre la poursuite du joueur
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 5L); // Exécuter toutes les 5 ticks (0.25 seconde)

        player.sendMessage("Ne regarde pas derrière toi...");

        return true;
    }

    // Méthode pour équiper le zombie avec la tête du joueur spécifié et une armure en diamant
    private void equipZombie(Zombie zombie, String uuid) {
        // Créer un ItemStack pour la tête du joueur
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();

        // Définir le profil du joueur sur la tête
        skullMeta.setOwnerProfile(Bukkit.createPlayerProfile(UUID.fromString(uuid)));
        playerHead.setItemMeta(skullMeta);

        // Créer l'armure en diamant
        ItemStack diamondChestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemStack diamondLeggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        ItemStack diamondBoots = new ItemStack(Material.DIAMOND_BOOTS);

        // Équiper le zombie
        zombie.getEquipment().setHelmet(playerHead);
        zombie.getEquipment().setChestplate(diamondChestplate);
        zombie.getEquipment().setLeggings(diamondLeggings);
        zombie.getEquipment().setBoots(diamondBoots);

        // Ajuster les probabilités de drop à zéro pour éviter que le joueur ne puisse obtenir l'équipement
        zombie.getEquipment().setHelmetDropChance(0.0f);
        zombie.getEquipment().setChestplateDropChance(0.0f);
        zombie.getEquipment().setLeggingsDropChance(0.0f);
        zombie.getEquipment().setBootsDropChance(0.0f);
    }

    // Méthode pour récupérer l'UUID du joueur depuis l'API Mojang
    private String getUUIDFromAPI(String playerName) throws Exception {
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        if (connection.getResponseCode() == 200) {
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            return jsonObject.get("id").getAsString().replaceAll(
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
            );  // Formater l'UUID avec des tirets
        }
        return null;
    }

    // Méthode pour trouver une position sûre derrière le joueur
    private Location findSafeSpawnLocation(Player player) {
        Location playerLocation = player.getLocation();
        World world = player.getWorld();

        // Direction opposée au regard du joueur
        Location behindPlayer = playerLocation.clone().add(playerLocation.getDirection().multiply(-5));

        // Arrondir les coordonnées
        int x = behindPlayer.getBlockX();
        int y = behindPlayer.getBlockY();
        int z = behindPlayer.getBlockZ();

        // Vérifier les positions autour pour trouver un espace avec au moins deux blocs d'air
        for (int dy = -1; dy <= 1; dy++) {
            Location potentialLocation = new Location(world, x + 0.5, y + dy, z + 0.5);
            if (isLocationSafe(potentialLocation)) {
                return potentialLocation;
            }
        }

        return null; // Aucune position sûre trouvée
    }

    // Vérifier si l'emplacement est sûr pour faire apparaître le zombie
    private boolean isLocationSafe(Location location) {
        World world = location.getWorld();
        Location loc1 = location.clone();
        Location loc2 = location.clone().add(0, 1, 0);

        return world.getBlockAt(loc1).isPassable() && world.getBlockAt(loc2).isPassable();
    }

    // Méthode pour vérifier si le joueur regarde l'entité
    private boolean isPlayerLookingAtEntity(Player player, Entity entity) {
        Location eyeLocation = player.getEyeLocation();
        Vector toEntity = entity.getLocation().add(0, entity.getHeight() / 2.0, 0).toVector().subtract(eyeLocation.toVector());
        double distanceSquared = toEntity.lengthSquared();

        // Vérifier si l'entité est dans un rayon raisonnable
        if (distanceSquared > 1024 && entity.hasMetadata("targetPlayer") && entity.getMetadata("targetPlayer").equals(player.getName())) { // 32 blocs de distance
            return false;
        }

        Vector direction = eyeLocation.getDirection().normalize();
        double dot = toEntity.normalize().dot(direction);

        return dot > 0.98D; // Ajuster la précision si nécessaire
    }

    // Nettoyer lorsque le joueur se déconnecte ou lorsque le zombie meurt
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ZombieData zombieData = playerZombies.get(player);
        if (zombieData != null && zombieData.zombie != null && !zombieData.zombie.isDead()) {
            zombieData.zombie.remove();
            playerZombies.remove(player);
        }
    }

    @EventHandler
    public void onZombieDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Zombie) {
            Zombie zombie = (Zombie) entity;
            Player player = null;
            for (Map.Entry<Player, ZombieData> entry : playerZombies.entrySet()) {
                if (entry.getValue().zombie.equals(zombie)) {
                    player = entry.getKey();
                    break;
                }
            }
            if (player != null) {
                playerZombies.remove(player);
            }
        }
    }

    // Événement pour annuler les dégâts de feu et enlever l'effet de feu pour les zombies avec la métadonnée "noBurn"
    @EventHandler
    public void onZombieDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Zombie) {
            Zombie zombie = (Zombie) entity;
            if (zombie.hasMetadata("noBurn")) {
                // Vérifier si les dégâts sont causés par le feu
                if (event.getCause() == EntityDamageEvent.DamageCause.FIRE ||
                        event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
                        event.getCause() == EntityDamageEvent.DamageCause.LAVA ||
                        event.getCause() == EntityDamageEvent.DamageCause.HOT_FLOOR) {
                    event.setCancelled(true);
                    zombie.setFireTicks(0); // Enlever l'effet de feu
                }
            }
        }
    }

    // Événement pour empêcher le zombie de prendre feu visuellement
    @EventHandler
    public void onZombieCombust(EntityCombustEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Zombie) {
            Zombie zombie = (Zombie) entity;
            if (zombie.hasMetadata("noBurn")) {
                event.setCancelled(true);
                zombie.setFireTicks(0); // Enlever l'effet de feu
            }
        }
    }
}
