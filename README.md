# CustomPotions
[Releases](https://github.com/IIInitiationnn/CustomPotions/releases)

A Bukkit plugin to create custom potions using brewing recipes.

### Compatibility
Tested with Spigot 1.16.1.

### Overview
- Functionality to create potions with custom effects, names, colours and recipes.
- Commands called using `/custompotions` or its alias `/cp`.
- Changes to the configuration file `config.yml` or potion data file `potions.yml` loaded using `/cp reload`.
- Potions can be easily cloned by making copies in the potion data file `potions.yml` and reloading the plugin.

### Data
- `potions.yml` stores:
    - Material names as they are found [here](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html).
    - Effect names as they are found [here](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html).
    - Effect durations in seconds.

### Features
- Allows use of the vanilla brewing stand to brew potions.
    - Allows choice of the ingredient (top slot) from all Minecraft materials, and the predecessor (bottom three slots)
     from all vanilla and custom potions.
- Allows potions to have multiple predecessor-ingredient combinations, and multiple effects.
- Full flexibility over choice of predecessor with the following caveats:
    - Predecessor must be an existing Vanilla or custom potion.
    - No two potions with the same predecessor can have the same ingredient corresponding with that predecessor.
    - ~~Predecessor can be assigned to be the potion itself (we let you handle the redundancy).~~

### Issues
- Converting potions from drinkable / splash to lingering will divide effect durations by 4. Similarly, converting from
lingering to splash will multiply effect durations by 4. To easily change potion types this way without having to
manually re-enter all durations with the appropriate factor of 4, simply change the type in `potions.yml` and reload.
- Known incompatibilities with ItemScroller when trying to scroll ingredients out of brewing stands.
    - Custom ingredients cannot be scrolled out of brewing stands.
    - Vanilla ingredients will be destroyed (half the stack).
- Other inventory QOL client-side mods are untested.
    - Please report if any issues are found whilst using other client-side mods.
- It is highly recommended you blacklist (or toggle off) brewing stand interaction from any such mods.

### Commands
| Command | Description | Permission |
| ------- | ----------- | ---------- |
| `cp info <potion>` | Displays all information about the potion. | `custompotions.brew` |
| `cp list` | Displays all custom potions. | `custompotions.brew` |
| `cp reload` | Reloads the config and plugin. | `custompotions.reload` |
| `cp modify` |  Allows you to edit and create new potions with custom effects. | `custompotions.modify` |
| `cp give <potion> <quantity>` | Allows you to withdraw a quantity of a custom potion. | `custompotions.modify` |
### Permissions
| Permission | Description | Default |
| ---------- | ----------- | ------- |
| `custompotions.brew` | Permission to use `cp info` and `cp list`. | All |
| `custompotions.reload` | Permission to use `cp reload`. | Operator |
| `custompotions.modify` | Permission to use `cp modify` and `cp give`. | Operator |