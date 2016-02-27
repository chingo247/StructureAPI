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
      * SettlerCraft-StructureAPI-2.x.x.zip
2. Put all these dependencies in the plugins directory of your server.

3. Unzip/extract 'SettlerCraft-StructureAPI-2.x.x.zip' within the plugins directory, so that you will have both SettlerCraft-StructureAPI-Bukkit-2.x.x-jar and the 'SettlerCraft-StructureAPI' directory within the plugins directory of your server.

4. Download both WorldEdit and AsyncWorldEdit from the resources described below. Note that AsyncWorldEdit has a dependency called AsyncWorldEditInjector.(which can be downloaded from the same resource as AsyncWorldEdit. Put all these dependencies/plugins in the plugins directory of your server

## Dependencies
* [SettlerCraft-Core](https://github.com/chingo247/SettlerCraft/releases/) - Core functionality of SettlerCraft, contains a distribution of [Neo4j](http://neo4j.com/) database
* [SettlerCraft-MenuAPI](https://github.com/chingo247/SettlerCraft/releases/) - Menu library of SettlerCraft
* [WorldEdit 6.1](http://dev.bukkit.org/bukkit-plugins/worldedit/files/61-world-edit-6-1-up-to-mc-1-8-6/)
* [AsyncWorldEdit](https://github.com/SBPrime/AsyncWorldEdit/releases|) - version 2.2.x (**Note:** you will need both **AsyncWorldEdit** and **AsyncWorldEditInjector**)


 

