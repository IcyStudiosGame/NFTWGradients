package com.nftworlds.gradients.hook;

import com.nftworlds.gradients.Gradient;
import com.nftworlds.gradients.GradientPlayer;
import com.nftworlds.gradients.NFTWGradientsPlugin;
import com.nftworlds.gradients.util.Cmpt;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GradientNameExpansion extends PlaceholderExpansion {

    private static final String IDENTIFIER = "nftw";
    private static final String PLACEHOLDER = "display_name";

    private final NFTWGradientsPlugin plugin;

    public GradientNameExpansion(NFTWGradientsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getName();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player handle, @NotNull String params) {
        if (!PLACEHOLDER.equalsIgnoreCase(params)) {
            return null;
        }

        GradientPlayer player = plugin.getPlayer(handle);
        if (player == null) {
            return null;
        }

        Gradient gradient = player.getGradient();
        if (gradient == null) {
            return null;
        }

        return Cmpt.gradientTest(handle.getName(), gradient.getColors());
    }

}
