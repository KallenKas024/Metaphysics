package net.minecraft.world.level.material;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class FluidState extends StateHolder<Fluid, FluidState> implements net.minecraftforge.common.extensions.IForgeFluidState {
   public static final Codec<FluidState> CODEC = codec(BuiltInRegistries.FLUID.byNameCodec(), Fluid::defaultFluidState).stable();
   public static final int AMOUNT_MAX = 9;
   public static final int AMOUNT_FULL = 8;

   public FluidState(Fluid pOwner, ImmutableMap<Property<?>, Comparable<?>> pValues, MapCodec<FluidState> pPropertiesCodec) {
      super(pOwner, pValues, pPropertiesCodec);
   }

   public Fluid getType() {
      return this.owner;
   }

   public boolean isSource() {
      return this.getType().isSource(this);
   }

   public boolean isSourceOfType(Fluid pFluid) {
      return this.owner == pFluid && this.owner.isSource(this);
   }

   public boolean isEmpty() {
      return this.getType().isEmpty();
   }

   public float getHeight(BlockGetter pLevel, BlockPos pPos) {
      return this.getType().getHeight(this, pLevel, pPos);
   }

   public float getOwnHeight() {
      return this.getType().getOwnHeight(this);
   }

   public int getAmount() {
      return this.getType().getAmount(this);
   }

   public boolean shouldRenderBackwardUpFace(BlockGetter pLevel, BlockPos pPos) {
      for(int i = -1; i <= 1; ++i) {
         for(int j = -1; j <= 1; ++j) {
            BlockPos blockpos = pPos.offset(i, 0, j);
            FluidState fluidstate = pLevel.getFluidState(blockpos);
            if (!fluidstate.getType().isSame(this.getType()) && !pLevel.getBlockState(blockpos).isSolidRender(pLevel, blockpos)) {
               return true;
            }
         }
      }

      return false;
   }

   public void tick(Level pLevel, BlockPos pPos) {
      this.getType().tick(pLevel, pPos, this);
   }

   public void animateTick(Level pLevel, BlockPos pPos, RandomSource pRandom) {
      this.getType().animateTick(pLevel, pPos, this, pRandom);
   }

   public boolean isRandomlyTicking() {
      return this.getType().isRandomlyTicking();
   }

   public void randomTick(Level pLevel, BlockPos pPos, RandomSource pRandom) {
      this.getType().randomTick(pLevel, pPos, this, pRandom);
   }

   public Vec3 getFlow(BlockGetter pLevel, BlockPos pPos) {
      return this.getType().getFlow(pLevel, pPos, this);
   }

   public BlockState createLegacyBlock() {
      return this.getType().createLegacyBlock(this);
   }

   @Nullable
   public ParticleOptions getDripParticle() {
      return this.getType().getDripParticle();
   }

   public boolean is(TagKey<Fluid> pTag) {
      return this.getType().builtInRegistryHolder().is(pTag);
   }

   public boolean is(HolderSet<Fluid> pFluids) {
      return pFluids.contains(this.getType().builtInRegistryHolder());
   }

   public boolean is(Fluid pFluid) {
      return this.getType() == pFluid;
   }

   @Deprecated //Forge: Use more sensitive version
   public float getExplosionResistance() {
      return this.getType().getExplosionResistance();
   }

   public boolean canBeReplacedWith(BlockGetter pLevel, BlockPos pPos, Fluid pFluid, Direction pDirection) {
      return this.getType().canBeReplacedWith(this, pLevel, pPos, pFluid, pDirection);
   }

   public VoxelShape getShape(BlockGetter pLevel, BlockPos pPos) {
      return this.getType().getShape(this, pLevel, pPos);
   }

   public Holder<Fluid> holder() {
      return this.owner.builtInRegistryHolder();
   }

   public Stream<TagKey<Fluid>> getTags() {
      return this.owner.builtInRegistryHolder().tags();
   }
}
