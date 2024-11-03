package fr.cyberdodo.booooooh.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class ScaryArmorStandCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public ScaryArmorStandCommand(JavaPlugin plugin) {
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

        if (args.length == 1) {
            sender.sendMessage("Veuillez spécifier la tete !");
            return false;
        }

        //get the player from args 0
        Player player = (Player) Bukkit.getPlayer(args[0]);

        if (player == null) {
            sender.sendMessage("Joueur introuvable !");
            return false;
        }

        String skullId = args[1];
        String skullName = switch (skullId) {
            case "1" -> "MHF_Herobrine";
            case "2" -> "Valyto_nolan";
            case "3" -> "Amado13222";
            case "4" -> "Lxjr";
            case "5" -> "zazaroyaume2";
            case "6" -> "Xx_IsaacCraft_xX";
            case "7" -> "The_Edge_Of_Dark";
            default -> "MHF_Herobrine";
        };

        // Récupérer l'UUID du joueur à partir de son pseudo via l'API Mojang
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String uuid = getUUIDFromAPI(skullName);
                    if (uuid == null) {
                        player.sendMessage("Impossible de trouver le joueur : " + skullName);
                        return;
                    }

                    // Créer l'armor stand avec la tête du joueur
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Location playerLoc = player.getEyeLocation();  // Obtenir la position des yeux du joueur

                        // Positionner l'armor stand plus loin et plus bas
                        Location armorStandLocation = playerLoc.clone().add(playerLoc.getDirection().normalize().multiply(2)); // Distance de 2 blocs

                        // Ajuster la position verticale pour que l'armor stand soit plus bas
                        armorStandLocation.setY(playerLoc.getY() - 1.0);  // Ajustement pour être plus bas

                        ArmorStand armorStand = armorStandLocation.getWorld().spawn(armorStandLocation, ArmorStand.class, as -> {
                            as.setInvisible(true);
                            as.setInvulnerable(true);
                            as.setGravity(false);
                            as.setCanPickupItems(false);
                            as.setMarker(true);  // Empêche les interactions et collisions
                            as.setCustomNameVisible(false);
                            as.setRotation(playerLoc.getYaw() + 180, 0);
                        });

                        // Appliquer la tête du joueur
                        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
                        skullMeta.setOwnerProfile(Bukkit.createPlayerProfile(UUID.fromString(uuid)));
                        skull.setItemMeta(skullMeta);
                        armorStand.setHelmet(skull);

                        // Effets sonores et visuels
                        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 1));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 3));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 5));
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1f);
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SCREAM, 5f, 0.1f);

                        // Faire suivre la tête de l'armor stand et le déplacer pour rester devant le joueur
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (armorStand.isDead() || !player.isOnline()) {
                                    this.cancel();
                                }

                                // Mettre à jour la position de l'armor stand pour rester devant le joueur
                                Location updatedPlayerLoc = player.getEyeLocation();
                                Location newArmorStandLoc = updatedPlayerLoc.clone().add(updatedPlayerLoc.getDirection().normalize().multiply(2)); // Distance ajustée à 2 blocs

                                // Garder l'armor stand plus bas
                                newArmorStandLoc.setY(updatedPlayerLoc.getY() - 2.0);  // Ajustement de la hauteur pour être plus bas
                                armorStand.teleport(newArmorStandLoc);
                                armorStand.setCanPickupItems(false);

                                // Garder la même rotation (direction opposée)
                                armorStand.setRotation(updatedPlayerLoc.getYaw() + 180, 0);
                            }
                        }.runTaskTimer(plugin, 0L, 1L);  // Exécuter toutes les ticks

                        // Supprimer l'armor stand après 10 secondes
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                armorStand.remove();
                            }
                        }.runTaskLater(plugin, 180L);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);

        return true;
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
}
