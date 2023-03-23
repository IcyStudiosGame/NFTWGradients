package com.nftworlds.gradients.command;

import com.nftworlds.gradients.Gradient;
import com.nftworlds.gradients.GradientPlayer;
import com.nftworlds.gradients.NFTWGradientsPlugin;
import com.nftworlds.gradients.menu.GradientPageMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GradientCommand implements TabExecutor {

    private final NFTWGradientsPlugin plugin;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public GradientCommand(NFTWGradientsPlugin plugin) {
        this.plugin = plugin;

        addSubCommand(new SetSubCommand());
        addSubCommand(new RemoveSubCommand());
        addSubCommand(new ReloadSubCommand());
        addSubCommand(new HelpSubCommand());
    }

    private void addSubCommand(SubCommand command) {
        subCommands.put(command.name.toLowerCase(), command);
    }

    private SubCommand getSubCommand(String name) {
        return subCommands.get(name.toLowerCase());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player handle) {
            GradientPlayer player = plugin.getPlayer(handle);
            if (player == null) {
                sender.sendMessage("");
                return true;
            }

            if (args.length == 0) {
                GradientPageMenu menu = new GradientPageMenu(plugin, handle);

                List<Gradient> gradients = new ArrayList<>(plugin.getGradients());
                gradients.sort(Comparator.comparingInt(value -> value.getTest(player)));
                for (Gradient gradient : gradients) {
                    menu.addGradient(gradient);
                }

                handle.openInventory(menu.getInventory());
            } else {
                SubCommand subCommand = getSubCommand(args[0]);
                if (subCommand != null) {
                    String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                    subCommand.onCommand(player, subArgs);
                } else {
                    // TODO
                }
            }
        }
        return false;
    }

    private class SetSubCommand extends SubCommand {

        public SetSubCommand() {
            super("set", "nftgradients.admin.set");
        }

        @Override
        public List<String> onTabComplete(GradientPlayer player, String[] args) {
            if (args.length == 0) {
                List<String> complete = new ArrayList<>();

                for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                    complete.add(onlinePlayer.getName());
                }

                return complete;
            } else if (args.length == 1) {
                List<String> complete = new ArrayList<>();

                String playerPrefix = args[0];
                playerPrefix = playerPrefix.toLowerCase();

                for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                    String playerName = onlinePlayer.getName();
                    if (playerName.toLowerCase().startsWith(playerPrefix)) {
                        complete.add(playerName);
                    }
                }

                return complete;
            } else if (args.length == 2) {
                List<String> complete = new ArrayList<>();

                String gradientPrefix = args[1];
                gradientPrefix = gradientPrefix.toLowerCase();

                for (Gradient gradient : plugin.getGradients()) {
                    String gradientKey = gradient.getKey();
                    if (gradientKey.toLowerCase().startsWith(gradientPrefix)) {
                        complete.add(gradientKey);
                    }
                }

                return complete;
            }

            return null;
        }

    }

    private class RemoveSubCommand extends SubCommand {

        public RemoveSubCommand() {
            super("remove", "nftgradients.admin.remove");
        }

        @Override
        public List<String> onTabComplete(GradientPlayer player, String[] args) {
            if (args.length == 0) {
                List<String> complete = new ArrayList<>();

                for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                    complete.add(onlinePlayer.getName());
                }

                return complete;
            } else if (args.length == 1) {
                List<String> complete = new ArrayList<>();

                String playerPrefix = args[0];
                playerPrefix = playerPrefix.toLowerCase();

                for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                    String playerName = onlinePlayer.getName();
                    if (playerName.toLowerCase().startsWith(playerPrefix)) {
                        complete.add(playerName);
                    }
                }

                return complete;
            }

            return null;
        }

    }

    private class ReloadSubCommand extends SubCommand {

        public ReloadSubCommand() {
            super("reload", "nftgradients.admin.reload");
        }

    }

    private class HelpSubCommand extends SubCommand {

        public HelpSubCommand() {
            super("help", null);
        }

    }

    private class SubCommand {

        private final String name;
        private final String permission;

        public SubCommand(String name, String permission) {
            this.name = name;
            this.permission = permission;
        }

        public void onCommand(GradientPlayer player, String[] args) {
        }

        public List<String> onTabComplete(GradientPlayer player, String[] args) {
            return null;
        }

    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player handle) {
            GradientPlayer player = plugin.getPlayer(handle);
            if (player == null) {
                return null;
            }

            if (args.length == 0) {
                List<String> complete = new ArrayList<>();

                for (SubCommand subCommand : subCommands.values()) {
                    String permission = subCommand.permission;
                    if (permission == null || permission.isEmpty() || sender.hasPermission(permission)) {
                        complete.add(subCommand.name);
                    }
                }

                return complete;
            } else if (args.length == 1) {
                List<String> complete = new ArrayList<>();

                String commandPrefix = args[0];
                commandPrefix = commandPrefix.toLowerCase();

                for (SubCommand subCommand : subCommands.values()) {
                    if (subCommand.name.toLowerCase().startsWith(commandPrefix)) {
                        complete.add(subCommand.name);
                    }
                }

                return complete;
            } else {
                SubCommand subCommand = getSubCommand(args[0]);
                if (subCommand != null) {
                    String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                    return subCommand.onTabComplete(player, subArgs);
                }
            }
        }

        return null;
    }

}
