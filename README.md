# Necromancy

A Forge Minecraft 1.20.1 mod where the player becomes a necromancer by reading the **Book of the Scourge**.

## Current playable slice

- Trade emeralds for the Book of the Scourge from a cleric villager (the cleric is the first acolyte implementation).
- Reading the book unlocks Dark Energy. It regenerates to 100 and is spent by the staff.
- Craft the Staff of the Necromancer with two amethyst shards and three sticks.
- Sneak-right-click the staff to switch between **Battle** and **Summon** modes.
- Battle mode spends 8 energy to fire a ranged dark bolt.
- Summon mode spends 25 energy and raises a Skeleton Warrior, Skeleton Archer, or Skeleton Worker (cycle the selection in the staff menu command/API).
- Press **N** for the summon menu; use **Undead Commands** to choose Follow, Roam, or Defend.
- Selecting Skeleton Worker opens its work menu: Mine Wood (axe), Mine Stone (pickaxe), Stop and Store Items, or inspect its 9-slot inventory.
- Run `/necromancy menu` for the in-game control guide.

The first slice uses tagged vanilla mobs so it is compatible with vanilla AI and multiplayer. The worker is currently a clearly marked gathering prototype; chest targeting, stack-based jobs, command buttons, and HUD rendering are queued for the next iteration.

## Build

Requires Java 17 and internet access to the Forge and Mojang Maven repositories.

```sh
./gradlew build
```
