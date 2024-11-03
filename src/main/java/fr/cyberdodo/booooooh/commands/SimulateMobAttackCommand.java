package fr.cyberdodo.booooooh.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.EntityEffect;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SimulateMobAttackCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public SimulateMobAttackCommand(JavaPlugin plugin) {
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

        // Simuler plusieurs attaques sur le joueur
        simulateMultipleAttacks(player);

        return true;
    }

    private void simulateMultipleAttacks(Player player) {
        // Nombre total d'attaques
        final int totalAttacks = 5;

        // Intervalle entre les attaques en ticks (20 ticks = 1 seconde)
        final int attackInterval = 40; // Attaques toutes les 2 secondes

        new BukkitRunnable() {
            int attacksDone = 0;

            @Override
            public void run() {
                if (attacksDone >= totalAttacks) {
                    this.cancel(); // Arrêter la tâche après le nombre d'attaques
                    return;
                }

                // Simuler une attaque sur le joueur
                simulateMobAttack(player);

                attacksDone++;
            }
        }.runTaskTimer(plugin, 0, attackInterval); // Démarrer immédiatement, répéter toutes les 2 secondes
    }

    private void simulateMobAttack(Player player) {
        // Jouer l'effet de dommage sans infliger de dégâts
        player.playEffect(EntityEffect.HURT);

        // Jouer le son de dommage
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);

        // Optionnel: appliquer un court recul au joueur pour simuler l'impact
        player.setVelocity(player.getLocation().getDirection().multiply(0.5).setY(0.5));
    }
}
