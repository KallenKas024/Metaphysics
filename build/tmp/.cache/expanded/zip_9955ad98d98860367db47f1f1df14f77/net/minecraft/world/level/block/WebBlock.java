package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class WebBlock extends Block implements net.minecraftforge.common.IForgeShearable {
   public WebBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
      pEntity.makeStuckInBlock(pState, new Vec3(0.25D, (double)0.05F, 0.25D));
   }
}
