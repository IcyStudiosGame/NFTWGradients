package com.nftworlds.gradients.listener;

import com.nftworlds.gradients.Gradient;
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

            // We do the data loading right in this event,
            // because this event is called in a new thread for each player (new Thread),
            // this is the best place to load data from the database.
            if (!plugin.getMySQL().loadPlayer(player)) {
                event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                event.kickMessage(Component.text("Failed to load data, please try again..."));
                return;
            }
        }

        // We add the player object to the cache,
        // because it is impossible to track
        // the cancellation of this event in 100% of cases,
        // the same situation will be with the PlayerLoginEvent event.
        // The PlayerQuitEvent event can only be raised if the PlayerJoinEvent fired.
        //
        // Order of calling player login events:
        //  1. AsyncPlayerPreLoginEvent
        //  2. PlayerLoginEvent
        //  3. PlayerJoinEvent
        plugin.addCachedPlayer(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        GradientPlayer player = plugin.removeCachedPlayer(event.getPlayer().getName());
        if (player == null) {
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            event.kickMessage(Component.text("Failed to load data, please try again..."));
            return;
        }

        plugin.addCachedPlayer(player);
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

        Gradient gradient = player.getGradient();
        if (gradient != null) {
            // In the terms of reference,
            // a mandatory condition is specified for checking
            // the permission when the player enters,
            // so this code looks like this and is located in this place.
            //
            // This could be changed and made better, possible options are:
            //  1. Run a task and check every 20 ticks for the permissions
            //     on the gradient for each player (Although it seems
            //     that such a variant of the code will load the server
            //     and does not look good, but it is not, such a check will
            //     take very little CPU time).
            //  2. If the plugin for permissions were known,
            //     then it would be possible through its API to track
            //     the change in the permissions of the player and change
            //     the status of access to the gradient.
            if (player.hasAccess(gradient)) {
                // TODO: apply gradient to player
            } else {
                player.setGradient(null);
            }
        }

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
