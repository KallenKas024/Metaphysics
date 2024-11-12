package net.minecraft.commands.arguments.blocks;

import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockInput implements Predicate<BlockInWorld> {
   private final BlockState state;
   private final Set<Property<?>> properties;
   @Nullable
   private final CompoundTag tag;

   public BlockInput(BlockState pState, Set<Property<?>> pProperties, @Nullable CompoundTag pTag) {
      this.state = pState;
      this.properties = pProperties;
      this.tag = pTag;
   }

   public BlockState getState() {
      return this.state;
   }

   public Set<Property<?>> getDefinedProperties() {
      return this.properties;
   }

   public boolean test(BlockInWorld pBlock) {
      BlockState blockstate = pBlock.getState();
      if (!blockstate.is(this.state.getBlock())) {
         return false;
      } else {
         for(Property<?> property : this.properties) {
            if (blockstate.getValue(property) != this.state.getValue(property)) {
               return false;
            }
         }

         if (this.tag == null) {
            return true;
         } else {
            BlockEntity blockentity = pBlock.getEntity();
            return blockentity != null && NbtUtils.compareNbt(this.tag, blockentity.saveWithFullMetadata(), true);
         }
      }
   }

   public boolean test(ServerLevel pLevel, BlockPos pPos) {
      return this.test(new BlockInWorld(pLevel, pPos, false));
   }

   public boolean place(ServerLevel pLevel, BlockPos pPos, int pFlags) {
      BlockState blockstate = Block.updateFromNeighbourShapes(this.state, pLevel, pPos);
      if (blockstate.isAir()) {
         blockstate = this.state;
      }

      if (!pLevel.setBlock(pPos, blockstate, pFlags)) {
         return false;
      } else {
         if (this.tag != null) {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);
            if (blockentity != null) {
               blockentity.load(this.tag);
            }
         }

         return true;
      }
   }
}