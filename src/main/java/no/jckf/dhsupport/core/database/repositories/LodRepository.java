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

package no.jckf.dhsupport.core.database.repositories;

import no.jckf.dhsupport.core.database.Database;
import no.jckf.dhsupport.core.database.models.LodModel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

public class LodRepository
{
    protected Database database;

    protected Logger logger;

    public LodRepository(Database database)
    {
        this.database = database;
    }

    public void setLogger(Logger logger)
    {
        this.logger = logger;
    }

    public Logger getLogger()
    {
        return this.logger;
    }

    public LodModel saveLod(UUID worldId, int sectionX, int sectionZ, byte[] data, byte[] beacons)
    {
        int timestamp = (int) (System.currentTimeMillis() / 1000);

        String sql = "REPLACE INTO lods (worldId, x, z, data, beacons, timestamp) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = this.database.getConnection().prepareStatement(sql)) {
            statement.setString(1, worldId.toString());
            statement.setInt(2, sectionX);
            statement.setInt(3, sectionZ);
            statement.setBytes(4, data);
            statement.setBytes(5, beacons);
            statement.setInt(6, timestamp);

            statement.executeUpdate();

            return LodModel.create()
                .setWorldId(worldId)
                .setX(sectionX)
                .setZ(sectionZ)
                .setData(data)
                .setBeacons(beacons)
                .setTimestamp(timestamp);
        } catch (SQLException exception) {
            this.getLogger().warning("Could not save LOD: " + exception);

            return null;
        }
    }

    public LodModel loadLod(UUID worldId, int sectionX, int sectionZ)
    {
        String sql = "SELECT data, beacons, timestamp FROM lods WHERE worldId = ? AND x = ? AND z = ? LIMIT 1";

        try (PreparedStatement statement = this.database.getConnection().prepareStatement(sql)) {
            statement.setString(1, worldId.toString());
            statement.setInt(2, sectionX);
            statement.setInt(3, sectionZ);

            ResultSet result = statement.executeQuery();

            byte[] data = result.getBytes("data");

            if (data == null) {
                return null;
            }

            return LodModel.create()
                .setWorldId(worldId)
                .setX(sectionX)
                .setZ(sectionZ)
                .setData(data)
                .setBeacons(result.getBytes("beacons"))
                .setTimestamp(result.getInt("timestamp"));
        } catch (SQLException exception) {
            this.getLogger().warning("Could not load LOD: " + exception);

            return null;
        }
    }

    public boolean lodExists(UUID worldId, int sectionX, int sectionZ)
    {
        String sql = "SELECT EXISTS( SELECT 1 FROM lods WHERE worldId = ? AND x = ? AND z = ? )";

        try (PreparedStatement statement = this.database.getConnection().prepareStatement(sql)) {
            statement.setString(1, worldId.toString());
            statement.setInt(2, sectionX);
            statement.setInt(3, sectionZ);

            ResultSet result = statement.executeQuery();

            return result.getInt(1) == 1;
        } catch (SQLException exception) {
            this.getLogger().warning("Could not check LOD existence: " + exception);

            return false;
        }
    }

    public boolean deleteLod(UUID worldId, int sectionX, int sectionZ)
    {
        String sql = "DELETE FROM lods WHERE worldId = ? AND x = ? AND z = ?";

        try (PreparedStatement statement = this.database.getConnection().prepareStatement(sql)) {
            statement.setString(1, worldId.toString());
            statement.setInt(2, sectionX);
            statement.setInt(3, sectionZ);

            statement.executeUpdate();

            return true;
        } catch (SQLException exception) {
            this.getLogger().warning("Could not delete LOD: " + exception);

            return false;
        }
    }
}
