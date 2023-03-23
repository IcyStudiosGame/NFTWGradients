package com.nftworlds.gradients.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

public interface GradientMenu extends InventoryHolder {

    void onOpen(Player handle);

    void onClick(Player handle, int slot);

    void onClose(Player handle);

}
