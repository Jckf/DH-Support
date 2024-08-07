# Server-side support for [Distant Horizons](https://gitlab.com/jeseibel/distant-horizons)

DH Support is a Bukkit/Spigot/Paper server plugin that transmits Level Of Detail (LOD) data to connected clients in order to provide a much higher view distance than Minecraft normally would allow. Distant Horizons _will_ work fine without this plugin, but then each client will have to be within normal view distance of chunks to load them, and they will not receive updates for distant chunks when they change. 

## Installation

Download the [latest release](https://github.com/Jckf/DH-Support/releases) and drop the JAR in your plugins folder, and you're done!

## Configuration

The default values should be pretty solid, but you may tweak them to better suit your specific needs. Everything you need to know should be in config.yml.

## Compilation

The project uses Maven, so just run `mvn` in the project directory to compile and package a new JAR.

## Contribution

There are several ways to contribute to this project. You can offer your feedback on [the DH Discord](https://discord.gg/xAB8G4cENx) in [this thread](https://discord.com/channels/881614130614767666/1154490009735417989), report any issues or bugs you find, or attack an open issue and submit a pull request.
