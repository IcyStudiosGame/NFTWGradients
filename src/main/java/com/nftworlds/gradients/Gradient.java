package com.nftworlds.gradients;

import java.util.List;

public class Gradient {

    private final String name;
    private final List<String> lore;

    private final String permission;
    private final List<Integer> colors;

    public Gradient(String name, List<String> lore, String permission, List<Integer> colors) {
        this.name = name;
        this.lore = lore;
        this.permission = permission;
        this.colors = colors;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<Integer> getColors() {
        return colors;
    }

    public String getPermission() {
        return permission;
    }

    public int getTest(GradientPlayer player) {
        if (equals(player.getGradient())) {
            return -1;
        }

        if (player.getHandle().hasPermission(permission)) {
            return 0;
        }

        return 1;
    }

}
