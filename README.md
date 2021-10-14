# CustomPotions
[Releases](https://github.com/IIInitiationnn/CustomPotions/releases)

A Bukkit plugin to create custom potions using brewing recipes.

### Compatibility
Tested with Spigot 1.16.1.

### Overview
- Functionality to create potions with custom effects, names, colours and recipes.
- Commands are called using `/custompotions` or its alias `/cp`.
- Changes to the configuration file `config.yml` or potion data file `potions.json` must be loaded using `/cp reload`.
    - Warning: it is highly recommended that you use `/cp modify` to edit potions.
- GUI uses localized names in the itemstack metadata, which could potentially conflict with other mods, plugins or non-Vanilla configurations.

### Data
- `potions.json` stores:
    - Potion names using the codes [here](https://minecraft.fandom.com/wiki/Formatting_codes) with the symbol §.
    - Material names from [here](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html).
    - Effect names from [here](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html).
    - Effect durations in [ticks](https://minecraft.fandom.com/wiki/Tick) (the game runs at 20 ticks per second).

### Features
- Allows use of the vanilla brewing stand to brew potions.
    - Allows choice of the ingredient (top slot) from all Minecraft materials, and the base potion (bottom three slots)
     from all vanilla and custom potions.
- Allows potions to have multiple base-ingredient combinations, and multiple effects.
- Full flexibility over choice of brewing recipes with the following caveats:
    - The base must be an existing Vanilla or valid custom potion.
    - No two potions with the same base can have the same ingredient corresponding with that base.
- Allows customised formatting of potion names using the codes from
    [here](https://minecraft.gamepedia.com/Formatting_codes) with the symbol &.

### Issues
- Known incompatibilities with ItemScroller when trying to scroll ingredients out of brewing stands.
    - Custom ingredients cannot be scrolled out of brewing stands.
    - Vanilla ingredients will be destroyed (half the stack).
- Other inventory QOL client-side mods are untested.
    - Please report if any issues are found whilst using other client-side mods.
- It is highly recommended you blacklist (or toggle off) brewing stand interaction from any such mods.

### Commands
| Command | Description | Permission |
| ------- | ----------- | ---------- |
| `cp info` | Displays all custom potions. | `custompotions.brew` |
| `cp reload` | Reloads the config and plugin. | `custompotions.reload` |
| `cp modify` |  Allows you to edit and create new potions with custom effects. | `custompotions.modify` |
| `cp give` | Allows you to withdraw a quantity of a custom potion. | `custompotions.modify` |
### Permissions
| Permission | Description | Default |
| ---------- | ----------- | ------- |
| `custompotions.brew` | Permission to use `cp info`. | All |
| `custompotions.reload` | Permission to use `cp reload`. | Operator |
| `custompotions.modify` | Permission to use `cp modify` and `cp give`. | Operator |