# Worthwhile Wandering Trader

A wandering trader worth stopping for.

Worthwhile Wandering Trader replaces the wandering trader's flowers and dyes with goods that are hard to get otherwise: a chance at a Heart of the Sea or a Totem of Undying, music discs, armor trim templates, explorer maps, and building blocks from far-away biomes. The trader also buys a few things players have lying around, such as hay bales and baked potatoes. Every trade is set in a server config file.

The mod runs on the server only. Players join with a plain vanilla client and trade as usual.

Available for Forge (also runs on NeoForge 1.20.1) and Fabric, for Minecraft 1.20.1. Items and structures added in Minecraft 1.21, such as the trial chambers map, are not part of this version.

## Features

- **New trades.** Every wandering trader offers 3 buy trades and 8 sell trades: 1 very rare (with a 25% chance), 4 rare and 3 useful. When the very rare roll fails, the trader gets a fifth rare trade instead.
- **Different every time.** Each trader picks its trades at random from the lists in the config, and items such as music discs, trim templates, logs and goat horns are picked at random per trader. No trade appears twice on one trader.
- **Explorer maps.** A map trade leads to the nearest village, desert pyramid, jungle temple or ancient city that is not on any other map yet. It looks like a cartographer's map, with a target marker and a name such as "Plains Village Map". When nothing is found nearby, the trader offers another rare trade instead.
- **Everything is configurable.** Prices, amounts, how often a trade can be used, how many trades of each kind a trader has, the very rare chance and the map search radius. Any item can be used, including items from other mods.
- **Replace or add.** By default the vanilla trades are replaced. The `add` mode keeps them and adds the configured trades after them.
- **Vanilla feel.** Trades behave like vanilla wandering trader trades: same experience reward, no restocking, and the normal trading screen.

## Default trades

Prices are in emeralds. "Uses" is how many times a trader offers the trade.

### Buy (3 per trader)

| The player gives | Gets | Uses |
|---|---|---|
| 1 Hay Bale | 1 emerald | 2 |
| 1 Water Bottle | 1 emerald | 2 |
| 4 Baked Potatoes | 1 emerald | 2 |
| 1 Milk Bucket | 2 emeralds | 2 |
| 1 Water Bucket | 2 emeralds | 2 |
| 1 Fermented Spider Eye | 3 emeralds | 2 |

Only real water bottles are accepted; bottles of any other potion are refused.

### Very rare (1 per trader, 25% chance)

| Item | Price | Uses |
|---|---|---|
| Heart of the Sea | 32 | 1 |
| Sniffer Egg | 24 | 1 |
| Totem of Undying | 48 | 1 |
| Trident | 40 | 1 |

### Rare (4 per trader, 5 when there is no very rare trade)

| Item | Price | Uses |
|---|---|---|
| Name Tag | 8 | 2 |
| Saddle | 8 | 2 |
| Music Disc (any vanilla disc, random) | 12 | 1 |
| Armor Trim Smithing Template (any vanilla template, random; not the Netherite Upgrade) | 16 | 1 |
| Sponge | 6 | 4 |
| Echo Shard | 8 | 4 |
| Goat Horn (random instrument of a normal goat: Ponder, Sing, Seek or Feel) | 10 | 1 |
| 2 Ender Pearls | 6 | 4 |
| Eye of Ender | 8 | 2 |
| Explorer map to a village, desert pyramid, jungle temple or ancient city (random) | 12 | 1 |

### Useful (3 per trader)

| Item | Price | Uses |
|---|---|---|
| 8 Ice | 2 | 6 |
| 8 Logs (cherry, mangrove, jungle, acacia or dark oak, random) | 1 | 8 |
| 2 Saplings (cherry, mangrove propagule, jungle, acacia, dark oak, bamboo or cocoa beans, random) | 1 | 6 |
| Mob Head (zombie, skeleton, creeper or piglin, random) | 8 | 1 |
| Pottery Sherd (any vanilla sherd, random) | 4 | 2 |
| Nautilus Shell | 5 | 3 |

## How trades are picked

A wandering trader picks its trades the first time a player opens it, or the first time it is saved (for example by an autosave shortly after it appears), whichever comes first. This is when vanilla picks them too. After that the trader keeps its trades until it disappears; changing the config does not change traders that already have trades.

For each group the trader picks entries at random, without picking the same entry twice. An entry with a higher `weight` is picked more often. When a group has fewer entries than slots, the trader offers all of them. When an entry cannot be made (a map that finds nothing), another entry of the same group is picked.

## Explorer maps

A map entry lists structures, for example villages and ancient cities. When the trader picks its trades, one of them is chosen at random and the server searches for the nearest one that is not on any map yet, the same way a cartographer does. If none is found within the search radius, the other structures of the entry are tried; if none of them is found either, the trader gets another trade from the same group.

- Minecraft 1.20.1 only has map icons for the woodland mansion and the ocean monument, so those maps use them and every other structure is marked with a red X, like a buried treasure map. Mansion, monument and buried treasure maps have their vanilla names; the others are named after the structure, such as "Plains Village Map", "Jungle Explorer Map" or "Desert Pyramid Map", in English.
- A structure handed out on a map is marked as known, exactly like vanilla explorer maps. Neither this mod nor cartographers send players to it again.
- Maps are made in the dimension the trader is in. Searching for overworld structures from the Nether finds nothing, so the trader gets another trade.

The search runs on the server thread, so the server waits for it, just like it does for a cartographer's map. A structure close by is found quickly; one that is far away, or that is not found at all, takes the longest because more of the world is searched, and can cause a short pause when a trader picks its trades. If that happens, lower `mapSearchRadius` or remove those structures (for example the ancient city) from the map entry. Each search is logged at debug level with its duration (`logs/debug.log`, lines starting with `Map search for`).

## Requirements

| | Forge / NeoForge | Fabric |
|---|---|---|
| Minecraft | 1.20.1 | 1.20 or 1.20.1 |
| Loader | Forge 47.1.0 or newer, or NeoForge 1.20.1 (47.1.x) | Fabric Loader 0.14.21 or newer |
| Other | | Fabric API 0.83.0 or newer |
| Jar | `worthwhilewanderingtrader-forge-1.20.1-<version>.jar` | `worthwhilewanderingtrader-fabric-1.20-1.20.1-<version>.jar` |

On Minecraft 1.20.1, NeoForge still runs Forge mods, so the Forge jar is used for both.

The Fabric jar bundles [NightConfig](https://github.com/TheElectronWill/night-config) 3.6.4 (core and toml, LGPL-3.0) to read the TOML config. Forge and NeoForge already ship it.

### Client and server

Install the mod on the server only, on every loader. It adds no items, blocks, entities, network messages or registries, and the trades only contain items the client already knows:

- **Forge and NeoForge:** vanilla clients and Forge or NeoForge clients without the mod can join and trade. The mod is marked as server-only, so a client without it still shows the server as compatible in the server list.
- **Fabric:** vanilla clients and Fabric clients without the mod can join and trade. Fabric API is only needed on the server.

Installing it on a client as well does no harm.

In singleplayer, install it in your own game; the built-in server runs it.

If the config uses items from another mod, players need that mod as usual.

## Configuration

### Server: `config/worthwhilewanderingtrader-server.toml`

The file is created on the first server (or world) start with every default trade and comments that explain each option. It is read every time a server or world starts. The mod never rewrites it, so your edits and comments stay.

```toml
# "replace": wandering traders only offer the trades in this file.
# "add": the trades in this file are added after the vanilla trades.
mode = "replace"

buySlots = 3
veryRareSlots = 1
veryRareChance = 0.25
rareSlots = 4
usefulSlots = 3

mapSearchRadius = 100
```

- `mode`: `"replace"` (default) or `"add"`.
- `buySlots`, `veryRareSlots`, `rareSlots`, `usefulSlots`: how many trades a trader gets from each group, 0 to 64. `0` turns a group off.
- `veryRareChance`: the chance from `0.0` to `1.0` that a very rare slot is filled. A slot that is not filled (also when the very rare group has run out of entries) becomes an extra rare trade. Default `0.25`.
- `mapSearchRadius`: how far map trades search, 1 to 1000, in the same unit as the cartographer's explorer maps. Default `100`, the vanilla value.

### Trades

Each trade is a `[[buy]]`, `[[veryRare]]`, `[[rare]]` or `[[useful]]` table:

```toml
[[rare]]
price = { item = "minecraft:emerald", count = 8 }
result = { item = "minecraft:name_tag", count = 1 }
maxUses = 2
```

| Key | Meaning |
|---|---|
| `price` | What the player pays. Required. |
| `price2` | A second item the player pays. Optional. |
| `result` | What the player gets. Required. |
| `maxUses` | How many times the trade can be used, at least 1. Required. |
| `weight` | How often the entry is picked compared to the others in its group, at least 1. Default 1. |

`price`, `price2` and `result` take an `item` and a `count` from 1 to 64, at most the item's max stack size (so unstackable items such as saddles always have count 1). Default count is 1. Any item id works, also from other mods.

More examples:

```toml
# Two prices: 4 emeralds and 4 bone meal for 8 logs.
[[useful]]
price = { item = "minecraft:emerald", count = 4 }
price2 = { item = "minecraft:bone_meal", count = 4 }
result = { item = "minecraft:oak_log", count = 8 }
maxUses = 4

# A random item: each trader offers one of these, picked when it picks its trades.
[[useful]]
price = { item = "minecraft:emerald", count = 1 }
result = { count = 8, items = [
    "minecraft:oak_log",
    "minecraft:birch_log",
] }
maxUses = 8
weight = 2

# An item with a potion. As a price only that exact potion is accepted.
[[buy]]
price = { item = "minecraft:potion", potion = "minecraft:water", count = 1 }
result = { item = "minecraft:emerald", count = 1 }
maxUses = 2

# A goat horn with a random instrument from a tag (or a single instrument id such as "minecraft:dream_goat_horn").
[[rare]]
price = { item = "minecraft:emerald", count = 10 }
result = { item = "minecraft:goat_horn", instrument = "#minecraft:regular_goat_horns", count = 1 }
maxUses = 1

# An explorer map. Structure ids or #tags.
[[rare]]
price = { item = "minecraft:emerald", count = 12 }
result = { map = ["#minecraft:village", "minecraft:ancient_city"] }
maxUses = 1
```

- `potion` works on potions, splash and lingering potions and tipped arrows.
- `instrument` is only used in a `result`. A goat horn as a price accepts any goat horn.
- `map` is only used in a `result`; the map always counts 1.

A group without entries offers nothing. To remove a trade, delete its table; to turn a whole group off, set its slots to 0.

Mistakes do not crash the server. An entry with an unknown item, a count out of range, a missing `maxUses`, an unknown potion or instrument, or no known structure is skipped with a warning in the log. An unknown item inside an `items` list, or an unknown structure inside a `map` list, is only left out of that list. Other invalid values (an unknown `mode`, slots out of range) use their default with a warning, and unknown keys are reported and ignored. If the file cannot be read at all (for example a TOML syntax error), the default trades are used, the file is left untouched and the log says why.

The config format is the same as in the 1.21.1 version of the mod, so a file from that version can be copied as it is. Entries with items or structures that do not exist in 1.20.1 (such as the trial chambers in a map list) are skipped or left out of their list with a warning.

## Removing the mod

The world opens normally. Wandering traders that already picked their trades keep them, because the trades are saved with the trader; this includes maps that were already made. New wandering traders get vanilla trades. The config file stays in `config/` until you delete it.

## Compatibility

- **Mods that add wandering trader trades** through the loader's usual way (Forge's wanderer trades event, Fabric API's `TradeOfferHelper`): these trades end up in the vanilla trade lists, so they behave like vanilla trades on every loader. In `replace` mode they are replaced along with the vanilla ones; in `add` mode they stay and the configured trades come after them.
- **Async Locator** (Forge and Fabric) moves vanilla structure searches (cartographer maps, treasure maps from chests, eyes of ender, dolphins, `/locate`) off the main server thread. It does not cover this mod's map trades, which search the same way vanilla's own code does, synchronously. It is still a good companion if vanilla maps make your server stutter; for this mod's maps, use `mapSearchRadius` as described above.
- Other mods that change how the wandering trader picks its trades may conflict; which one wins depends on the mod.

## Building from source

```
./gradlew build
```

The jars are written to `forge/build/libs/` and `fabric/build/libs/`.

## License

MIT. The Fabric jar includes NightConfig, which is licensed under the LGPL-3.0.
