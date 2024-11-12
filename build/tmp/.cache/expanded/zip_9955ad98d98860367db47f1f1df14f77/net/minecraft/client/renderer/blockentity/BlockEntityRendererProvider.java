package net.minecraft.client.renderer.blockentity;

import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface BlockEntityRendererProvider<T extends BlockEntity> {
   BlockEntityRenderer<T> create(BlockEntityRendererProvider.Context pContext);

   @OnlyIn(Dist.CLIENT)
   public static class Context {
      private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
      private final BlockRenderDispatcher blockRenderDispatcher;
      private final ItemRenderer itemRenderer;
      private final EntityRenderDispatcher entityRenderer;
      private final EntityModelSet modelSet;
      private final Font font;

      public Context(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, BlockRenderDispatcher pBlockRenderDispatcher, ItemRenderer pItemRenderer, EntityRenderDispatcher pEntityRenderer, EntityModelSet pModelSet, Font pFont) {
         this.blockEntityRenderDispatcher = pBlockEntityRenderDispatcher;
         this.blockRenderDispatcher = pBlockRenderDispatcher;
         this.itemRenderer = pItemRenderer;
         this.entityRenderer = pEntityRenderer;
         this.modelSet = pModelSet;
         this.font = pFont;
      }

      public BlockEntityRenderDispatcher getBlockEntityRenderDispatcher() {
         return this.blockEntityRenderDispatcher;
      }

      public BlockRenderDispatcher getBlockRenderDispatcher() {
         return this.blockRenderDispatcher;
      }

      public EntityRenderDispatcher getEntityRenderer() {
         return this.entityRenderer;
      }

      public ItemRenderer getItemRenderer() {
         return this.itemRenderer;
      }

      public EntityModelSet getModelSet() {
         return this.modelSet;
      }

      public ModelPart bakeLayer(ModelLayerLocation pLayerLocation) {
         return this.modelSet.bakeLayer(pLayerLocation);
      }

      public Font getFont() {
         return this.font;
      }
   }
}