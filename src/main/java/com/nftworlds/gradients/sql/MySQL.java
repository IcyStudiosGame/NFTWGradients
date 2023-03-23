package com.nftworlds.gradients.sql;

import com.nftworlds.gradients.NFTWGradientsPlugin;
import com.nftworlds.gradients.sql.func.SafeRunnable;
import com.nftworlds.gradients.sql.func.SelectCallback;
import com.nftworlds.gradients.sql.func.StatementPreparer;
import com.nftworlds.gradients.sql.func.UpdateCallback;

import java.sql.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.logging.Level;

// The terms of reference did not contain restrictions
// on the use of other libraries, but in order to assess the quality of the code,
// work with the database was implemented from scratch,
// without using ready-made solutions in frameworks.
public class MySQL extends Thread {

    protected final NFTWGradientsPlugin plugin;

    private final String url;
    private final String username;
    private final String password;

    private final Queue<Consumer<Connection>> queries = new ConcurrentLinkedQueue<>();

    private volatile boolean running;
    private volatile boolean connected;

    protected Connection connection;

    public MySQL(NFTWGradientsPlugin plugin, String url, String username, String password) {
        this.plugin = plugin;

        this.url = url;
        this.username = username;
        this.password = password;

        setName(plugin.getDescription().getName() + " - MySQL");
        setDaemon(true);
    }

    public void select(String query, SelectCallback callback) {
        select(query, null, callback);
    }

    public void select(String query, StatementPreparer preparer, SelectCallback callback) {
        submitQuery((connection) -> {
            String testQuery = onPreQuery(query);
            if (testQuery != null) {
                try (Statement statement = preparer != null ? connection.prepareStatement(testQuery) : connection.createStatement()) {
                    if (preparer != null) {
                        PreparedStatement preparedStatement = (PreparedStatement) statement;
                        try {
                            preparer.prepare(preparedStatement);
                        } catch (Exception ex) {
                            plugin.getLogger().log(Level.SEVERE, "Could not fill prepared statement " + testQuery, ex);
                            return;
                        }
                        preparedStatement.execute();
                    } else {
                        statement.execute(testQuery);
                    }

                    if (callback != null) {
                        try (ResultSet resultSet = statement.getResultSet()) {
                            callback.done(resultSet);
                        } catch (Exception ex) {
                            plugin.getLogger().log(Level.SEVERE, "Query " + testQuery + " is failed!", ex);
                        }
                    }

                    onPostQuery(testQuery, true);
                } catch (Exception exception) {
                    onPostQuery(testQuery, false);

                    if (exception.getMessage() != null && exception.getMessage().contains("try restarting transaction")) {
                        select(query, preparer, callback);
                        plugin.getLogger().warning("Query " + testQuery + " is failed! Restarting: " + exception.getMessage());
                    } else {
                        plugin.getLogger().severe("Query " + testQuery + " is failed! Message: " + exception.getMessage());
                    }
                }
            }
        });
    }

    public void query(String query) {
        update(query, null);
    }

    public void query(String query, StatementPreparer preparer) {
        update(query, preparer, null);
    }

    public void update(String query, UpdateCallback callback) {
        update(query, null, callback);
    }

    public void update(String query, StatementPreparer preparer, UpdateCallback callback) {
        submitQuery((connection) -> {
            String testQuery = onPreQuery(query);
            if (testQuery != null) {
                try (Statement statement = preparer != null ? connection.prepareStatement(testQuery) : connection.createStatement()) {
                    if (preparer != null) {
                        PreparedStatement preparedStatement = (PreparedStatement) statement;
                        try {
                            preparer.prepare(preparedStatement);
                        } catch (Exception exception) {
                            plugin.getLogger().log(Level.SEVERE, "Could not fill prepared statement " + testQuery, exception);
                            return;
                        }
                        preparedStatement.execute();
                    } else {
                        statement.execute(testQuery);
                        System.out.println("TEST QUERY - " + testQuery);
                    }

                    if (callback != null) {
                        try {
                            callback.done(statement.getUpdateCount());
                        } catch (Exception ex) {
                            plugin.getLogger().log(Level.SEVERE, "Query " + testQuery + " is failed!", ex);
                        }
                    }

                    onPostQuery(testQuery, true);
                } catch (Exception exception) {
                    onPostQuery(testQuery, false);

                    if (exception.getMessage() != null && exception.getMessage().contains("try restarting transaction")) {
                        update(query, preparer, callback);
                        plugin.getLogger().warning("Query " + testQuery + " is failed! Restarting: " + exception.getMessage());
                    } else {
                        plugin.getLogger().severe("Query " + testQuery + " is failed! Message: " + exception.getMessage());
                    }
                }
            }
        });
    }

    private void submitQuery(Consumer<Connection> query) {
        queries.add(query);
    }

    @Override
    public void start() {
        if (!running) {
            running = true;
            super.start();
        }
    }

    public void finish() {
        if (running) {
            running = false;

            safe(this::join);
            if (connection != null) {
                safe(this::checkConnection);
                safe(this::executeQueries);
                safe(connection::close);
            }
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isRunning() {
        return running;
    }

    protected void onConnect() {
    }

    protected void onDisconnect() {
    }

    protected String onPreQuery(String query) {
        return query;
    }

    protected void onPostQuery(String query, boolean success) {
    }

    protected void safe(SafeRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        checkConnection();

        while (running) {
            if (queries.size() > 0) {
                if (checkConnection()) {
                    executeQueries();
                } else {
                    queries.clear();
                }
            }

            try {
                sleep(1000);
            } catch (InterruptedException exception) {
                running = false;
            }
        }
    }

    private void executeQueries() {
        for (Consumer<Connection> query = queries.poll(); query != null; query = queries.poll()) {
            try {
                query.accept(connection);
            } catch (Exception exception) {
                plugin.getLogger().log(Level.WARNING, "Task failed", exception);
            }
        }
    }

    public boolean checkConnection() {
        boolean state = false;

        try {
            if (connection != null && !isValid()) {
                safe(connection::close);
                connection = null;
            }

            if (connection == null) {
                connect();
            }

            state = connection != null && isValid();
        } catch (Exception exception) {
            plugin.getLogger().log(Level.WARNING, "Error while connecting to database: {0}", exception.getMessage());
        }

        if (connected != state) {
            connected = state;
            if (!connected) {
                onDisconnect();
            }
        }

        return state;
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(url, username, password);
            if (isValid()) {
                plugin.getLogger().log(Level.INFO, "MySQL connected");
                onConnect();
            }
        } catch (SQLException exception) {
            plugin.getLogger().log(Level.WARNING, "", exception);
        }
    }

    private boolean isValid() throws SQLException {
        return connection.isValid(30);
    }

}
