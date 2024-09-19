package no.jckf.dhsupport.core.database.migrations;

import no.jckf.dhsupport.core.database.Database;

public abstract class Migration
{
    protected Database database;

    public Migration(Database database)
    {
        this.database = database;
    }

    public abstract void up() throws Exception;
}
