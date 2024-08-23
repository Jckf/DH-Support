package no.jckf.dhsupport.core.configuration;

import no.jckf.dhsupport.core.world.WorldInterface;

public class WorldConfiguration extends Configuration
{
    protected static String WORLD_PREFIX = "worlds.%s.";

    protected WorldInterface world;

    protected Configuration config;

    public WorldConfiguration(WorldInterface world, Configuration config)
    {
        this.world = world;
        this.config = config;
    }

    @Override
    public void set(String key, Object value)
    {
        this.config.set(WORLD_PREFIX.formatted(this.world.getName()) + key, value);
    }

    @Override
    public Object get(String key)
    {
        Object specific = this.config.get(WORLD_PREFIX.formatted(this.world.getName()) + key);

        return specific == null ? this.config.get(key) : specific;
    }
}
