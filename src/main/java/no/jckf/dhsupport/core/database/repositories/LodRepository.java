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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// TODO: Add logger.
public class LodRepository
{
    protected Database database;

    protected Map<String, LodModel> queuedSaves = new ConcurrentHashMap<>();

    public LodRepository(Database database)
    {
        this.database = database;
    }

    public boolean saveLod(UUID worldId, int sectionX, int sectionZ, byte[] data, int timestamp)
    {
        String sql = "INSERT INTO lods (worldId, x, z, data, timestamp) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = this.database.getConnection().prepareStatement(sql)) {
            statement.setString(1, worldId.toString());
            statement.setInt(2, sectionX);
            statement.setInt(3, sectionZ);
            statement.setBytes(4, data);
            statement.setInt(5, timestamp);

            statement.executeUpdate();
        } catch (SQLException exception) {
            return false;
        }

        return true;
    }

    public void saveLodQueued(UUID worldId, int sectionX, int sectionZ, byte[] data)
    {
        LodModel lod = LodModel.create()
            .setWorldId(worldId)
            .setX(sectionX)
            .setZ(sectionZ)
            .setData(data)
            .setTimestamp((int) (System.currentTimeMillis() / 1000));

        this.queuedSaves.put(lod.toString(), lod);
    }

    public int processQueuedSaves()
    {
        int processed = 0;

        for (String key : this.queuedSaves.keySet())
        {
            LodModel lod = this.queuedSaves.get(key);

            if (this.saveLod(lod.getWorldId(), lod.getX(), lod.getZ(), lod.getData(), lod.getTimestamp())) {
                processed++;
            }

            this.queuedSaves.remove(key);
        }

        return processed;
    }

    public LodModel loadLod(UUID worldId, int sectionX, int sectionZ)
    {
        String sql = "SELECT data, timestamp FROM lods WHERE worldId = ? AND x = ? AND z = ? LIMIT 1";

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
                .setTimestamp(result.getInt("timestamp"));
        } catch (SQLException exception) {
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

        }

        return false;
    }

    public boolean deleteLod(UUID worldId, int sectionX, int sectionZ)
    {
        String sql = "DELETE FROM lods WHERE worldId = ? AND x = ? AND z = ?";

        try (PreparedStatement statement = this.database.getConnection().prepareStatement(sql)) {
            statement.setString(1, worldId.toString());
            statement.setInt(2, sectionX);
            statement.setInt(3, sectionZ);

            statement.executeUpdate();
        } catch (SQLException exception) {
            return false;
        }

        return true;
    }
}
