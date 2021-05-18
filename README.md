# CustomPotions
[Releases](https://github.com/IIInitiationnn/CustomPotions/releases)

A Bukkit plugin to create custom potions using brewing recipes.

### Compatibility
Tested with Spigot 1.16.1.

### Overview
- Functionality to create potions with custom effects, names, colours and recipes.
- Commands called using `/custompotions` or its alias `/cp`.
- Changes to the configuration file `config.yml` or potion data file `potions.yml` loaded using `/cp reload`.
    - Warning: changing a potion in `potions.yml` will not update all the recipes which use it. It is highly recommended
    you use `/cp modify` to edit potions.
- GUI uses localized names in the itemstack metadata, which could potentially conflict with other mods, plugins or non-Vanilla configurations.

### Data
- `potions.yml` stores:
    - Potion names using the codes [here](https://minecraft.gamepedia.com/Formatting_codes) with the symbol §.
    - Material names from [here](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html).
    - Effect names from [here](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html).
    - Effect durations in seconds.

### Features
- Allows use of the vanilla brewing stand to brew potions.
    - Allows choice of the ingredient (top slot) from all Minecraft materials, and the predecessor (bottom three slots)
     from all vanilla and custom potions.
- Allows potions to have multiple predecessor-ingredient combinations, and multiple effects.
- Full flexibility over choice of brewing recipes with the following caveats:
    - The predecessor must be an existing Vanilla or valid custom potion.
    - No two potions with the same predecessor can have the same ingredient corresponding with that predecessor.
- Allows customised formatting of potion names using the codes from
    [here](https://minecraft.gamepedia.com/Formatting_codes) with the symbol &.

### Issues
- Converting potions from drinkable / splash to lingering in the `cp modify` menu will divide effect durations by 4.
Similarly, converting from lingering to splash will multiply durations by 4. To easily change potion types this way
without having to manually re-enter all durations with the appropriate factor of 4, simply change the type in `potions.yml` and reload.
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