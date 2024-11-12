package net.minecraft.client.renderer.blockentity;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BrightnessCombiner<S extends BlockEntity> implements DoubleBlockCombiner.Combiner<S, Int2IntFunction> {
   public Int2IntFunction acceptDouble(S pFirst, S pSecond) {
      return (p_112325_) -> {
         int i = LevelRenderer.getLightColor(pFirst.getLevel(), pFirst.getBlockPos());
         int j = LevelRenderer.getLightColor(pSecond.getLevel(), pSecond.getBlockPos());
         int k = LightTexture.block(i);
         int l = LightTexture.block(j);
         int i1 = LightTexture.sky(i);
         int j1 = LightTexture.sky(j);
         return LightTexture.pack(Math.max(k, l), Math.max(i1, j1));
      };
   }

   public Int2IntFunction acceptSingle(S pSingle) {
      return (p_112333_) -> {
         return p_112333_;
      };
   }

   public Int2IntFunction acceptNone() {
      return (p_112316_) -> {
         return p_112316_;
      };
   }
}