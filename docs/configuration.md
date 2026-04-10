# ⚙️ Configuration JSON

Resourceful Sheep is **fully customizable**. The mod is entirely data-driven, meaning you can create your own sheep types, define mutations, and control where they spawn using simple JSON files.

!!! info "File Location"
    All config files are located in `config/resourceful_sheep/` inside your Minecraft instance. They are loaded at game start. **Every change needs a full restart of the game** `/reload` is not enough.

---

## 1. Sheep Types

This is the main configuration file. Each file defines a sheep type and creates variants based on Tiers.

### 1.1 Global Settings applied to all tiers

* `SheepName`: UNIQUE resource ID (Example: `"cobblestone"`, `"refined_glowstone"`).
* `EggColorBackground`: Hex color for the spawn egg background (Example: `"#88771f"`).
* `EggColorSpotsNTitle`: Hex color for the egg spots and the item name hover text.
* `FireImmune`: `true` or `false`. If true, the sheep cannot be hurt by fire or lava.
* `ImmuneEffects`: A list of potion effects that cannot affect the sheep (Example: `["minecraft:poison", "minecraft:wither"]`).
* `FoodItems`: A list of items used to tempt or breed the sheep.
    * You can use item IDs: `"minecraft:carrot"`
    * You can use **Tags** (start with `#`): `"#minecraft:leaves"`
* `EtableBocksMap`: A list of rules defining what the sheep eats and what the block becomes.
    * **Format**: `"Block_To_Eat": "Block_Result"`
    * **Tags**: You can use tags (only for the input block), example: `"#c:cobblestone": "minecraft:air"`.
    * **Destruction**: Use `"minecraft:air"` as the result to destroy the block completely, like eating short grass.

### 1.2 Tiers & Drops Settings

In the `SheepTier` list, you define the progression. Each tier creates a unique sheep variant (Example: `andesite_tier_1`).

* `Tier`: An integer defining the tier level.
* `DroppedItems`: A list of items dropped when sheared. You can have multiple different items per tier.
    * `ItemId`: The item ID (e.g., `"minecraft:diamond"`). This fully supports **Tags** (e.g., `"#forge:ingots/iron"`).
    * `MinDrops` / `MaxDrops`: The quantity range.

### 1.3 Sheep Type JSON file Example

```json title="netherrack_sheep.json"
{
  "SheepName": "netherrack",
  "EggColorBackground": "#5a2b2b",
  "EggColorSpotsNTitle": "#3d1d1d",
  "FoodItems": [
    "minecraft:nether_wart"
  ],
  "FireImmune": true,
  "ImmuneEffects": [],
  "EtableBocksMap": {
    "minecraft:warped_nylium": "minecraft:netherrack",
    "minecraft:crimson_fungus": "minecraft:air",
    "minecraft:warped_roots": "minecraft:air",
    "minecraft:warped_fungus": "minecraft:air",
    "minecraft:crimson_nylium": "minecraft:netherrack",
    "minecraft:crimson_roots": "minecraft:air"
  },
  "SheepTier": [
    {
      "Tier": 1,
      "DroppedItems": [
        {
          "ItemId": "minecraft:netherrack",
          "MinDrops": 12,
          "MaxDrops": 20
        }
      ]
    },
    {
      "Tier": 2,
      "DroppedItems": [
        {
          "ItemId": "minecraft:netherrack",
          "MinDrops": 20,
          "MaxDrops": 30
        }
      ]
    },
    {
      "Tier": 3,
      "DroppedItems": [
        {
          "ItemId": "minecraft:netherrack",
          "MinDrops": 30,
          "MaxDrops": 42
        }
      ]
    }
  ]
}
```

### 1.4 Texture Generation

Textures are generated automatically based on the drops. The mod analyzes the colors of the exact items and blocks defined in the variant's `DroppedItems` list to create a weighted color palette.

Wool color coverage increases by **10% per tier**. Maximum coverage is **60%**. If a sheep type has more than 6 tiers, the coverage scales proportionally but never exceeds 60%.

---

## 2. Mutations

### 2.1 Settings

These files define breeding recipes for new sheep when using classic breeding (feeding two sheep).

* `MomId`: ID of the first parent (Example: `"iron_tier_1"`).
* `DadId`: ID of the second parent.
* `ChildId`: ID of the new sheep.
* `Chance`: Integer (1 to 100) representing the chance of success.

!!! warning "Probability Rule"
    If you define multiple possible mutations for the same parents, their total chance must be **≤ 100**. Otherwise, some mutations will be ignored. If no mutation happens or it fails, there is a **50/50 chance** to get either parent instead.

### 2.2 Mutation JSON file Example

```json title="steel_tier_2.json"
{
"MomId": "steel_tier_1",
"DadId": "steel_tier_1",
"ChildId": "steel_tier_2",
"Chance": 30
}
```

---

## 3. DNA Crossbreeding

### 3.2 Settings

You can create custom DNA crossbreeding recipes for the **DNA Splicer** machine.

* `MomId`: The ID of the first DNA sequence (Example: `"resourceful_sheep:gold_tier_1"`). *Note: You must include the namespace here.*
* `DadId`: The ID of the second DNA sequence.
* `ChildId`: The ID of the resulting sheep if the DNA splicing is successful.
* `Chance`: The success rate as a percentage (from 1 to 100).
* `ResultsIfFail`: A list of entity IDs that the game will randomly choose from if the crossbreeding fails (Example: `["minecraft:creeper", "minecraft:sheep"]`).

!!! warning "Splicing Failure Logic"
    Unlike natural breeding (Mutations), the parent sheep are **not** automatically selected upon failure. If you want the player to potentially get the parent DNA back when a Splicer operation fails, you must explicitly add the parents' IDs to the `ResultsIfFail` list!

### 3.2 DNA Crossbreeding JSON file Example

```json title="wither_tier_1.json"
{
"MomId": "minecraft:wither",
"DadId": "minecraft:sheep",
"ChildId": "resourceful_sheep:wither_tier_1",
"ResultsIfFail": [
    "minecraft:sheep",
    "minecraft:wither_skeleton"
],
"Chance": 15
}
```

---

## 4. Spawning

### 4.1 Settings

Here you define how sheep appear in the world naturally.

* `sheepId`: Sheep ID (Example: `"netherrack_tier_1"`).
* `minCount` / `maxCount`: Size of the sheep group.
* `densityRadius` / `maxNearby`: Control how often sheep spawn. Roughly `maxNearby` sheep every `densityRadius` blocks.
* `requireSeeSky`: If set to `true`, the sheep will only spawn if there are no solid blocks above it (Use `true` for Overworld surface, `false` for caves/Nether).
* `Biomes`: List of biomes where the sheep can spawn (Example: `["minecraft:plains"]`). **Tags are supported** (e.g., `"#minecraft:is_forest"`). If left empty, they spawn wherever vanilla sheep spawn.

!!! info "Experimental Settings Warning"
    When creating a new world, Minecraft may show a warning about **experimental features**. This happens because the mod injects a *dynamic datapack* at runtime to handle sheep spawning cleanly without overwriting other mods. It is **100% safe to ignore** and proceed.

    If this warning really bothers you or if you are building a modpack and want to hide it from your players, you can completely disable the mod's natural spawning system. To do this, delete all the spawning JSON files and create a single empty JSON file in the folder to prevent the default configs from regenerating.
    
    **However**, if you do this, the mod offers no alternative way to naturally spawn sheep. You will have to provide another method in your modpack such as custom crafting recipes, custom structures, or using other mods for players to get their very first sheep!

### 4.2 Spawning JSON file Example

```json title="gold_tier_1.json"
{
  "sheepId": "gold_tier_1",
  "minCount": 1,
  "maxCount": 1,
  "densityRadius": 1024,
  "maxNearby": 1,
  "Biomes": [
    "minecraft:wooded_badlands"
  ],
  "RequireSeeSky": true
}
```

---

## 5. Helpful Tips

* The mod checks your JSON files on startup. If you make a typo in a biome, item, or effect ID, the mod will print a helpful warning in your console logs to help you fix it!