package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class LevelChunkSection {
   public static final int SECTION_WIDTH = 16;
   public static final int SECTION_HEIGHT = 16;
   public static final int SECTION_SIZE = 4096;
   public static final int BIOME_CONTAINER_BITS = 2;
   private short nonEmptyBlockCount;
   private short tickingBlockCount;
   private short tickingFluidCount;
   private final PalettedContainer<BlockState> states;
   private PalettedContainerRO<Holder<Biome>> biomes;

   public LevelChunkSection(PalettedContainer<BlockState> pStates, PalettedContainerRO<Holder<Biome>> pBiomes) {
      this.states = pStates;
      this.biomes = pBiomes;
      this.recalcBlockCounts();
   }

   public LevelChunkSection(Registry<Biome> pBiomeRegistry) {
      this.states = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
      this.biomes = new PalettedContainer<>(pBiomeRegistry.asHolderIdMap(), pBiomeRegistry.getHolderOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
   }

   public BlockState getBlockState(int pX, int pY, int pZ) {
      return this.states.get(pX, pY, pZ);
   }

   public FluidState getFluidState(int pX, int pY, int pZ) {
      return this.states.get(pX, pY, pZ).getFluidState();
   }

   public void acquire() {
      this.states.acquire();
   }

   public void release() {
      this.states.release();
   }

   public BlockState setBlockState(int pX, int pY, int pZ, BlockState pState) {
      return this.setBlockState(pX, pY, pZ, pState, true);
   }

   public BlockState setBlockState(int pX, int pY, int pZ, BlockState pState, boolean pUseLocks) {
      BlockState blockstate;
      if (pUseLocks) {
         blockstate = this.states.getAndSet(pX, pY, pZ, pState);
      } else {
         blockstate = this.states.getAndSetUnchecked(pX, pY, pZ, pState);
      }

      FluidState fluidstate = blockstate.getFluidState();
      FluidState fluidstate1 = pState.getFluidState();
      if (!blockstate.isAir()) {
         --this.nonEmptyBlockCount;
         if (blockstate.isRandomlyTicking()) {
            --this.tickingBlockCount;
         }
      }

      if (!fluidstate.isEmpty()) {
         --this.tickingFluidCount;
      }

      if (!pState.isAir()) {
         ++this.nonEmptyBlockCount;
         if (pState.isRandomlyTicking()) {
            ++this.tickingBlockCount;
         }
      }

      if (!fluidstate1.isEmpty()) {
         ++this.tickingFluidCount;
      }

      return blockstate;
   }

   /**
    * @return {@code true} if this section consists only of air-like blocks.
    */
   public boolean hasOnlyAir() {
      return this.nonEmptyBlockCount == 0;
   }

   public boolean isRandomlyTicking() {
      return this.isRandomlyTickingBlocks() || this.isRandomlyTickingFluids();
   }

   /**
    * @return {@code true} if this section has any blocks that require random ticks.
    */
   public boolean isRandomlyTickingBlocks() {
      return this.tickingBlockCount > 0;
   }

   /**
    * @return {@code true} if this section has any fluids that require random ticks.
    */
   public boolean isRandomlyTickingFluids() {
      return this.tickingFluidCount > 0;
   }

   public void recalcBlockCounts() {
      class BlockCounter implements PalettedContainer.CountConsumer<BlockState> {
         public int nonEmptyBlockCount;
         public int tickingBlockCount;
         public int tickingFluidCount;

         public void accept(BlockState p_204444_, int p_204445_) {
            FluidState fluidstate = p_204444_.getFluidState();
            if (!p_204444_.isAir()) {
               this.nonEmptyBlockCount += p_204445_;
               if (p_204444_.isRandomlyTicking()) {
                  this.tickingBlockCount += p_204445_;
               }
            }

            if (!fluidstate.isEmpty()) {
               this.nonEmptyBlockCount += p_204445_;
               if (fluidstate.isRandomlyTicking()) {
                  this.tickingFluidCount += p_204445_;
               }
            }

         }
      }

      BlockCounter levelchunksection$1blockcounter = new BlockCounter();
      this.states.count(levelchunksection$1blockcounter);
      this.nonEmptyBlockCount = (short)levelchunksection$1blockcounter.nonEmptyBlockCount;
      this.tickingBlockCount = (short)levelchunksection$1blockcounter.tickingBlockCount;
      this.tickingFluidCount = (short)levelchunksection$1blockcounter.tickingFluidCount;
   }

   public PalettedContainer<BlockState> getStates() {
      return this.states;
   }

   public PalettedContainerRO<Holder<Biome>> getBiomes() {
      return this.biomes;
   }

   public void read(FriendlyByteBuf pBuffer) {
      this.nonEmptyBlockCount = pBuffer.readShort();
      this.states.read(pBuffer);
      PalettedContainer<Holder<Biome>> palettedcontainer = this.biomes.recreate();
      palettedcontainer.read(pBuffer);
      this.biomes = palettedcontainer;
   }

   public void readBiomes(FriendlyByteBuf pBuffer) {
      PalettedContainer<Holder<Biome>> palettedcontainer = this.biomes.recreate();
      palettedcontainer.read(pBuffer);
      this.biomes = palettedcontainer;
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeShort(this.nonEmptyBlockCount);
      this.states.write(pBuffer);
      this.biomes.write(pBuffer);
   }

   public int getSerializedSize() {
      return 2 + this.states.getSerializedSize() + this.biomes.getSerializedSize();
   }

   /**
    * @return {@code true} if this section has any states matching the given predicate. As the internal representation
    * uses a {@link net.minecraft.world.level.chunk.Palette}, this is more efficient than looping through every position
    * in the section, or indeed the chunk.
    */
   public boolean maybeHas(Predicate<BlockState> pPredicate) {
      return this.states.maybeHas(pPredicate);
   }

   public Holder<Biome> getNoiseBiome(int pX, int pY, int pZ) {
      return this.biomes.get(pX, pY, pZ);
   }

   public void fillBiomesFromNoise(BiomeResolver pBiomeResolver, Climate.Sampler pClimateSampler, int pX, int pY, int pZ) {
      PalettedContainer<Holder<Biome>> palettedcontainer = this.biomes.recreate();
      int i = 4;

      for(int j = 0; j < 4; ++j) {
         for(int k = 0; k < 4; ++k) {
            for(int l = 0; l < 4; ++l) {
               palettedcontainer.getAndSetUnchecked(j, k, l, pBiomeResolver.getNoiseBiome(pX + j, pY + k, pZ + l, pClimateSampler));
            }
         }
      }

      this.biomes = palettedcontainer;
   }
}