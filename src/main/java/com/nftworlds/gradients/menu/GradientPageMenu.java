package com.nftworlds.gradients.menu;

import com.nftworlds.gradients.Gradient;
import com.nftworlds.gradients.GradientPlayer;
import com.nftworlds.gradients.NFTWGradientsPlugin;
import com.nftworlds.gradients.util.Cmpt;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GradientPageMenu implements GradientMenu {

    private static final int SIZE = 6 * 9;

    private static final int START_ITEM_SLOT = 10;
    private static final int END_ITEM_SLOT = 34;
    private static final int PREVIOUS_PAGE_SLOT = 48;
    private static final int NEXT_PAGE_SLOT = 50;

    private final NFTWGradientsPlugin plugin;
    private final Player handle;
    private final Inventory inventory;

    private final Int2ObjectMap<Action> actions = new Int2ObjectOpenHashMap<>();

    private int lastItemSlot = START_ITEM_SLOT - 1;

    public GradientPageMenu(NFTWGradientsPlugin plugin, Player handle) {
        this.plugin = plugin;
        this.handle = handle;
        this.inventory = Bukkit.createInventory(this, SIZE, Component.text("Test"));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public boolean addGradient(Gradient gradient) {
        if (nextSlot()) {
            actions.put(lastItemSlot, new GradientAction(gradient));
            return true;
        }
        return false;
    }

    public void setPreviousPage(GradientPageMenu menu) {
        actions.put(PREVIOUS_PAGE_SLOT, new PageAction(menu));
    }

    public void setNextPage(GradientPageMenu menu) {
        actions.put(NEXT_PAGE_SLOT, new PageAction(menu));
    }

    @Override
    public void onOpen(Player handle) {
        for (Int2ObjectMap.Entry<Action> entry : actions.int2ObjectEntrySet()) {
            int slot = entry.getIntKey();
            Action action = entry.getValue();

            inventory.setItem(slot, action.getItemStack());

        }
    }

    @Override
    public void onClick(Player handle, int slot) {
        Action action = actions.get(slot);
        if (action != null) {
            action.apply(handle);
        }
    }

    @Override
    public void onClose(Player handle) {
    }

    private boolean nextSlot() {
        if (!nextSlot(1)) {
            return false;
        }
        return (lastItemSlot + 1) % 9 != 0 || nextSlot(3);
    }

    private boolean nextSlot(int step) {
        int nextItemSlot = lastItemSlot + step;
        if (nextItemSlot > END_ITEM_SLOT) {
            return false;
        }

        lastItemSlot = nextItemSlot;
        return true;
    }

    private class GradientAction implements Action {

        private final Gradient gradient;

        public GradientAction(Gradient gradient) {
            this.gradient = gradient;
        }

        @Override
        public ItemStack getItemStack() {
            ItemStack test = new ItemStack(Material.PAPER);

            ItemMeta meta = test.getItemMeta();

            Component displayName = Cmpt.gradient(gradient.getName(), gradient.getColors());
            displayName = displayName.decoration(TextDecoration.BOLD, true);
            displayName = displayName.decoration(TextDecoration.ITALIC, false);
            meta.displayName(displayName);

            List<Component> loreTest = new ArrayList<>();
            loreTest.add(Component.empty());

            Component nameP = Component.empty();
            nameP = nameP.decoration(TextDecoration.ITALIC, false);
            nameP = nameP.append(Component.text("  §8▪§f Display Name: "));
            nameP = nameP.append(Cmpt.gradient(handle.getName(), gradient.getColors()));
            loreTest.add(nameP);

            loreTest.add(Component.empty());

            for (String s : gradient.getLore()) {
                Component wtf = Cmpt.parse(s);
                wtf = wtf.decoration(TextDecoration.ITALIC, false);
                loreTest.add(wtf);
            }
            loreTest.add(Component.empty());

            Component status = Component.empty();
            status = status.decoration(TextDecoration.ITALIC, false);
            status = status.append(Component.text("Status: ", TextColor.color(0xA5ACB8)));
            status = status.append(Component.text("Available", TextColor.color(0x00c300)));
            loreTest.add(status);

            meta.lore(loreTest);

            test.setItemMeta(meta);

            return test;
        }

        @Override
        public void apply(Player handle) {
            GradientPlayer player = plugin.getPlayer(handle);
            if (player != null) {
                player.setGradient(gradient);
            }

            Component displayName = Cmpt.gradient(handle.getName(), gradient.getColors());
            handle.displayName(displayName);
            handle.playerListName(displayName);
        }

    }

    private class PageAction implements Action {

        private final GradientMenu menu;

        public PageAction(GradientMenu menu) {
            this.menu = menu;
        }

        @Override
        public ItemStack getItemStack() {
            return null;
        }

        @Override
        public void apply(Player handle) {
            handle.openInventory(menu.getInventory());
        }

    }

    private interface Action {

        ItemStack getItemStack();

        void apply(Player handle);

    }

}
