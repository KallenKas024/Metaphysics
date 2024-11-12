package net.minecraft.world.level.material;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMapper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class Fluid implements net.minecraftforge.common.extensions.IForgeFluid {
   public static final IdMapper<FluidState> FLUID_STATE_REGISTRY = new IdMapper<>();
   protected final StateDefinition<Fluid, FluidState> stateDefinition;
   private FluidState defaultFluidState;
   private final Holder.Reference<Fluid> builtInRegistryHolder = BuiltInRegistries.FLUID.createIntrusiveHolder(this);

   protected Fluid() {
      StateDefinition.Builder<Fluid, FluidState> builder = new StateDefinition.Builder<>(this);
      this.createFluidStateDefinition(builder);
      this.stateDefinition = builder.create(Fluid::defaultFluidState, FluidState::new);
      this.registerDefaultState(this.stateDefinition.any());
   }

   protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> pBuilder) {
   }

   public StateDefinition<Fluid, FluidState> getStateDefinition() {
      return this.stateDefinition;
   }

   protected final void registerDefaultState(FluidState pState) {
      this.defaultFluidState = pState;
   }

   public final FluidState defaultFluidState() {
      return this.defaultFluidState;
   }

   public abstract Item getBucket();

   protected void animateTick(Level pLevel, BlockPos pPos, FluidState pState, RandomSource pRandom) {
   }

   protected void tick(Level pLevel, BlockPos pPos, FluidState pState) {
   }

   protected void randomTick(Level pLevel, BlockPos pPos, FluidState pState, RandomSource pRandom) {
   }

   @Nullable
   protected ParticleOptions getDripParticle() {
      return null;
   }

   protected abstract boolean canBeReplacedWith(FluidState pState, BlockGetter pLevel, BlockPos pPos, Fluid pFluid, Direction pDirection);

   protected abstract Vec3 getFlow(BlockGetter pBlockReader, BlockPos pPos, FluidState pFluidState);

   public abstract int getTickDelay(LevelReader pLevel);

   protected boolean isRandomlyTicking() {
      return false;
   }

   protected boolean isEmpty() {
      return false;
   }

   protected abstract float getExplosionResistance();

   public abstract float getHeight(FluidState pState, BlockGetter pLevel, BlockPos pPos);

   public abstract float getOwnHeight(FluidState pState);

   protected abstract BlockState createLegacyBlock(FluidState pState);

   public abstract boolean isSource(FluidState pState);

   public abstract int getAmount(FluidState pState);

   public boolean isSame(Fluid pFluid) {
      return pFluid == this;
   }

   /** @deprecated */
   @Deprecated
   public boolean is(TagKey<Fluid> pTag) {
      return this.builtInRegistryHolder.is(pTag);
   }

   public abstract VoxelShape getShape(FluidState pState, BlockGetter pLevel, BlockPos pPos);

   private net.minecraftforge.fluids.FluidType forgeFluidType;
   @Override
   public net.minecraftforge.fluids.FluidType getFluidType() {
      if (forgeFluidType == null) forgeFluidType = net.minecraftforge.common.ForgeHooks.getVanillaFluidType(this);
      return forgeFluidType;
   }

   public Optional<SoundEvent> getPickupSound() {
      return Optional.empty();
   }

   /** @deprecated */
   @Deprecated
   public Holder.Reference<Fluid> builtInRegistryHolder() {
      return this.builtInRegistryHolder;
   }
}
