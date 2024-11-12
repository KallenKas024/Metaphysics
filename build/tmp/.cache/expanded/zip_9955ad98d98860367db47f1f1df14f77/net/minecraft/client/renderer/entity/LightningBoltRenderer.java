package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LightningBolt;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class LightningBoltRenderer extends EntityRenderer<LightningBolt> {
   public LightningBoltRenderer(EntityRendererProvider.Context pContext) {
      super(pContext);
   }

   public void render(LightningBolt pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      float[] afloat = new float[8];
      float[] afloat1 = new float[8];
      float f = 0.0F;
      float f1 = 0.0F;
      RandomSource randomsource = RandomSource.create(pEntity.seed);

      for(int i = 7; i >= 0; --i) {
         afloat[i] = f;
         afloat1[i] = f1;
         f += (float)(randomsource.nextInt(11) - 5);
         f1 += (float)(randomsource.nextInt(11) - 5);
      }

      VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.lightning());
      Matrix4f matrix4f = pPoseStack.last().pose();

      for(int j = 0; j < 4; ++j) {
         RandomSource randomsource1 = RandomSource.create(pEntity.seed);

         for(int k = 0; k < 3; ++k) {
            int l = 7;
            int i1 = 0;
            if (k > 0) {
               l = 7 - k;
            }

            if (k > 0) {
               i1 = l - 2;
            }

            float f2 = afloat[l] - f;
            float f3 = afloat1[l] - f1;

            for(int j1 = l; j1 >= i1; --j1) {
               float f4 = f2;
               float f5 = f3;
               if (k == 0) {
                  f2 += (float)(randomsource1.nextInt(11) - 5);
                  f3 += (float)(randomsource1.nextInt(11) - 5);
               } else {
                  f2 += (float)(randomsource1.nextInt(31) - 15);
                  f3 += (float)(randomsource1.nextInt(31) - 15);
               }

               float f6 = 0.5F;
               float f7 = 0.45F;
               float f8 = 0.45F;
               float f9 = 0.5F;
               float f10 = 0.1F + (float)j * 0.2F;
               if (k == 0) {
                  f10 *= (float)j1 * 0.1F + 1.0F;
               }

               float f11 = 0.1F + (float)j * 0.2F;
               if (k == 0) {
                  f11 *= ((float)j1 - 1.0F) * 0.1F + 1.0F;
               }

               quad(matrix4f, vertexconsumer, f2, f3, j1, f4, f5, 0.45F, 0.45F, 0.5F, f10, f11, false, false, true, false);
               quad(matrix4f, vertexconsumer, f2, f3, j1, f4, f5, 0.45F, 0.45F, 0.5F, f10, f11, true, false, true, true);
               quad(matrix4f, vertexconsumer, f2, f3, j1, f4, f5, 0.45F, 0.45F, 0.5F, f10, f11, true, true, false, true);
               quad(matrix4f, vertexconsumer, f2, f3, j1, f4, f5, 0.45F, 0.45F, 0.5F, f10, f11, false, true, false, false);
            }
         }
      }

   }

   private static void quad(Matrix4f pMatrix, VertexConsumer pConsumer, float pX1, float pZ1, int pIndex, float pX2, float pZ2, float pRed, float pGreen, float pBlue, float p_115283_, float p_115284_, boolean p_115285_, boolean p_115286_, boolean p_115287_, boolean p_115288_) {
      pConsumer.vertex(pMatrix, pX1 + (p_115285_ ? p_115284_ : -p_115284_), (float)(pIndex * 16), pZ1 + (p_115286_ ? p_115284_ : -p_115284_)).color(pRed, pGreen, pBlue, 0.3F).endVertex();
      pConsumer.vertex(pMatrix, pX2 + (p_115285_ ? p_115283_ : -p_115283_), (float)((pIndex + 1) * 16), pZ2 + (p_115286_ ? p_115283_ : -p_115283_)).color(pRed, pGreen, pBlue, 0.3F).endVertex();
      pConsumer.vertex(pMatrix, pX2 + (p_115287_ ? p_115283_ : -p_115283_), (float)((pIndex + 1) * 16), pZ2 + (p_115288_ ? p_115283_ : -p_115283_)).color(pRed, pGreen, pBlue, 0.3F).endVertex();
      pConsumer.vertex(pMatrix, pX1 + (p_115287_ ? p_115284_ : -p_115284_), (float)(pIndex * 16), pZ1 + (p_115288_ ? p_115284_ : -p_115284_)).color(pRed, pGreen, pBlue, 0.3F).endVertex();
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(LightningBolt pEntity) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}