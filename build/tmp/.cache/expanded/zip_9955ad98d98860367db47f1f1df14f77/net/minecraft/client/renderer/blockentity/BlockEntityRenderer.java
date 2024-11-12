package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface BlockEntityRenderer<T extends BlockEntity> {
   void render(T pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay);

   default boolean shouldRenderOffScreen(T pBlockEntity) {
      return false;
   }

   default int getViewDistance() {
      return 64;
   }

   default boolean shouldRender(T pBlockEntity, Vec3 pCameraPos) {
      return Vec3.atCenterOf(pBlockEntity.getBlockPos()).closerThan(pCameraPos, (double)this.getViewDistance());
   }
}