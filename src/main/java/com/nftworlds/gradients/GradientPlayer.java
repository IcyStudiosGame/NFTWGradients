package com.nftworlds.gradients;

import org.bukkit.entity.Player;

public class  GradientPlayer {

    private final Player handle;

    private Gradient gradient;

    public GradientPlayer(Player handle) {
        this.handle = handle;
    }

    public Player getHandle() {
        return handle;
    }

    public Gradient getGradient() {
        return gradient;
    }

    public void setGradient(Gradient gradient) {
        this.gradient = gradient;
    }

}
