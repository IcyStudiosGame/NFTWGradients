package com.nftworlds.gradients.menu;

import com.nftworlds.gradients.Gradient;
import com.nftworlds.gradients.GradientPlayer;
import com.nftworlds.gradients.NFTWGradientsPlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradientMenuListener implements Listener {

    private final NFTWGradientsPlugin plugin;

    public GradientMenuListener(NFTWGradientsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().equalsIgnoreCase("/menu")) {
            GradientPlayer player = plugin.getPlayer(event.getPlayer());

            GradientPageMenu menu = new GradientPageMenu(plugin, event.getPlayer());

            List<Gradient> gradients = new ArrayList<>(plugin.getGradients());
            gradients.sort(Comparator.comparingInt(value -> value.getTest(player)));

            for (Gradient gradient : gradients) {
                menu.addGradient(gradient);
            }

            event.getPlayer().openInventory(menu.getInventory());

            event.setCancelled(true);
        } else if (event.getMessage().equalsIgnoreCase("/test")) {
            Player player = event.getPlayer();

            player.sendMessage(PlaceholderAPI.setPlaceholders(player, "Test wtf kek %nftw_display_name%"));

            event.setCancelled(true);
        }
    }

    public static String hexConvert(String message) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher match = pattern.matcher(message);
        while (match.find()) {
            String color = message.substring(match.start(), match.end());
            message = message.replace(color, color(color) + "");
            match = pattern.matcher(message);
        }
        return message.replace("{", "").replace("}", "");
    }

    public static String color(String hex) {
        if (hex.startsWith("#") && hex.length() == 7) {
            StringBuilder magic = new StringBuilder("§x");

            char[] array = hex.toCharArray();
            for (int index = 1; index < array.length; index++) {
                magic.append('§').append(array[index]);
            }

            return magic.toString();
        }
        return hex;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        HumanEntity whoOpen = event.getPlayer();
        InventoryHolder holder = event.getView().getTopInventory().getHolder();

        if (whoOpen instanceof Player handle && holder instanceof GradientMenu menu) {
            try {
                menu.onOpen(handle);
            } catch (Exception exception) {
                plugin.getLogger().log(Level.WARNING, "Error when opening menu: " + menu.getClass().getSimpleName(), exception);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity whoClicked = event.getWhoClicked();
        Inventory topInventory = event.getView().getTopInventory();
        InventoryHolder holder = topInventory.getHolder();

        if (whoClicked instanceof Player handle && holder instanceof GradientMenu menu) {
            if (topInventory.equals(event.getClickedInventory())) {
                try {
                    menu.onClick(handle, event.getSlot());
                } catch (Exception exception) {
                    plugin.getLogger().log(Level.WARNING, "Error when clicking menu: " + menu.getClass().getSimpleName(), exception);
                }
            }

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        HumanEntity whoClicked = event.getWhoClicked();
        InventoryHolder holder = event.getView().getTopInventory().getHolder();

        if (whoClicked instanceof Player && holder instanceof GradientMenu) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity whoClose = event.getPlayer();
        InventoryHolder holder = event.getView().getTopInventory().getHolder();

        if (whoClose instanceof Player handle && holder instanceof GradientMenu menu) {
            try {
                menu.onClose(handle);
            } catch (Exception exception) {
                plugin.getLogger().log(Level.WARNING, "Error when closing menu: " + menu.getClass().getSimpleName(), exception);
            }
        }
    }

}
