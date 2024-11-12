package net.minecraft.client.color.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface BlockColor {
   int getColor(BlockState pState, @Nullable BlockAndTintGetter pLevel, @Nullable BlockPos pPos, int pTintIndex);
}