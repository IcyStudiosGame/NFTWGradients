package com.nftworlds.gradients;

import com.nftworlds.gradients.util.Cmpt;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class GradientPlayer {

    private final String name;

    private Player handle;
    private Gradient gradient;

    public GradientPlayer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Player getHandle() {
        return handle;
    }

    public void setHandle(Player handle) {
        this.handle = handle;

        if (this.gradient != null) {
            applyGradient(this.handle, this.gradient);
        }
    }

    public Gradient getGradient() {
        return gradient;
    }

    public void setGradient(Gradient gradient) {
        this.gradient = gradient;

        if (this.handle != null) {
            applyGradient(this.handle, this.gradient);
        }
    }

    private static void applyGradient(Player handle, Gradient gradient) {
        Component displayName = null;
        if (gradient != null) {
            displayName = Cmpt.gradient(handle.getName(), gradient.getColors());
        }

        handle.displayName(displayName);
        handle.playerListName(displayName);
    }

}
