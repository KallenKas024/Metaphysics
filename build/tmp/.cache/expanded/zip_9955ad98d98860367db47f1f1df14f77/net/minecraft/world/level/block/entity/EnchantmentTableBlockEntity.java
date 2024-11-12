package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EnchantmentTableBlockEntity extends BlockEntity implements Nameable {
   public int time;
   public float flip;
   public float oFlip;
   public float flipT;
   public float flipA;
   public float open;
   public float oOpen;
   public float rot;
   public float oRot;
   public float tRot;
   private static final RandomSource RANDOM = RandomSource.create();
   private Component name;

   public EnchantmentTableBlockEntity(BlockPos pPos, BlockState pBlockState) {
      super(BlockEntityType.ENCHANTING_TABLE, pPos, pBlockState);
   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      if (this.hasCustomName()) {
         pTag.putString("CustomName", Component.Serializer.toJson(this.name));
      }

   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      if (pTag.contains("CustomName", 8)) {
         this.name = Component.Serializer.fromJson(pTag.getString("CustomName"));
      }

   }

   public static void bookAnimationTick(Level pLevel, BlockPos pPos, BlockState pState, EnchantmentTableBlockEntity pBlockEntity) {
      pBlockEntity.oOpen = pBlockEntity.open;
      pBlockEntity.oRot = pBlockEntity.rot;
      Player player = pLevel.getNearestPlayer((double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, 3.0D, false);
      if (player != null) {
         double d0 = player.getX() - ((double)pPos.getX() + 0.5D);
         double d1 = player.getZ() - ((double)pPos.getZ() + 0.5D);
         pBlockEntity.tRot = (float)Mth.atan2(d1, d0);
         pBlockEntity.open += 0.1F;
         if (pBlockEntity.open < 0.5F || RANDOM.nextInt(40) == 0) {
            float f1 = pBlockEntity.flipT;

            do {
               pBlockEntity.flipT += (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
            } while(f1 == pBlockEntity.flipT);
         }
      } else {
         pBlockEntity.tRot += 0.02F;
         pBlockEntity.open -= 0.1F;
      }

      while(pBlockEntity.rot >= (float)Math.PI) {
         pBlockEntity.rot -= ((float)Math.PI * 2F);
      }

      while(pBlockEntity.rot < -(float)Math.PI) {
         pBlockEntity.rot += ((float)Math.PI * 2F);
      }

      while(pBlockEntity.tRot >= (float)Math.PI) {
         pBlockEntity.tRot -= ((float)Math.PI * 2F);
      }

      while(pBlockEntity.tRot < -(float)Math.PI) {
         pBlockEntity.tRot += ((float)Math.PI * 2F);
      }

      float f2;
      for(f2 = pBlockEntity.tRot - pBlockEntity.rot; f2 >= (float)Math.PI; f2 -= ((float)Math.PI * 2F)) {
      }

      while(f2 < -(float)Math.PI) {
         f2 += ((float)Math.PI * 2F);
      }

      pBlockEntity.rot += f2 * 0.4F;
      pBlockEntity.open = Mth.clamp(pBlockEntity.open, 0.0F, 1.0F);
      ++pBlockEntity.time;
      pBlockEntity.oFlip = pBlockEntity.flip;
      float f = (pBlockEntity.flipT - pBlockEntity.flip) * 0.4F;
      float f3 = 0.2F;
      f = Mth.clamp(f, -0.2F, 0.2F);
      pBlockEntity.flipA += (f - pBlockEntity.flipA) * 0.9F;
      pBlockEntity.flip += pBlockEntity.flipA;
   }

   public Component getName() {
      return (Component)(this.name != null ? this.name : Component.translatable("container.enchant"));
   }

   public void setCustomName(@Nullable Component pName) {
      this.name = pName;
   }

   @Nullable
   public Component getCustomName() {
      return this.name;
   }
}