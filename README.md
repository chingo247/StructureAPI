# StructureAPI

## What is StructureAPI?
The StructureAPI is a minecraft server plugin (e.g. Bukkit) that provides a convenient way to load up schematics. Players can pick these schematics (also referred to as plans) from a menu and place them in the world. Scroll down to 'Quick Start' or watch the 'Quick Demo' to know more about how to setup your server with plans. StructureAPI aims to provide servers with a wide range of tools to place structures. 
## Features
* Generate plans from schematics
* Loading plans into a fancy menu
* Select plans from a menu
* Buy plans from a menu (requires Vault)
* Build a structure or continue construction of a existing one
* Demolish a structure
* Stop construction or demolition of a structure
* Rollback a structure (since 2.2.0)
* Structures don't overlap each other
* Substructures, structures may be placed inside other structures (configurable to enable/disable)

## Quick Demo
Youtube: https://www.youtube.com/watch?feature=player_embedded&v=c7G0psVaBcQ

## Quick Start
1. Download schematics from any website (for example minecraft-schematics.com)
2. Place the downloaded schematics within "plugins/SettlerCraft-StructureAPI/generate"
3. Start the server or execute the command "stt generate plans" in the server console
4. Place the schematics together with the generate XML in "plugins/SettlerCraft-StructureAPI/plans"
5. Restart or reload the server
6. Plans are now available in the menu, you can open the menu by either using the /stt menu or /stt shop

Note: There is a small requirement for loading the schematics into SettlerCraft. All schematics need to be placed in neutral position which means the schematic needs to be aligned to the east (front pointing to west), unless you don't care about it's orientation. You can do this by simply using the command '/stt rotate [structureid][degrees]' which will rotate a schematic permanently.


## Installation
1. Download the following required SettlerCraft dependencies from https://github.com/chingo247/SettlerCraft/releases
      * SettlerCraft-Core-Bukkit-2.x.x.jar
      * SettlerCraft-MenuAPI-Bukkit-2.x.x.jar
2. Put all these dependencies in the plugins directory of your server.

3. **SKIP THIS STEP WHEN USING 2.2.0** Unzip/extract 'SettlerCraft-StructureAPI-2.x.x.zip' within the plugins directory, so that you will have both SettlerCraft-StructureAPI-Bukkit-2.x.x-jar and the 'SettlerCraft-StructureAPI' directory within the plugins directory of your server.

4. Download both WorldEdit and AsyncWorldEdit from the resources described below. Note that AsyncWorldEdit has a dependency called AsyncWorldEditInjector.(which can be downloaded from the same resource as AsyncWorldEdit. Put all these dependencies/plugins in the plugins directory of your server

## Dependencies
* [SettlerCraft-Core](https://github.com/chingo247/SettlerCraft/releases/) - Core functionality of SettlerCraft, contains a distribution of [Neo4j](http://neo4j.com/) database
* [SettlerCraft-MenuAPI](https://github.com/chingo247/SettlerCraft/releases/) - Menu library of SettlerCraft
* [WorldEdit 6.1](http://dev.bukkit.org/bukkit-plugins/worldedit/files/61-world-edit-6-1-up-to-mc-1-8-6/)
* [AsyncWorldEdit](https://github.com/SBPrime/AsyncWorldEdit/releases/tag/2.2.2) - version 2.2.x (**Note:** you will need both **AsyncWorldEdit** and **AsyncWorldEditInjector**) or see [The Spigot Page](https://www.spigotmc.org/resources/asyncworldedit.327/)

## Optional Hooks
StructureAPI has hooks to other plugins. For maintainability and other reasons, these hooks are provided separately:
* [StructureAPI-WorldGuard](https://github.com/chingo247/StructureAPI/releases/) - WorldGuard support (requires worldguard 6.0). **NOTE** [Old versions are listed here!](https://github.com/chingo247/SettlerCraft/releases/)
* [StructureAPI-HolographicDisplays](https://github.com/chingo247/StructureAPI/releases/) - Hologram support (requires HolographicDisplays 2.1.x).  **NOTE** [Old versions are listed here!](https://github.com/chingo247/SettlerCraft/releases/)
* [StructureAPI-Towny](https://github.com/chingo247/StructureAPI/releases/) - Support for Towny (since SettlerCraft-2.1.0) and ofcourse requires Towny. Mayors are able to place structures in the wildernis and players won't be able to place structures on or across plots they don't own. . **NOTE**  [Old versions are listed here!](https://github.com/chingo247/SettlerCraft/releases/)
* [Vault 1.5.x](http://dev.bukkit.org/bukkit-plugins/vault/) - Plugin that serves a a Economyhub, 
 
## Client Plugin Hooks (LiteLoader)
Although not required,  you will need [WorldEditCUI](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/1292886-worldeditcui) in order to see areas being 'marked'. 

![WorldEdit CUI Preview](http://dev.bukkit.org/thumbman/images/83/716/600x434/worldedit-cui.PNG.-m1.png "Preview")

## Commands  <div id="commands"/>

### Settler Commands <div id="settlercommands"/>
| Command | Alias | Desciption |
| ------------- |:-------------:| :----- |
| settler:me | settler:whoami | Will tell you you're settler id, this id can be used with some commands for cases where the player name was not unique |

### Structure Commands <div id="structurecommands"/>
| Command | Alias | Arguments | Desciption |
| ------------- |:-------------:|:-------------:| ----- |
| structure:build | stt:build | [Structure-Id] | Start building process of a structure |
| structure:demolish | stt:demolish | [Structure-Id] | Start demolition process of a structure |
| structure:rollback | stt:rollback | [Structure-Id] | Start rollback process of a structure |
| structure:halt | stt:halt | [Structure-Id] | Stop any construction process of a structure |
| structure:list | stt:list | [page] | List structures you own |
| structure:info | stt:info | (id) | Displays information of the structure you are in or with the given id |
| structure:members | stt:members | (Structure-Id) | Display a list of members of a structure |   
| structure:members | stt:members | (Structure-Id) (add/remove) (playername) | Add or remove a player as member of a structure |
| structure:members | stt:members | (Structure-Id) (add/remove) (Settler-Id) | Add or remove a player as member of a structure |
| structure:owners | stt:owners | (Structure-Id) | Display a list of owner of a structure |   
| structure:owners | stt:owners | (Structure-Id) (add/remove) (playername) | Add or remove a player as owner of a structure |  
| structure:owners | stt:owners | (Structure-Id) (add/remove) (Settler-Id) | Add or remove a player as owner of a structure |  
| structure:masters | stt:masters | (Structure-Id) |  Display a list of masters of a structure |   
| structure:masters | stt:masters | (Structure-Id) (add/remove) (playername) | Add or remove a player as master of a structure | 
| structure:masters | stt:masters | (Structure-Id) (add/remove) (Settler-ID) | Add or remove a player as master of a structure | 

### StructurePlan Commands <div id="planscommands"/>
| Command | Desciption |
| ------------- | ------- |
| plans:generate| Generates plans for schematics located in directory: */plugins/SettlerCraft-StructureAPI/generate*. Note can only be executed from console (non-players) |
| plans:menu | opens plan menu where plans are **free** |
| plans:shop | opens plan shop menu where plans are **not free** |
| plans:reload | reloads plans located in directory: */plugins/SettlerCraft-StructureAPI/plans* |

### StructureAPI-WorldGuard Commands (Since 2.3.0) <div id="schematiccommands"/>
| Command | Arguments | Desciption | Permission
| ------------- |:-------:| ----- | ------ |
| sttwg:unistall |  | Removes worldguard protection from all structures  | Only from console |
| sttwg:install |  | Adds worldguard protection to all structures | Only from console |
| sttwg:expire | [structureid] | Expires worldguard protection from a structure with the given id | structureapi.wg.expire.single |
| sttwg:expire | [world] | Expires worldguard protection for all structures within a world | structureapi.wg.expire.world |
| sttwg:protect | [structureid] | Adds worldguard protection to a structure with the given id | structureapi.wg.protect.single |
| sttwg:protect | [world] | Adds worldguard protection to all structures within a world | structureapi.wg.protect.world |

### Schematic Commands <div id="schematiccommands"/>
| Command | Arguments | Desciption |
| ------------- |:-------:| ----- |
| schematic:rotate | (Structure-Id) (degrees) | Rotates a schematic by structure id. Note this will rotate the **original** schematic and will only work for structures that will be placed in the future.

## Permissions

| Permission | Desciption |
| ------------- | ------- |
| structureapi.plans.reload | Allows a player to reload plans using the /plans:reload commands |
| structureapi.schematic.rotate | Allows a player to rotate **the original** schematic using the /schematic:rotate command |
| structureapi.structure.place | Allows a player to place structures using a structureplan |
| structureapi.structure.info | Allows a player to show info of a structure using /stt:info |
| structureapi.structure.list | Allows a player to list structures using /stt:list |
| structureapi.location | Allows a player to see it's current relative position from a structure |
| structureapi.demolish | Allows a player to demolish a structure |
| structureapi.build | Allows a player to build a structure |
| structureapi.rollback | Allows a player to rollback a structure |
| structureapi.halt | Allows a player to halt any construction process of a structure |



