package com.nftworlds.gradients.listener;

import com.nftworlds.gradients.GradientPlayer;
import com.nftworlds.gradients.NFTWGradientsPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final NFTWGradientsPlugin plugin;

    public PlayerListener(NFTWGradientsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        String name = event.getName();

        GradientPlayer player = plugin.removeCachedPlayer(name);
        if (player == null) {
            player = new GradientPlayer(name);
            if (!plugin.getMySQL().loadPlayer(player)) {
                event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                event.kickMessage(Component.text("Failed to load data, please try again..."));
                return;
            }
        }

        plugin.addCachedPlayer(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        // TODO: check cache + update time
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player handle = event.getPlayer();

        GradientPlayer player = plugin.removeCachedPlayer(handle.getName());
        if (player == null) {
            handle.kick(Component.text("Failed to load data, please try again..."));
            return;
        }
        player.setHandle(handle);

        plugin.addPlayer(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player handle = event.getPlayer();

        GradientPlayer player = plugin.removePlayer(handle);
        player.setHandle(null);

        plugin.addCachedPlayer(player);
        plugin.getMySQL().savePlayer(player);
    }

}
