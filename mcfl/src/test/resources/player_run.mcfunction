# player_run.mcfunction
#
# From: https://www.planetminecraft.com/data-pack/moving-blocks-5621017/
# This file is used to test if MCFL can parse a random .mcfunction file
#

execute as @s run function moving_blocks:get_r
data modify block 0 -64 0 Items set value 0

execute as @s store result score @s moving_blocks_PosX_dec run data get entity @s Pos[0] 100
execute as @s store result score @s moving_blocks_PosX run data get entity @s Pos[0] 1
execute as @s run scoreboard players operation @s moving_blocks_PosX *= 100 moving_blocks_Con
execute as @s run scoreboard players operation @s moving_blocks_PosX_dec -= @s moving_blocks_PosX

execute as @s store result score @s moving_blocks_PosZ_dec run data get entity @s Pos[2] 100
execute as @s store result score @s moving_blocks_PosZ run data get entity @s Pos[2]
execute as @s run scoreboard players operation @s moving_blocks_PosZ *= 100 moving_blocks_Con
execute as @s run scoreboard players operation @s moving_blocks_PosZ_dec -= @s moving_blocks_PosZ

gamerule doTileDrops false
execute at @s if score @s moving_blocks_PosX_dec matches 69 if block ~1 ~1 ~ air unless block ~1 ~ ~ air unless block ~1 ~ ~ bedrock unless block ~1 ~ ~ obsidian if block ~2 ~ ~ air if score @s moving_blocks_Direction matches 2 run loot replace block 0 -64 0 container.0 mine ~1 ~ ~ netherite_pickaxe{Enchantments:[{id:"minecraft:silk_touch",lvl:1s}]}
execute at @s if score @s moving_blocks_PosX_dec matches 69 if block ~1 ~1 ~ air unless block ~1 ~ ~ air unless block ~1 ~ ~ bedrock unless block ~1 ~ ~ obsidian if block ~2 ~ ~ air if score @s moving_blocks_Direction matches 2 if block 0 -64 0 minecraft:chest{Items:[{Slot:0b,Count:1b}]} align xz run summon falling_block ~2.50 ~ ~0.50 {Time:10}
execute at @s if score @s moving_blocks_PosX_dec matches 69 if block ~1 ~1 ~ air unless block ~1 ~ ~ air unless block ~1 ~ ~ bedrock unless block ~1 ~ ~ obsidian if block ~2 ~ ~ air if score @s moving_blocks_Direction matches 2 run data modify entity @e[type=minecraft:falling_block,limit=1,sort=nearest] BlockState.Name set from block 0 -64 0 Items[0].id
execute at @s if score @s moving_blocks_PosX_dec matches 69 if block ~1 ~1 ~ air unless block ~1 ~ ~ air unless block ~1 ~ ~ bedrock unless block ~1 ~ ~ obsidian if block ~2 ~ ~ air if score @s moving_blocks_Direction matches 2 if block 0 -64 0 minecraft:chest{Items:[{Slot:0b,Count:1b}]} run setblock ~1 ~ ~ air destroy

execute at @s if score @s moving_blocks_PosX_dec matches 30 if block ~-1 ~1 ~ air unless block ~-1 ~ ~ air unless block ~-1 ~ ~ bedrock unless block ~-1 ~ ~ obsidian if block ~-2 ~ ~ air if score @s moving_blocks_Direction matches 4 run loot replace block 0 -64 0 container.0 mine ~-1 ~ ~ netherite_pickaxe{Enchantments:[{id:"minecraft:silk_touch",lvl:1s}]}
execute at @s if score @s moving_blocks_PosX_dec matches 30 if block ~-1 ~1 ~ air unless block ~-1 ~ ~ air unless block ~-1 ~ ~ bedrock unless block ~-1 ~ ~ obsidian if block ~-2 ~ ~ air if score @s moving_blocks_Direction matches 4 if block 0 -64 0 minecraft:chest{Items:[{Slot:0b,Count:1b}]} align xz run summon falling_block ~-1.50 ~ ~0.50 {Time:10}
execute at @s if score @s moving_blocks_PosX_dec matches 30 if block ~-1 ~1 ~ air unless block ~-1 ~ ~ air unless block ~-1 ~ ~ bedrock unless block ~-1 ~ ~ obsidian if block ~-2 ~ ~ air if score @s moving_blocks_Direction matches 4 run data modify entity @e[type=minecraft:falling_block,limit=1,sort=nearest] BlockState.Name set from block 0 -64 0 Items[0].id
execute at @s if score @s moving_blocks_PosX_dec matches 30 if block ~-1 ~1 ~ air unless block ~-1 ~ ~ air unless block ~-1 ~ ~ bedrock unless block ~-1 ~ ~ obsidian if block ~-2 ~ ~ air if score @s moving_blocks_Direction matches 4 if block 0 -64 0 minecraft:chest{Items:[{Slot:0b,Count:1b}]} run setblock ~-1 ~ ~ air destroy

execute at @s if score @s moving_blocks_PosZ_dec matches 69 if block ~ ~1 ~1 air unless block ~ ~ ~1 air unless block ~ ~ ~1 bedrock unless block ~ ~ ~1 obsidian if block ~ ~ ~2 air if score @s moving_blocks_Direction matches 3 run loot replace block 0 -64 0 container.0 mine ~ ~ ~1 netherite_pickaxe{Enchantments:[{id:"minecraft:silk_touch",lvl:1s}]}
execute at @s if score @s moving_blocks_PosZ_dec matches 69 if block ~ ~1 ~1 air unless block ~ ~ ~1 air unless block ~ ~ ~1 bedrock unless block ~ ~ ~1 obsidian if block ~ ~ ~2 air if score @s moving_blocks_Direction matches 3 if block 0 -64 0 minecraft:chest{Items:[{Slot:0b,Count:1b}]} align xz run summon falling_block ~0.50 ~ ~2.50 {Time:10}
execute at @s if score @s moving_blocks_PosZ_dec matches 69 if block ~ ~1 ~1 air unless block ~ ~ ~1 air unless block ~ ~ ~1 bedrock unless block ~ ~ ~1 obsidian if block ~ ~ ~2 air if score @s moving_blocks_Direction matches 3 run data modify entity @e[type=minecraft:falling_block,limit=1,sort=nearest] BlockState.Name set from block 0 -64 0 Items[0].id
execute at @s if score @s moving_blocks_PosZ_dec matches 69 if block ~ ~1 ~1 air unless block ~ ~ ~1 air unless block ~ ~ ~1 bedrock unless block ~ ~ ~1 obsidian if block ~ ~ ~2 air if score @s moving_blocks_Direction matches 3 if block 0 -64 0 minecraft:chest{Items:[{Slot:0b,Count:1b}]} run setblock ~ ~ ~1 air destroy

execute at @s if score @s moving_blocks_PosZ_dec matches 30 if block ~ ~1 ~-1 air unless block ~ ~ ~-1 air unless block ~ ~ ~-1 bedrock unless block ~ ~ ~-1 obsidian if block ~ ~ ~-2 air if score @s moving_blocks_Direction matches 1 run loot replace block 0 -64 0 container.0 mine ~ ~ ~-1 netherite_pickaxe{Enchantments:[{id:"minecraft:silk_touch",lvl:1s}]}
execute at @s if score @s moving_blocks_PosZ_dec matches 30 if block ~ ~1 ~-1 air unless block ~ ~ ~-1 air unless block ~ ~ ~-1 bedrock unless block ~ ~ ~-1 obsidian if block ~ ~ ~-2 air if score @s moving_blocks_Direction matches 1 if block 0 -64 0 minecraft:chest{Items:[{Slot:0b,Count:1b}]} align xz run summon falling_block ~0.50 ~ ~-1.50 {Time:10}
execute at @s if score @s moving_blocks_PosZ_dec matches 30 if block ~ ~1 ~-1 air unless block ~ ~ ~-1 air unless block ~ ~ ~-1 bedrock unless block ~ ~ ~-1 obsidian if block ~ ~ ~-2 air if score @s moving_blocks_Direction matches 1 run data modify entity @e[type=minecraft:falling_block,limit=1,sort=nearest] BlockState.Name set from block 0 -64 0 Items[0].id
execute at @s if score @s moving_blocks_PosZ_dec matches 30 if block ~ ~1 ~-1 air unless block ~ ~ ~-1 air unless block ~ ~ ~-1 bedrock unless block ~ ~ ~-1 obsidian if block ~ ~ ~-2 air if score @s moving_blocks_Direction matches 1 if block 0 -64 0 minecraft:chest{Items:[{Slot:0b,Count:1b}]} run setblock ~ ~ ~-1 air destroy
gamerule doTileDrops true