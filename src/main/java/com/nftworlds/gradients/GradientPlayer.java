package com.nftworlds.gradients;

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
    }

    public Gradient getGradient() {
        return gradient;
    }

    public void setGradient(Gradient gradient) {
        this.gradient = gradient;
    }

    public boolean hasAccess(Gradient gradient) {
        if (handle == null) {
            return false;
        }

        if (gradient == null) {
            return true;
        }

        String permission = gradient.getPermission();
        if (permission == null || permission.isEmpty()) {
            return true;
        }

        return handle.hasPermission(permission);
    }

}
