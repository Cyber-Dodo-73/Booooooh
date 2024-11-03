package fr.cyberdodo.booooooh.commands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class PutPumpkinOnHeadCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final Random random = new Random();

    public PutPumpkinOnHeadCommand(JavaPlugin plugin) {
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

        Player targetPlayer = Bukkit.getPlayer(args[0]);

        if (targetPlayer == null) {
            sender.sendMessage("Joueur introuvable !");
            return false;
        }

        // Obtenir l'inventaire du joueur
        PlayerInventory inventory = targetPlayer.getInventory();

        // Vérifier s'il y a un objet sur la tête
        ItemStack currentHelmet = inventory.getHelmet();

        if (currentHelmet != null && currentHelmet.getType() != Material.AIR) {
            // Vérifier s'il y a de la place dans l'inventaire
            if (inventory.firstEmpty() == -1) {
                sender.sendMessage("L'inventaire du joueur est plein. Impossible de placer la citrouille.");
                return false;
            } else {
                // Déplacer l'objet de la tête vers l'inventaire
                inventory.addItem(currentHelmet);
            }
        }

        // Placer la citrouille sur la tête du joueur
        ItemStack pumpkin = new ItemStack(Material.CARVED_PUMPKIN, 1);
        inventory.setHelmet(pumpkin);

        // Jouer des rires de sorcières
        targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 1.0f, 1.0f);

        // Spawner des chauves-souris autour du joueur
        spawnBatsAroundPlayer(targetPlayer, 10); // Fait apparaître 10 chauves-souris

        // Optionnel : Jouer un son pour indiquer que la citrouille a été placée
        targetPlayer.playSound(targetPlayer.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.0f, 1.0f);

        sender.sendMessage("Une citrouille a été placée sur la tête de " + targetPlayer.getName() + ".");

        return true;
    }

    private void spawnBatsAroundPlayer(Player player, int numberOfBats) {
        Location playerLocation = player.getLocation();
        World world = player.getWorld();

        for (int i = 0; i < numberOfBats; i++) {
            double xOffset = (random.nextDouble() * 6) - 3; // Décalage entre -3 et +3
            double yOffset = random.nextDouble() * 3; // Décalage entre 0 et +3
            double zOffset = (random.nextDouble() * 6) - 3; // Décalage entre -3 et +3

            Location batLocation = playerLocation.clone().add(xOffset, yOffset, zOffset);

            // Spawner la chauve-souris
            Bat bat = world.spawn(batLocation, Bat.class);

            // Optionnel : Faire en sorte que la chauve-souris s'envole immédiatement
            bat.setAwake(true);
        }
    }
}
