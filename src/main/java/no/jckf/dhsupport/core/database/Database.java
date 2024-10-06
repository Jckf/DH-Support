/*
 * DH Support, server-side support for Distant Horizons.
 * Copyright (C) 2024 Jim C K Flaten
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package no.jckf.dhsupport.core.database;

import no.jckf.dhsupport.core.database.migrations.Migration;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Database
{
    protected String path;

    protected Connection connection;

    protected Map<String, Class<? extends Migration>> migrations = new HashMap<>();

    public Connection getConnection() throws SQLException
    {
        if (this.connection == null || this.connection.isClosed()) {
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.path);
        }

        return this.connection;
    }

    public void open(String path) throws SQLException
    {
        this.path = path;

        this.getConnection();
    }

    public void close() throws SQLException
    {
        if (this.connection == null || this.connection.isClosed()) {
            return;
        }

        this.getConnection().close();
    }

    protected void createMigrationsTable() throws SQLException
    {
        String sql = """
            CREATE TABLE IF NOT EXISTS migrations (
                name STRING PRIMARY KEY,
                timestamp INTEGER
            );
        """;

        try (Statement statement = this.getConnection().createStatement()) {
            statement.execute(sql);
        }
    }

    public void addMigration(Class<? extends Migration> migration)
    {
        this.migrations.put(migration.getSimpleName(), migration);
    }

    public boolean hasMigrationRan(String name) throws SQLException
    {
        String sql = "SELECT 1 FROM migrations WHERE name = ?;";

        try (PreparedStatement statement = this.getConnection().prepareStatement(sql)) {
            statement.setString(1, name);

            ResultSet result = statement.executeQuery();

            return result.next();
        }
    }

    public void markMigrationAsRan(String name) throws SQLException
    {
        String sql = "INSERT INTO migrations (name, timestamp) VALUES (?, ?);";

        try (PreparedStatement statement = this.getConnection().prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setInt(2, (int) (System.currentTimeMillis() / 1000));

            statement.executeUpdate();
        }
    }

    public void migrate() throws Exception
    {
        this.createMigrationsTable();

        for (String name : this.migrations.keySet()) {
            if (this.hasMigrationRan(name)) {
                continue;
            }

            Migration migration = this.migrations.get(name).getConstructor(this.getClass()).newInstance(this);

            migration.up();

            this.markMigrationAsRan(name);
        }
    }
}
