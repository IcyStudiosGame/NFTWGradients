package com.nftworlds.gradients.sql;

import com.nftworlds.gradients.Gradient;
import com.nftworlds.gradients.GradientPlayer;
import com.nftworlds.gradients.NFTWGradientsPlugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class GradientMySQL extends MySQL {

    private static final String TABLE_NAME = "gradient_users";

    private static final String PLAYER_NAME = "player_name";
    private static final String GRADIENT_NAME = "gradient_name";

    private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS `" + TABLE_NAME + "` (`" + PLAYER_NAME + "` VARCHAR(16) NOT NULL, `" + GRADIENT_NAME + "` VARCHAR(100) NULL);";

    private static final String LOAD_PLAYER_QUERY = "SELECT `" + GRADIENT_NAME + "` FROM `" + TABLE_NAME + "` WHERE `" + PLAYER_NAME + "` = ? LIMIT 1;";
    private static final String CREATE_PLAYER_QUERY = "INSERT INTO `" + TABLE_NAME + "` (`" + PLAYER_NAME + "`, `" + GRADIENT_NAME + "`) VALUES (?, ?);";
    private static final String SAVE_PLAYER_QUERY = "UPDATE `" + TABLE_NAME + "` SET `" + GRADIENT_NAME + "`= ? WHERE `" + PLAYER_NAME + "` = ?;";

    public GradientMySQL(NFTWGradientsPlugin plugin, String url, String username, String password) {
        super(plugin, url, username, password);
    }

    public void createTableIfNotExists() {
        // query(CREATE_TABLE_QUERY);
    }

    public boolean loadPlayer(GradientPlayer player) {
        if (true) {
            return true;
        }

        try {
            if (checkConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(LOAD_PLAYER_QUERY)) {
                    statement.setString(1, player.getName());
                    statement.execute();

                    ResultSet resultSet = statement.getResultSet();
                    if (resultSet.next()) {
                        String gradientName = resultSet.getString(GRADIENT_NAME);
                        Gradient gradient = plugin.getGradient(gradientName);
                        if (gradient != null) {
                            player.setGradient(gradient);
                        }
                    } else {
                        return insertPlayer(player);
                    }
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to load player data from MySQL: ", exception);
            return true;
        }
    }

    private boolean insertPlayer(GradientPlayer player) throws SQLException {
        String gradientName = null;
        Gradient gradient = player.getGradient();
        if (gradient != null) {
            gradientName = gradient.getKey();
        }

        try (PreparedStatement statement = connection.prepareStatement(CREATE_PLAYER_QUERY)) {
            statement.setString(1, player.getName());
            statement.setString(2, gradientName);
            statement.executeUpdate();
            return true;
        }
    }

    public void savePlayer(GradientPlayer player) {
        String gradientName;
        Gradient gradient = player.getGradient();
        if (gradient != null) {
            gradientName = gradient.getKey();
        } else {
            gradientName = null;
        }

        query(SAVE_PLAYER_QUERY, statement -> {
            statement.setString(1, gradientName);
            statement.setString(2, player.getName());
        });
    }

}
