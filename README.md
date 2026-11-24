# Abyssal Dominion Coins

A Paper (1.21.x) server plugin that awards coins when players kill mobs,
multiplies the payout by the player's LuckPerms rank, and lets you tune
per-entity (i.e. ecomobs boss) coin values from config.

## Requirements
- Java 17+
- Maven
- A Paper server running the matching Minecraft version
- The [Vault](https://www.spigotmc.org/resources/vault.34315/) plugin
- The [LuckPerms](https://www.luckperms.net/) plugin

## Project layout
```
abyssaldominioncoins/
├── pom.xml
├── src/main/java/com/abyssal/dominion/coins/
│   ├── AbyssalDominionCoins.java   # Main plugin class, commands, economy, luckperms
│   └── events/
│       └── MobKillListener.java    # Kills -> coins listener
└── src/main/resources/
    ├── plugin.yml
    └── config.yml
```

## Build
```bash
cd abyssaldominioncoins
mvn package
```
The built plugin is `target/abyssaldominioncoins-1.0.0.jar`.

> The `paper.version` property in `pom.xml` must match your server's Minecraft
> version (Paper is versioned like `1.21.11-R0.1-SNAPSHOT`). Look at your
> server banner, then grab that exact Paper snapshot from
> https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/.
> "26.2" is not a valid Paper version string.

## Install
1. Copy `target/abyssaldominioncoins-1.0.0.jar` into your server's
   `plugins/` folder.
2. (Optional) drop a copy of `config.yml` into the plugin folder.
3. Start the server. The plugin will generate a fresh `config.yml` if none
   exists.
4. Reload with `/adc reload`.

## Commands
- `/adc reload` — reload config.yml (op only)
- `/adc balance` — check your coin balance (player only)

## Config reference

### Per-entity coin overrides (`entity-coin-value`)
Keys are entity registry names, e.g. `ecomobs:myboss` or `minecraft:ender_dragon`.
Give a boss extra coins by adding its exact registry name. Keys are
case-sensitive.

If you want to see a full list of registry names, run this on the server
temporarily and look for your ecomobs boss:
```java
System.out.println(entity.getType().getKey().getValue());
```

### Boss bonus (`boss-multiplier`)
A global multiplier applied on top of the base and per-entity values.
Set to `1.0` to disable.

### LuckPerms rank multiplier (`rank-multipliers`)
The payout is multiplied by the multiplier for the player's **highest
priority** LuckPerms group. Group names are case-sensitive, so if you
rename a group in LuckPerms you must also update this config.

Example: a `member` (1.0) kills an `ender_dragon` (25 coins, boss 2.0) =
50 coins. An `admin` (3.0) doing the same = 150 coins.

## LuckPerms integration notes
- Requires **LuckPerms 5.x**.
- The plugin reads groups via `LuckPermsProvider` at runtime; the plugin
  works with no LuckPerms installed (uses `default` = 1.0).

## Troubleshooting
- **No coins?**:
  - Confirm Vault is loaded: `plugins/Vault` in `plugins/`.
  - Check the server console for "Abyssal Dominion Coins enabled".
  - Make sure the killer is an online player (offline/console kills pay nothing).
- **Boss coins not applying?**:
  - Use the exact registry name (case-sensitive). Debug the name with the
    snippet above or via ecomobs config.
- **Multiplier not applying?**:
  - Match group names exactly (lowercase, no spaces).
  - Toggle a real group on the player; "default" gives ×1.0.
