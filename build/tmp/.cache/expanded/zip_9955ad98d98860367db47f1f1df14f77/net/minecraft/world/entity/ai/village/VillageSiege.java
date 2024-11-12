package net.minecraft.world.entity.ai.village;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class VillageSiege implements CustomSpawner {
   private static final Logger LOGGER = LogUtils.getLogger();
   private boolean hasSetupSiege;
   private VillageSiege.State siegeState = VillageSiege.State.SIEGE_DONE;
   private int zombiesToSpawn;
   private int nextSpawnTime;
   private int spawnX;
   private int spawnY;
   private int spawnZ;

   public int tick(ServerLevel pLevel, boolean pSpawnHostiles, boolean pSpawnPassives) {
      if (!pLevel.isDay() && pSpawnHostiles) {
         float f = pLevel.getTimeOfDay(0.0F);
         if ((double)f == 0.5D) {
            this.siegeState = pLevel.random.nextInt(10) == 0 ? VillageSiege.State.SIEGE_TONIGHT : VillageSiege.State.SIEGE_DONE;
         }

         if (this.siegeState == VillageSiege.State.SIEGE_DONE) {
            return 0;
         } else {
            if (!this.hasSetupSiege) {
               if (!this.tryToSetupSiege(pLevel)) {
                  return 0;
               }

               this.hasSetupSiege = true;
            }

            if (this.nextSpawnTime > 0) {
               --this.nextSpawnTime;
               return 0;
            } else {
               this.nextSpawnTime = 2;
               if (this.zombiesToSpawn > 0) {
                  this.trySpawn(pLevel);
                  --this.zombiesToSpawn;
               } else {
                  this.siegeState = VillageSiege.State.SIEGE_DONE;
               }

               return 1;
            }
         }
      } else {
         this.siegeState = VillageSiege.State.SIEGE_DONE;
         this.hasSetupSiege = false;
         return 0;
      }
   }

   private boolean tryToSetupSiege(ServerLevel pLevel) {
      for(Player player : pLevel.players()) {
         if (!player.isSpectator()) {
            BlockPos blockpos = player.blockPosition();
            if (pLevel.isVillage(blockpos) && !pLevel.getBiome(blockpos).is(BiomeTags.WITHOUT_ZOMBIE_SIEGES)) {
               for(int i = 0; i < 10; ++i) {
                  float f = pLevel.random.nextFloat() * ((float)Math.PI * 2F);
                  this.spawnX = blockpos.getX() + Mth.floor(Mth.cos(f) * 32.0F);
                  this.spawnY = blockpos.getY();
                  this.spawnZ = blockpos.getZ() + Mth.floor(Mth.sin(f) * 32.0F);
                  Vec3 siegeLocation = this.findRandomSpawnPos(pLevel, new BlockPos(this.spawnX, this.spawnY, this.spawnZ));
                  if (siegeLocation != null) {
                     if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.village.VillageSiegeEvent(this, pLevel, player, siegeLocation))) return false;
                     this.nextSpawnTime = 0;
                     this.zombiesToSpawn = 20;
                     break;
                  }
               }

               return true;
            }
         }
      }

      return false;
   }

   private void trySpawn(ServerLevel pLevel) {
      Vec3 vec3 = this.findRandomSpawnPos(pLevel, new BlockPos(this.spawnX, this.spawnY, this.spawnZ));
      if (vec3 != null) {
         Zombie zombie;
         try {
            zombie = EntityType.ZOMBIE.create(pLevel); //Forge: Direct Initialization is deprecated, use EntityType.
            zombie.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(zombie.blockPosition()), MobSpawnType.EVENT, (SpawnGroupData)null, (CompoundTag)null);
         } catch (Exception exception) {
            LOGGER.warn("Failed to create zombie for village siege at {}", vec3, exception);
            return;
         }

         zombie.moveTo(vec3.x, vec3.y, vec3.z, pLevel.random.nextFloat() * 360.0F, 0.0F);
         pLevel.addFreshEntityWithPassengers(zombie);
      }
   }

   @Nullable
   private Vec3 findRandomSpawnPos(ServerLevel pLevel, BlockPos pPos) {
      for(int i = 0; i < 10; ++i) {
         int j = pPos.getX() + pLevel.random.nextInt(16) - 8;
         int k = pPos.getZ() + pLevel.random.nextInt(16) - 8;
         int l = pLevel.getHeight(Heightmap.Types.WORLD_SURFACE, j, k);
         BlockPos blockpos = new BlockPos(j, l, k);
         if (pLevel.isVillage(blockpos) && Monster.checkMonsterSpawnRules(EntityType.ZOMBIE, pLevel, MobSpawnType.EVENT, blockpos, pLevel.random)) {
            return Vec3.atBottomCenterOf(blockpos);
         }
      }

      return null;
   }

   static enum State {
      SIEGE_CAN_ACTIVATE,
      SIEGE_TONIGHT,
      SIEGE_DONE;
   }
}
