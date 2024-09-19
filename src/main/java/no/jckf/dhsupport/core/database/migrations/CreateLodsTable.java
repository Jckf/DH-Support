package no.jckf.dhsupport.core.database.migrations;

import no.jckf.dhsupport.core.database.Database;

import java.sql.Statement;

public class CreateLodsTable extends Migration
{
    public CreateLodsTable(Database database)
    {
        super(database);
    }

    @Override
    public void up() throws Exception
    {
        try (Statement statement = this.database.getConnection().createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS lods (
                    worldId STRING,
                    x INTEGER,
                    z INTEGER,
                    data BLOB,
                    timestamp INTEGER,
                    PRIMARY KEY (worldId, x, z)
                );
            """);
        }
    }
}
