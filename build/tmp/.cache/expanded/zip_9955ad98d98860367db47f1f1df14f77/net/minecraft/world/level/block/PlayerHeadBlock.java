package net.minecraft.world.level.block;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class PlayerHeadBlock extends SkullBlock {
   public PlayerHeadBlock(BlockBehaviour.Properties p_55177_) {
      super(SkullBlock.Types.PLAYER, p_55177_);
   }

   /**
    * Called by BlockItem after this block has been placed.
    */
   public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
      super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      if (blockentity instanceof SkullBlockEntity skullblockentity) {
         GameProfile gameprofile = null;
         if (pStack.hasTag()) {
            CompoundTag compoundtag = pStack.getTag();
            if (compoundtag.contains("SkullOwner", 10)) {
               gameprofile = NbtUtils.readGameProfile(compoundtag.getCompound("SkullOwner"));
            } else if (compoundtag.contains("SkullOwner", 8) && !Util.isBlank(compoundtag.getString("SkullOwner"))) {
               gameprofile = new GameProfile((UUID)null, compoundtag.getString("SkullOwner"));
            }
         }

         skullblockentity.setOwner(gameprofile);
      }

   }
}