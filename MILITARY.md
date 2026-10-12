# Military System Design

## Overview
Military introduces combat, unit recruitment, war declarations, and
peace treaties. Battles auto-resolve. The player's decisions happen
before (composition, timing, target) and after (loot, peace, leave),
not during.

## Units

Four unit types, starting with Infantry only:
- Infantry: cheap, ground, high losses
- Tank: expensive, ground, high attack
- Warship: naval, area denial
- Plane: air, fast travel, low losses

Each unit is defined in a JSON file with:
- id, name
- attack, defense
- goldCost (recruitment)
- goldUpkeep (per turn)
- oilCost (per unit sent on attack)
- travelTime (turns each way)
- lossResistance (0.0–1.0, higher = survives more)

## Nation Compositions
Every nation (player and rival) has a Map<UnitType, Integer>
representing their army. Rival compositions are seeded in nation JSON
and grow each turn based on their static gold.

Nation JSON additions:
- "military": { "INFANTRY": 5000, ... }
- military growth handled by engine formula

## Recruitment
- Pay gold, receive unit instantly
- No resource-based training for now
- Units add to player composition

## War Declaration
- Button in diplomacy
- Requires relations below a threshold (e.g., -30)
- Enemy can also declare war on player if relations are low
- No warning system; player can see relations dropping

## Attack Flow
1. Declare war in diplomacy
2. Select units to send (UI showing current composition)
3. Pay oil cost per unit
4. Wait travel time (unit-based)
5. Battle auto-resolves
6. Outcome screen: casualties for both sides
7. Choices: Loot / Annex (disabled) / Leave
8. Units return home (travel time again)
9. War continues unless ended

## Combat Formula
playerPower = sum(count × attack) for units sent
enemyPower = sum(count × defense) for defending units
ratio = playerPower / enemyPower

Losses computed per unit type:
- baseLoss from ratio (close fights = higher losses)
- adjusted by each unit's lossResistance
- random variance (+/-20%)

Enemy losses computed similarly.

## War Endings
- Peace treaty (player or enemy-initiated)
- Surrender (one side loses all units)
- Annexation (disabled for now)
- Stalemate (neither side can attack)

## Peace Treaties
- Player-initiated via diplomacy button
- Enemy accepts if their strength < player's × 0.7
- Enemy-initiated when their military is weakened enough
- Terms: war ends, units return home

## Losing a Battle
- 15% flat annexation chance
- If annexed: game over (popup summary, then exit/restart)
- Otherwise: player is looted (loses % of gold and resources)

## Loot (After Winning)
- Take a percentage of the defeated nation's gold and resources
- Percentage TBD (suggested 20%)

## Rival Military Growth
- Each turn, rival military grows
- Rate depends on their static gold as a prosperity multiplier
- Richer nations militarize faster
- No full economy simulation for rivals

## Game Over
- Triggered by annexation
- Popup summary
- Endgame details (restart, score) TBD

## Future Additions
- Additional unit types (Tanks, Warships, Planes)
- Resource-based unit training (armor, helmets, personnel)
- Oil as a resource (produced by Oil Rigs)
- Annexation and territory system
- Espionage
- Alliances (military cooperation)
- Unit composition for rivals (generated, not static)