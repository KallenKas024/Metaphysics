package net.minecraft.client.multiplayer;

import com.mojang.logging.LogUtils;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientChunkCache extends ChunkSource {
   static final Logger LOGGER = LogUtils.getLogger();
   private final LevelChunk emptyChunk;
   private final LevelLightEngine lightEngine;
   volatile ClientChunkCache.Storage storage;
   final ClientLevel level;

   public ClientChunkCache(ClientLevel pLevel, int pViewDistance) {
      this.level = pLevel;
      this.emptyChunk = new EmptyLevelChunk(pLevel, new ChunkPos(0, 0), pLevel.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS));
      this.lightEngine = new LevelLightEngine(this, true, pLevel.dimensionType().hasSkyLight());
      this.storage = new ClientChunkCache.Storage(calculateStorageRange(pViewDistance));
   }

   public LevelLightEngine getLightEngine() {
      return this.lightEngine;
   }

   private static boolean isValidChunk(@Nullable LevelChunk pChunk, int pX, int pZ) {
      if (pChunk == null) {
         return false;
      } else {
         ChunkPos chunkpos = pChunk.getPos();
         return chunkpos.x == pX && chunkpos.z == pZ;
      }
   }

   /**
    * Unload chunk from ChunkProviderClient's hashmap. Called in response to a Packet50PreChunk with its mode field set
    * to false
    */
   public void drop(int pX, int pZ) {
      if (this.storage.inRange(pX, pZ)) {
         int i = this.storage.getIndex(pX, pZ);
         LevelChunk levelchunk = this.storage.getChunk(i);
         if (isValidChunk(levelchunk, pX, pZ)) {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.level.ChunkEvent.Unload(levelchunk));
            this.storage.replace(i, levelchunk, (LevelChunk)null);
         }

      }
   }

   /**
    * Gets the chunk at the provided position, if it exists.
    * Note: This method <strong>can deadlock</strong> when called from within an existing chunk load, as it will be
    * stuck waiting for the current chunk to load!
    * @param pLoad If this should force a chunk load. When {@code false}, this will return null if the chunk is not
    * loaded.
    */
   @Nullable
   public LevelChunk getChunk(int pChunkX, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad) {
      if (this.storage.inRange(pChunkX, pChunkZ)) {
         LevelChunk levelchunk = this.storage.getChunk(this.storage.getIndex(pChunkX, pChunkZ));
         if (isValidChunk(levelchunk, pChunkX, pChunkZ)) {
            return levelchunk;
         }
      }

      return pLoad ? this.emptyChunk : null;
   }

   public BlockGetter getLevel() {
      return this.level;
   }

   public void replaceBiomes(int pX, int pZ, FriendlyByteBuf pBuffer) {
      if (!this.storage.inRange(pX, pZ)) {
         LOGGER.warn("Ignoring chunk since it's not in the view range: {}, {}", pX, pZ);
      } else {
         int i = this.storage.getIndex(pX, pZ);
         LevelChunk levelchunk = this.storage.chunks.get(i);
         if (!isValidChunk(levelchunk, pX, pZ)) {
            LOGGER.warn("Ignoring chunk since it's not present: {}, {}", pX, pZ);
         } else {
            levelchunk.replaceBiomes(pBuffer);
         }

      }
   }

   @Nullable
   public LevelChunk replaceWithPacketData(int pX, int pZ, FriendlyByteBuf pBuffer, CompoundTag pTag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> pConsumer) {
      if (!this.storage.inRange(pX, pZ)) {
         LOGGER.warn("Ignoring chunk since it's not in the view range: {}, {}", pX, pZ);
         return null;
      } else {
         int i = this.storage.getIndex(pX, pZ);
         LevelChunk levelchunk = this.storage.chunks.get(i);
         ChunkPos chunkpos = new ChunkPos(pX, pZ);
         if (!isValidChunk(levelchunk, pX, pZ)) {
            levelchunk = new LevelChunk(this.level, chunkpos);
            levelchunk.replaceWithPacketData(pBuffer, pTag, pConsumer);
            this.storage.replace(i, levelchunk);
         } else {
            levelchunk.replaceWithPacketData(pBuffer, pTag, pConsumer);
         }

         this.level.onChunkLoaded(chunkpos);
         net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.level.ChunkEvent.Load(levelchunk, false));
         return levelchunk;
      }
   }

   public void tick(BooleanSupplier pHasTimeLeft, boolean pTickChunks) {
   }

   public void updateViewCenter(int pX, int pZ) {
      this.storage.viewCenterX = pX;
      this.storage.viewCenterZ = pZ;
   }

   public void updateViewRadius(int pViewDistance) {
      int i = this.storage.chunkRadius;
      int j = calculateStorageRange(pViewDistance);
      if (i != j) {
         ClientChunkCache.Storage clientchunkcache$storage = new ClientChunkCache.Storage(j);
         clientchunkcache$storage.viewCenterX = this.storage.viewCenterX;
         clientchunkcache$storage.viewCenterZ = this.storage.viewCenterZ;

         for(int k = 0; k < this.storage.chunks.length(); ++k) {
            LevelChunk levelchunk = this.storage.chunks.get(k);
            if (levelchunk != null) {
               ChunkPos chunkpos = levelchunk.getPos();
               if (clientchunkcache$storage.inRange(chunkpos.x, chunkpos.z)) {
                  clientchunkcache$storage.replace(clientchunkcache$storage.getIndex(chunkpos.x, chunkpos.z), levelchunk);
               }
            }
         }

         this.storage = clientchunkcache$storage;
      }

   }

   private static int calculateStorageRange(int pViewDistance) {
      return Math.max(2, pViewDistance) + 3;
   }

   /**
    * @return A human-readable string representing data about this chunk source.
    */
   public String gatherStats() {
      return this.storage.chunks.length() + ", " + this.getLoadedChunksCount();
   }

   public int getLoadedChunksCount() {
      return this.storage.chunkCount;
   }

   public void onLightUpdate(LightLayer pType, SectionPos pPos) {
      Minecraft.getInstance().levelRenderer.setSectionDirty(pPos.x(), pPos.y(), pPos.z());
   }

   @OnlyIn(Dist.CLIENT)
   final class Storage {
      final AtomicReferenceArray<LevelChunk> chunks;
      final int chunkRadius;
      private final int viewRange;
      volatile int viewCenterX;
      volatile int viewCenterZ;
      int chunkCount;

      Storage(int pChunkRadius) {
         this.chunkRadius = pChunkRadius;
         this.viewRange = pChunkRadius * 2 + 1;
         this.chunks = new AtomicReferenceArray<>(this.viewRange * this.viewRange);
      }

      int getIndex(int pX, int pZ) {
         return Math.floorMod(pZ, this.viewRange) * this.viewRange + Math.floorMod(pX, this.viewRange);
      }

      protected void replace(int pChunkIndex, @Nullable LevelChunk pChunk) {
         LevelChunk levelchunk = this.chunks.getAndSet(pChunkIndex, pChunk);
         if (levelchunk != null) {
            --this.chunkCount;
            ClientChunkCache.this.level.unload(levelchunk);
         }

         if (pChunk != null) {
            ++this.chunkCount;
         }

      }

      protected LevelChunk replace(int pChunkIndex, LevelChunk pChunk, @Nullable LevelChunk pReplaceWith) {
         if (this.chunks.compareAndSet(pChunkIndex, pChunk, pReplaceWith) && pReplaceWith == null) {
            --this.chunkCount;
         }

         ClientChunkCache.this.level.unload(pChunk);
         return pChunk;
      }

      boolean inRange(int pX, int pZ) {
         return Math.abs(pX - this.viewCenterX) <= this.chunkRadius && Math.abs(pZ - this.viewCenterZ) <= this.chunkRadius;
      }

      @Nullable
      protected LevelChunk getChunk(int pChunkIndex) {
         return this.chunks.get(pChunkIndex);
      }

      private void dumpChunks(String pFilePath) {
         try (FileOutputStream fileoutputstream = new FileOutputStream(pFilePath)) {
            int i = ClientChunkCache.this.storage.chunkRadius;

            for(int j = this.viewCenterZ - i; j <= this.viewCenterZ + i; ++j) {
               for(int k = this.viewCenterX - i; k <= this.viewCenterX + i; ++k) {
                  LevelChunk levelchunk = ClientChunkCache.this.storage.chunks.get(ClientChunkCache.this.storage.getIndex(k, j));
                  if (levelchunk != null) {
                     ChunkPos chunkpos = levelchunk.getPos();
                     fileoutputstream.write((chunkpos.x + "\t" + chunkpos.z + "\t" + levelchunk.isEmpty() + "\n").getBytes(StandardCharsets.UTF_8));
                  }
               }
            }
         } catch (IOException ioexception) {
            ClientChunkCache.LOGGER.error("Failed to dump chunks to file {}", pFilePath, ioexception);
         }

      }
   }
}
