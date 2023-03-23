package com.nftworlds.gradients;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nftworlds.gradients.command.GradientCommand;
import com.nftworlds.gradients.hook.GradientNameExpansion;
import com.nftworlds.gradients.listener.PlayerListener;
import com.nftworlds.gradients.menu.GradientMenuListener;
import com.nftworlds.gradients.sql.GradientMySQL;
import com.nftworlds.gradients.util.IntColor;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class NFTWGradientsPlugin extends JavaPlugin {

    private GradientMySQL mySQL;

    // Here ConcurrentHashMap, because the information
    // from this collection is used by the PlaceholderAPI
    // or can be useful in asynchronous chat events.
    private final Map<Player, GradientPlayer> players = new ConcurrentHashMap<>();
    private final Cache<String, GradientPlayer> cachedPlayers = CacheBuilder.newBuilder()
            .expireAfterWrite(10L, TimeUnit.MINUTES)
            .build();

    // The use of this collection occurs when the player's data is loaded in another thread,
    // so we make this collection ConcurrentHashMap
    private final Map<String, Gradient> gradients = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        try {
            loadConfig("config.yml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Server server = getServer();

        PluginManager pluginManager = server.getPluginManager();
        pluginManager.registerEvents(new GradientMenuListener(this), this);
        pluginManager.registerEvents(new PlayerListener(this), this);

        Plugin plugin = pluginManager.getPlugin("PlaceholderAPI");
        if (plugin != null && plugin.isEnabled()) {
            GradientNameExpansion expansion = new GradientNameExpansion(this);
            expansion.register();
        }

        PluginCommand pluginCommand = server.getPluginCommand("gradient");
        if (pluginCommand != null) {
            GradientCommand command = new GradientCommand(this);
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }
    }

    public GradientMySQL getMySQL() {
        return mySQL;
    }

    public void addCachedPlayer(GradientPlayer player) {
        cachedPlayers.put(player.getName().toLowerCase(), player);
    }

    public GradientPlayer removeCachedPlayer(String playerName) {
        playerName = playerName.toLowerCase();

        GradientPlayer player = cachedPlayers.getIfPresent(playerName);
        if (player != null) {
            cachedPlayers.invalidate(playerName);
        }

        return player;
    }

    public void addPlayer(GradientPlayer player) {
        players.put(player.getHandle(), player);
    }

    public GradientPlayer getPlayer(Player handle) {
        return players.get(handle);
    }

    public GradientPlayer removePlayer(Player handle) {
        return players.remove(handle);
    }

    public Gradient getGradient(String name) {
        return gradients.get(name.toLowerCase());
    }

    public Collection<Gradient> getGradients() {
        return gradients.values();
    }

    private void loadConfig(String fileName) throws IOException {
        File dataFolder = getDataFolder();
        dataFolder.mkdirs();

        File file = new File(dataFolder, fileName);
        if (!file.exists()) {
            if (file.createNewFile()) {
                InputStream inputStream = getResource(fileName);
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(file);

                    byte[] buf = new byte[1024];

                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                } finally {
                    try {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                }
            }
        }

        Configuration configuration = YamlConfiguration.loadConfiguration(file);

        // TODO: sql
        String host = configuration.getString("MySQL.Host", "localhost");
        int port = configuration.getInt("MySQL.Port", 3306);
        String database = configuration.getString("MySQL.Database", "minecraft");
        String username = configuration.getString("MySQL.Username", "root");
        String password = configuration.getString("MySQL.Password");

        File databaseFile = new File(dataFolder, database + ".db");
        if (!databaseFile.exists() && databaseFile.createNewFile()) {

        }

        // String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useUnicode=true&characterEncoding=utf-8&cachePrepStmts=true";
        String url = "jdbc:sqlite:" + databaseFile.getAbsolutePath();

        mySQL = new GradientMySQL(this, url, username, password);
        mySQL.createTableIfNotExists();

        ConfigurationSection section = configuration.getConfigurationSection("Gradients");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String name = section.getString(key + ".Name");
                List<String> lore = section.getStringList(key + ".Description");
                String permission = "nftgradients." + key;

                List<String> colors = section.getStringList(key + ".Colors");
                List<Integer> intColors = new ArrayList<>();
                for (String color : colors) {
                    intColors.add(IntColor.fromHex(color));
                }

                gradients.put(key.toLowerCase(), new Gradient(key, name, lore, permission, intColors));
            }
        }
    }

}
