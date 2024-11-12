package net.minecraft.client.renderer.entity;

import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface EntityRendererProvider<T extends Entity> {
   EntityRenderer<T> create(EntityRendererProvider.Context pContext);

   @OnlyIn(Dist.CLIENT)
   public static class Context {
      private final EntityRenderDispatcher entityRenderDispatcher;
      private final ItemRenderer itemRenderer;
      private final BlockRenderDispatcher blockRenderDispatcher;
      private final ItemInHandRenderer itemInHandRenderer;
      private final ResourceManager resourceManager;
      private final EntityModelSet modelSet;
      private final Font font;

      public Context(EntityRenderDispatcher pEntityRenderDispatcher, ItemRenderer pItemRenderer, BlockRenderDispatcher pBlockRenderDispatcher, ItemInHandRenderer pItemInHandRenderer, ResourceManager pResourceManager, EntityModelSet pModelSet, Font pFont) {
         this.entityRenderDispatcher = pEntityRenderDispatcher;
         this.itemRenderer = pItemRenderer;
         this.blockRenderDispatcher = pBlockRenderDispatcher;
         this.itemInHandRenderer = pItemInHandRenderer;
         this.resourceManager = pResourceManager;
         this.modelSet = pModelSet;
         this.font = pFont;
      }

      public EntityRenderDispatcher getEntityRenderDispatcher() {
         return this.entityRenderDispatcher;
      }

      public ItemRenderer getItemRenderer() {
         return this.itemRenderer;
      }

      public BlockRenderDispatcher getBlockRenderDispatcher() {
         return this.blockRenderDispatcher;
      }

      public ItemInHandRenderer getItemInHandRenderer() {
         return this.itemInHandRenderer;
      }

      public ResourceManager getResourceManager() {
         return this.resourceManager;
      }

      public EntityModelSet getModelSet() {
         return this.modelSet;
      }

      public ModelManager getModelManager() {
         return this.blockRenderDispatcher.getBlockModelShaper().getModelManager();
      }

      public ModelPart bakeLayer(ModelLayerLocation pLayer) {
         return this.modelSet.bakeLayer(pLayer);
      }

      public Font getFont() {
         return this.font;
      }
   }
}