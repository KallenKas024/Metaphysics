package net.minecraft.world.level;

import net.minecraft.server.level.ServerLevel;

public interface CustomSpawner {
   int tick(ServerLevel pLevel, boolean pSpawnEnemies, boolean pSpawnFriendlies);
}