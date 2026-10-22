<p align="center"><img src="src/main/resources/hollowmancy_logo.png" alt="Hollowmancy" width="560"></p>

<p align="center"><b>A magic mod for Minecraft 1.21.1 (NeoForge)</b></p>

Rise from a mundane player to Archmage through seven stages of magic: spells, rituals, familiars and wands that grow with use. Then cross the veil into **the Hollow**, a dimension that has been listening for a long time.

Everything is explained in game by the **Arcane Journal**, which you receive the moment you awaken.

## Features

- **Seven stages**, each earned by doing something in the world: Mundane, Awakening, Apprentice, Arcane Arts, Mastery, Ascension, Archmage
- **Two dozen spells** across six schools (General, Elemental, Warding, Divination, Nature, Forbidden), each growing through four tiers, with legendary forms at tier IV
- **Spell crafting** from runes and scrolls, a Spellbook, a spell wheel and quick-cast slots
- **Rituals** at the Ritual Altar to infuse artifacts, fuse gear and call bosses
- **Five familiars** that grow with your bond, and **wands** with five levels of mastery
- **Mana machines**: batteries, conduits, engines, the Infuser and the Mana Beacon
- **The Hollow**: six biomes, its own creatures (no vanilla mobs), ruins, a sunken library and a floating castle
- **Seven bosses**, from the Hollow Sovereign to the Listener, plus a rematch for Archmages
- Original sounds and music, an advancement tree, JEI and Jade support, and config options for spawn rates, particles and reduced flashing

## Requirements

| | |
|---|---|
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.262 or newer |
| Required | [Patchouli](https://modrinth.com/mod/patchouli) |
| Optional | [JEI](https://modrinth.com/mod/jei), [Jade](https://modrinth.com/mod/jade) |

Install it on both the client and the server.

## Download

Hollowmancy 1.0.0 is in review on Modrinth and CurseForge. Links will be added here when it is live.

## Reporting bugs

Please open an issue on this repository's **Issues** tab. It helps a lot to include:

- your Hollowmancy, NeoForge and Minecraft versions
- what you did, what you expected, and what happened
- your `latest.log` (in the `logs` folder), or the crash report if the game crashed

## Building from source

You need Java 21.

```
git clone <this repository's URL>
cd hollowmancy
./gradlew build
```

The jar is written to `build/libs/`. To run a test client, use `./gradlew runClient`.

The `blockbench/` folder holds the boss models as Blockbench projects, if you want to look at or edit them.

## Translating

All text is in language files. See [TRANSLATING.md](TRANSLATING.md) for how to add a language.

## License

Hollowmancy is released under the [MIT License](LICENSE). The Gradle build files come from the NeoForge MDK and are covered by [TEMPLATE_LICENSE.txt](TEMPLATE_LICENSE.txt).

Made by Matthew Porter (Walnut Beach Digital). Built with NeoForge, Patchouli and JEI.
