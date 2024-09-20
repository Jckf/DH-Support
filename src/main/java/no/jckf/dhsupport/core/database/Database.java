package no.jckf.dhsupport.core.database;

import no.jckf.dhsupport.core.DhSupport;
import no.jckf.dhsupport.core.database.migrations.Migration;
import no.jckf.dhsupport.core.exceptions.DatabaseException;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Database
{
    protected DhSupport dhSupport;

    protected Connection connection;

    protected Map<String, Class<? extends Migration>> migrations = new HashMap<>();

    public Database(DhSupport dhSupport)
    {
        this.dhSupport = dhSupport;
    }

    public Connection getConnection() throws DatabaseException
    {
        try {
            if (this.connection == null || this.connection.isClosed()) {
                this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.dhSupport.getDataDirectory() + "/data.sqlite");
            }
        } catch (SQLException exception) {
            throw new DatabaseException(exception.getMessage(), exception);
        }

        return this.connection;
    }

    public void open() throws DatabaseException
    {
        try {
            this.getConnection();
        } catch (DatabaseException exception) {
            throw new DatabaseException(exception.getMessage(), exception);
        }
    }

    public void close() throws DatabaseException
    {
        try {
            if (this.connection == null || this.connection.isClosed()) {
                return;
            }

            this.getConnection().close();
        } catch (SQLException exception) {
            throw new DatabaseException(exception.getMessage(), exception);
        }
    }

    protected void createMigrationsTable() throws DatabaseException
    {
        String sql = """
            CREATE TABLE IF NOT EXISTS migrations (
                name STRING PRIMARY KEY,
                timestamp INTEGER
            );
        """;

        try (Statement statement = this.getConnection().createStatement()) {
            statement.execute(sql);
        } catch (SQLException exception) {
            throw new DatabaseException(exception.getMessage(), exception);
        }
    }

    public void addMigration(String name, Class<? extends Migration> migration)
    {
        this.migrations.put(name, migration);
    }

    public boolean hasMigrationRan(String name) throws DatabaseException
    {
        String sql = "SELECT 1 FROM migrations WHERE name = ?;";

        try (PreparedStatement statement = this.getConnection().prepareStatement(sql)) {
            statement.setString(1, name);

            ResultSet result = statement.executeQuery();

            return result.next();
        } catch (SQLException exception) {
            throw new DatabaseException(exception.getMessage(), exception);
        }
    }

    public void markMigrationAsRan(String name) throws DatabaseException
    {
        String sql = "INSERT INTO migrations (name, timestamp) VALUES (?, ?);";

        try (PreparedStatement statement = this.getConnection().prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setInt(2, (int) (System.currentTimeMillis() / 1000));

            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DatabaseException(exception.getMessage(), exception);
        }
    }

    public void migrate() throws DatabaseException
    {
        this.createMigrationsTable();

        for (String name : this.migrations.keySet()) {
            if (this.hasMigrationRan(name)) {
                continue;
            }

            this.dhSupport.info("Running database migration: " + name);

            try {
                Migration migration = this.migrations.get(name).getConstructor(this.getClass()).newInstance(this);

                migration.up();
            } catch (Exception exception) {
                throw new DatabaseException(exception.getMessage(), exception);
            }

            this.markMigrationAsRan(name);
        }
    }
}
