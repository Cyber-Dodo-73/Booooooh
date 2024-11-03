package fr.cyberdodo.booooooh.commands;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.*;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HalloweenShowCommand implements CommandExecutor, Listener {

    private final JavaPlugin plugin;
    private Zombie boss; // Référence au boss

    private final List<EnderCrystal> spawnedCrystals = new ArrayList<>();


    public HalloweenShowCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin); // Enregistrer les événements
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Seuls les joueurs peuvent exécuter cette commande.");
            return false;
        }

        Player player = (Player) sender;
        if (!player.getName().equals("Dodo_Report")) {
            player.sendMessage("Désolé, cette commande est réservée à Dodo_Report.");
            return false;
        }

        startHalloweenShow(player);
        return true;
    }

    private void startHalloweenShow(Player player) {
        Location center = player.getLocation();
        World world = center.getWorld();
        if (world == null) return;

        world.setTime(18000);
        world.playSound(center, Sound.AMBIENT_CAVE, 2.0f, 0.5f);
        world.setStorm(true);

        // Phase 1: Invocation des Cristaux avec Faisceaux
        new BukkitRunnable() {
            int crystalsSpawned = 0;

            @Override
            public void run() {
                if (crystalsSpawned < 8) {
                    double angle = crystalsSpawned * (2 * Math.PI / 8); // 8 cristaux en cercle
                    Location crystalLocation = center.clone().add(Math.cos(angle) * 8, -1, Math.sin(angle) * 8);
                    EnderCrystal crystal = (EnderCrystal) world.spawnEntity(crystalLocation, EntityType.END_CRYSTAL);
                    crystal.setInvulnerable(true);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            crystalLocation.add(0, 0.1, 0);
                            crystal.teleport(crystalLocation);
                            if (crystalLocation.getY() >= center.getY()) {
                                crystal.setBeamTarget(center.clone().add(0, 10, 0));
                                // Particules au-dessus du joueur lors de chaque apparition de faisceau
                                world.spawnParticle(Particle.WITCH, center.clone().add(0, 10, 0), 200, 1, 1, 1, 0.1);
                                world.playSound(center.clone().add(0, 10, 0), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.5f);
                                crystalsSpawned++;
                                this.cancel();
                            }
                        }
                    }.runTaskTimer(plugin, 0L, 2L);
                } else {
                    this.cancel();
                    startPhase2(player, center);
                }
            }
        }.runTaskTimer(plugin, 0L, 60L);
    }

    private void startPhase2(Player player, Location center) {
        World world = center.getWorld();
        if (world == null) return;

        // Phase 2: Effets de Particules et Explosion Finale
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks == 0) {
                    for (int i = 0; i < 8; i++) {
                        double angle = i * (2 * Math.PI / 8);
                        Location crystalLocation = center.clone().add(Math.cos(angle) * 8, 0, Math.sin(angle) * 8);
                        EnderCrystal crystal = (EnderCrystal) world.spawnEntity(crystalLocation, EntityType.END_CRYSTAL);
                        crystal.setBeamTarget(center.clone().add(0, 10, 0));
                        spawnedCrystals.add(crystal);
                        crystal.setInvulnerable(true);

                        // Effets de particules constants autour des cristaux
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (crystal.isValid()) {
                                    world.spawnParticle(Particle.ENCHANTED_HIT, crystal.getLocation().add(0, 2, 0), 300, 2.0, 2.0, 2.0, 0.3);
                                    world.playSound(crystal.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.5f, 1.0f);
                                    world.playSound(crystal.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
                                } else {
                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(plugin, 0L, 5L);
                    }
                }

                if (ticks == 50) {
                    // Explosion et lancement des feux d'artifice
                    world.spawnParticle(Particle.EXPLOSION_EMITTER, center.clone().add(0, 5, 0), 10);
                    world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 4.0f, 0.5f);

                    // Pause de 3 secondes avant de commencer le lancement des feux d'artifice
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Lancement de 250 feux d'artifice en 15 secondes
                            new BukkitRunnable() {
                                int fireworksLaunched = 0;

                                @Override
                                public void run() {
                                    if (fireworksLaunched < 250) {
                                        for (int i = 0; i < 5; i++) { // Lancer 5 feux d'artifice à la fois
                                            Location fireworkLocation = center.clone().add(Math.random() * 16 - 8, 0, Math.random() * 16 - 8);
                                            fireworkLocation.setY(world.getHighestBlockYAt(fireworkLocation) + 1);
                                            Firework firework = (Firework) world.spawnEntity(fireworkLocation, EntityType.FIREWORK_ROCKET);
                                            FireworkMeta meta = firework.getFireworkMeta();

                                            // Propriétés aléatoires pour le feu d'artifice
                                            Random random = new Random();
                                            int power = random.nextInt(2) + 1; // Puissance entre 1 et 2
                                            meta.setPower(power);

                                            FireworkEffect.Type type = FireworkEffect.Type.values()[random.nextInt(FireworkEffect.Type.values().length)];

                                            // Couleurs noir et orange
                                            Color color1 = Color.BLACK;
                                            Color color2 = Color.ORANGE;

                                            meta.addEffect(FireworkEffect.builder()
                                                    .withColor(color1, color2)
                                                    .withFade(color2)
                                                    .with(type)
                                                    .trail(random.nextBoolean())
                                                    .flicker(random.nextBoolean())
                                                    .build());
                                            firework.setFireworkMeta(meta);

                                            // Effets de chauves-souris
                                            world.spawnEntity(fireworkLocation, EntityType.BAT);
                                        }
                                        fireworksLaunched += 5;
                                    } else {
                                        // Explosion finale des Ender Crystals
                                        for (Entity entity : world.getEntities()) {
                                            if (entity instanceof EnderCrystal) {
                                                world.spawnParticle(Particle.EXPLOSION_EMITTER, entity.getLocation(), 10);
                                                world.playSound(entity.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 4.0f, 0.5f);
                                                entity.remove();
                                            }
                                        }
                                        // Apparition du Maire Démoniaque
                                        spawnDemonicMayor(player, center);

                                        // Terminer la tâche
                                        this.cancel();
                                    }
                                }
                            }.runTaskTimer(plugin, 0L, 3L);
                        }
                    }.runTaskLater(plugin, 60L);
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void spawnDemonicMayor(Player player, Location center) {
        World world = center.getWorld();
        if (world == null) return;

        Location bossLocation = center.clone().add(0, 10, 0);
        boss = (Zombie) world.spawnEntity(bossLocation, EntityType.ZOMBIE);
        boss.setBaby(true);

        // Ajouter une boss bar
        BossBar bossBar = Bukkit.createBossBar(ChatColor.DARK_RED + "Le Maire Démoniaque", BarColor.RED, BarStyle.SEGMENTED_10);
        bossBar.setProgress(1.0);
        bossBar.addPlayer(player);

        // Ajouter l'équipement en netherite enchanté
        ItemStack helmet = new ItemStack(Material.NETHERITE_HELMET);
        ItemStack chestplate = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.NETHERITE_LEGGINGS);
        ItemStack boots = new ItemStack(Material.NETHERITE_BOOTS);

        // Utiliser les enchantements corrects
        helmet.addEnchantment(Enchantment.PROTECTION, 4);
        chestplate.addEnchantment(Enchantment.PROTECTION, 4);
        leggings.addEnchantment(Enchantment.PROTECTION, 4);
        boots.addEnchantment(Enchantment.PROTECTION, 4);
        helmet.addEnchantment(Enchantment.THORNS, 3);
        chestplate.addEnchantment(Enchantment.THORNS, 3);
        leggings.addEnchantment(Enchantment.THORNS, 3);
        boots.addEnchantment(Enchantment.THORNS, 3);
        helmet.addEnchantment(Enchantment.UNBREAKING, 3);
        chestplate.addEnchantment(Enchantment.UNBREAKING, 3);
        leggings.addEnchantment(Enchantment.UNBREAKING, 3);
        boots.addEnchantment(Enchantment.UNBREAKING, 3);

        boss.getEquipment().setHelmet(helmet);
        boss.getEquipment().setChestplate(chestplate);
        boss.getEquipment().setLeggings(leggings);
        boss.getEquipment().setBoots(boots);

        // Configurer le nom et la visibilité du boss
        boss.setCustomName(ChatColor.DARK_RED + "Le Maire Démoniaque");
        boss.setCustomNameVisible(true);

        // Définir les attributs du boss
        boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(500.0);
        boss.setHealth(500.0);
        boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(15.0);

        // Ajouter des effets de potion au boss
        boss.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 4));
        boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));

        // Effet de particules rouges autour du boss
        new BukkitRunnable() {
            @Override
            public void run() {
                if (boss.isDead() || !boss.isValid()) {
                    this.cancel();
                    return;
                }
                World world = boss.getWorld();
                Location bossLocation = boss.getLocation();
                // Particules de poussière rouge
                Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 1.5f);
                world.spawnParticle(Particle.DUST, bossLocation.add(0, 1, 0), 50, 1.0, 2.0, 1.0, 0, dustOptions);
            }
        }.runTaskTimer(plugin, 0L, 10L); // Exécuter toutes les 10 ticks (0,5 seconde)

        // Mise à jour de la boss bar
        new BukkitRunnable() {
            @Override
            public void run() {
                if (boss.isDead()) {
                    bossBar.removeAll();
                    this.cancel();
                } else {
                    double progress = boss.getHealth() / boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
                    bossBar.setProgress(progress);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);

        // Apparition des sbires toutes les 20 secondes
        new BukkitRunnable() {
            @Override
            public void run() {
                if (boss.isDead()) {
                    this.cancel();
                    return;
                }
                spawnMinions(boss);
            }
        }.runTaskTimer(plugin, 0L, 400L); // 400 ticks = 20 secondes
    }

    private void spawnMinions(Zombie boss) {
        World world = boss.getWorld();
        List<Location> spawnLocations = new ArrayList<>();

        // Générer les emplacements d'apparition et afficher les particules
        for (int i = 0; i < 20; i++) {
            Location spawnLocation = boss.getLocation().clone().add(Math.random() * 20 - 10, 0, Math.random() * 20 - 10);
            // Définir Y sur le bloc le plus haut à ces coordonnées
            spawnLocation.setY(world.getHighestBlockYAt(spawnLocation) + 1);
            spawnLocations.add(spawnLocation);

            // Afficher des particules pendant 2 secondes avant l'apparition des sbires
            new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (ticks < 40) {
                        world.spawnParticle(Particle.ANGRY_VILLAGER, spawnLocation, 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.LARGE_SMOKE, spawnLocation, 5, 0.5, 0.5, 0.5, 0.01);
                        ticks++;
                    } else {
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }

        // Attendre 2 secondes avant de faire apparaître les sbires
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Location spawnLocation : spawnLocations) {
                    EntityType[] minions = {EntityType.ZOMBIE, EntityType.SKELETON, EntityType.HUSK, EntityType.SPIDER};
                    EntityType minionType = minions[new Random().nextInt(minions.length)];
                    LivingEntity minion = (LivingEntity) world.spawnEntity(spawnLocation, minionType);
                    minion.setCustomName(ChatColor.GRAY + "Sbire");
                    minion.setCustomNameVisible(true);
                }
            }
        }.runTaskLater(plugin, 40L); // 40 ticks = 2 secondes
    }

    // Événement déclenché à la mort d'une entité
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().equals(boss)) {
            Player player = boss.getKiller();
            if (player == null) return;

            // Ajouter un feu d'artifice spécial au moment de la mort du boss
            spawnBossDeathFirework(boss.getLocation());

            triggerFinalAnimation(player);
        }
    }

    private void spawnBossDeathFirework(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        Firework firework = (Firework) world.spawnEntity(location, EntityType.FIREWORK_ROCKET);
        FireworkMeta meta = firework.getFireworkMeta();

        meta.setPower(3);
        meta.addEffect(FireworkEffect.builder()
                .withColor(Color.BLACK, Color.ORANGE)
                .withFade(Color.ORANGE)
                .with(FireworkEffect.Type.BALL_LARGE)
                .trail(true)
                .flicker(true)
                .build());
        firework.setFireworkMeta(meta);
    }

    private void triggerFinalAnimation(Player player) {
        Location center = player.getLocation();
        World world = center.getWorld();
        if (world == null) return;

        // Éclairs autour du joueur
        for (int i = 0; i < 8; i++) {
            double angle = i * (2 * Math.PI / 8);
            Location lightningLocation = center.clone().add(Math.cos(angle) * 5, 0, Math.sin(angle) * 5);
            world.strikeLightningEffect(lightningLocation);
        }

        // Sons dans tous les sens
        world.playSound(center, Sound.MUSIC_DISC_11, SoundCategory.MUSIC, 10f, 1f);
        world.playSound(center, Sound.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.AMBIENT, 10f, 1f);

        // Feux d'artifice aléatoires dans un rayon de 50 blocs pendant 10 minutes
        new BukkitRunnable() {
            int runCount = 0;
            Random random = new Random();

            @Override
            public void run() {
                if (runCount < 6000) { // 6000 exécutions * 2 ticks = 12 000 ticks = 10 minutes
                    for (int i = 0; i < 30; i++) { // Lancer 30 feux d'artifice à la fois
                        double offsetX = random.nextDouble() * 100 - 50; // Entre -50 et +50
                        double offsetZ = random.nextDouble() * 100 - 50;
                        Location fireworkLocation = center.clone().add(offsetX, 0, offsetZ);

                        // Définir Y sur le bloc le plus haut à ces coordonnées
                        fireworkLocation.setY(world.getHighestBlockYAt(fireworkLocation) + 1);

                        Firework firework = (Firework) world.spawnEntity(fireworkLocation, EntityType.FIREWORK_ROCKET);
                        FireworkMeta meta = firework.getFireworkMeta();

                        // Puissance aléatoire entre 1 et 3
                        int power = random.nextInt(3) + 1;
                        meta.setPower(power);

                        // Type aléatoire
                        FireworkEffect.Type type = FireworkEffect.Type.values()[random.nextInt(FireworkEffect.Type.values().length)];

                        // Couleurs aléatoires
                        Color color1 = Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                        Color color2 = Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256));

                        meta.addEffect(FireworkEffect.builder()
                                .withColor(color1, color2)
                                .withFade(color2)
                                .with(type)
                                .trail(random.nextBoolean())
                                .flicker(random.nextBoolean())
                                .build());
                        firework.setFireworkMeta(meta);
                    }
                    runCount++;
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 2L); // Exécuter toutes les 2 ticks (0,1 seconde)

        // Particules de pétales de cerisier pendant 10 minutes
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks < 12000) { // 12000 ticks = 10 minutes
                    world.spawnParticle(Particle.CHERRY_LEAVES,
                            center.clone().add(0, 50, 0), // Position au-dessus du joueur
                            5000, // Nombre de particules par tick
                            50, 50, 50, // Delta en X, Y, Z pour couvrir une large zone
                            1); // Vitesse
                    ticks++;
                } else {
                    // Remettre le beau temps et le jour
                    world.setStorm(false);
                    world.setThundering(false);
                    world.setTime(1000); // Heure du matin
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); // Exécuter chaque tick
    }

    private double randomDouble(double min, double max) {
        return min + (max - min) * new Random().nextDouble();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof EnderCrystal) {
            EnderCrystal crystal = (EnderCrystal) event.getEntity();
            if (spawnedCrystals.contains(crystal)) {
                event.setCancelled(true); // Annule les dégâts
            }
        }
    }
}
