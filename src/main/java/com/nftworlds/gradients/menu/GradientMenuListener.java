package com.nftworlds.gradients.menu;

import com.nftworlds.gradients.Gradient;
import com.nftworlds.gradients.NFTWGradientsPlugin;
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

import java.util.logging.Level;

public class GradientMenuListener implements Listener {

    private final NFTWGradientsPlugin plugin;

    public GradientMenuListener(NFTWGradientsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().equalsIgnoreCase("/menu")) {
            GradientPageMenu menu = new GradientPageMenu(plugin, event.getPlayer());

            for (Gradient gradient : plugin.getGradients()) {
                menu.addGradient(gradient);
            }

            event.getPlayer().openInventory(menu.getInventory());

            event.setCancelled(true);
        }
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
