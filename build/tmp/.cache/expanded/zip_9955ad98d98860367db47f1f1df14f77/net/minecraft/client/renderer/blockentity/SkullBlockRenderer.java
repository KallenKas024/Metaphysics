package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PiglinHeadModel;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.dragon.DragonHeadModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkullBlockRenderer implements BlockEntityRenderer<SkullBlockEntity> {
   private final Map<SkullBlock.Type, SkullModelBase> modelByType;
   public static final Map<SkullBlock.Type, ResourceLocation> SKIN_BY_TYPE = Util.make(Maps.newHashMap(), (p_261388_) -> {
      p_261388_.put(SkullBlock.Types.SKELETON, new ResourceLocation("textures/entity/skeleton/skeleton.png"));
      p_261388_.put(SkullBlock.Types.WITHER_SKELETON, new ResourceLocation("textures/entity/skeleton/wither_skeleton.png"));
      p_261388_.put(SkullBlock.Types.ZOMBIE, new ResourceLocation("textures/entity/zombie/zombie.png"));
      p_261388_.put(SkullBlock.Types.CREEPER, new ResourceLocation("textures/entity/creeper/creeper.png"));
      p_261388_.put(SkullBlock.Types.DRAGON, new ResourceLocation("textures/entity/enderdragon/dragon.png"));
      p_261388_.put(SkullBlock.Types.PIGLIN, new ResourceLocation("textures/entity/piglin/piglin.png"));
      p_261388_.put(SkullBlock.Types.PLAYER, DefaultPlayerSkin.getDefaultSkin());
   });

   public static Map<SkullBlock.Type, SkullModelBase> createSkullRenderers(EntityModelSet pEntityModelSet) {
      ImmutableMap.Builder<SkullBlock.Type, SkullModelBase> builder = ImmutableMap.builder();
      builder.put(SkullBlock.Types.SKELETON, new SkullModel(pEntityModelSet.bakeLayer(ModelLayers.SKELETON_SKULL)));
      builder.put(SkullBlock.Types.WITHER_SKELETON, new SkullModel(pEntityModelSet.bakeLayer(ModelLayers.WITHER_SKELETON_SKULL)));
      builder.put(SkullBlock.Types.PLAYER, new SkullModel(pEntityModelSet.bakeLayer(ModelLayers.PLAYER_HEAD)));
      builder.put(SkullBlock.Types.ZOMBIE, new SkullModel(pEntityModelSet.bakeLayer(ModelLayers.ZOMBIE_HEAD)));
      builder.put(SkullBlock.Types.CREEPER, new SkullModel(pEntityModelSet.bakeLayer(ModelLayers.CREEPER_HEAD)));
      builder.put(SkullBlock.Types.DRAGON, new DragonHeadModel(pEntityModelSet.bakeLayer(ModelLayers.DRAGON_SKULL)));
      builder.put(SkullBlock.Types.PIGLIN, new PiglinHeadModel(pEntityModelSet.bakeLayer(ModelLayers.PIGLIN_HEAD)));
      net.minecraftforge.fml.ModLoader.get().postEvent(new net.minecraftforge.client.event.EntityRenderersEvent.CreateSkullModels(builder, pEntityModelSet));
      return builder.build();
   }

   public SkullBlockRenderer(BlockEntityRendererProvider.Context pContext) {
      this.modelByType = createSkullRenderers(pContext.getModelSet());
   }

   public void render(SkullBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
      float f = pBlockEntity.getAnimation(pPartialTick);
      BlockState blockstate = pBlockEntity.getBlockState();
      boolean flag = blockstate.getBlock() instanceof WallSkullBlock;
      Direction direction = flag ? blockstate.getValue(WallSkullBlock.FACING) : null;
      int i = flag ? RotationSegment.convertToSegment(direction.getOpposite()) : blockstate.getValue(SkullBlock.ROTATION);
      float f1 = RotationSegment.convertToDegrees(i);
      SkullBlock.Type skullblock$type = ((AbstractSkullBlock)blockstate.getBlock()).getType();
      SkullModelBase skullmodelbase = this.modelByType.get(skullblock$type);
      RenderType rendertype = getRenderType(skullblock$type, pBlockEntity.getOwnerProfile());
      renderSkull(direction, f1, f, pPoseStack, pBuffer, pPackedLight, skullmodelbase, rendertype);
   }

   public static void renderSkull(@Nullable Direction pDirection, float pYRot, float pMouthAnimation, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, SkullModelBase pModel, RenderType pRenderType) {
      pPoseStack.pushPose();
      if (pDirection == null) {
         pPoseStack.translate(0.5F, 0.0F, 0.5F);
      } else {
         float f = 0.25F;
         pPoseStack.translate(0.5F - (float)pDirection.getStepX() * 0.25F, 0.25F, 0.5F - (float)pDirection.getStepZ() * 0.25F);
      }

      pPoseStack.scale(-1.0F, -1.0F, 1.0F);
      VertexConsumer vertexconsumer = pBufferSource.getBuffer(pRenderType);
      pModel.setupAnim(pMouthAnimation, pYRot, 0.0F);
      pModel.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      pPoseStack.popPose();
   }

   public static RenderType getRenderType(SkullBlock.Type pSkullType, @Nullable GameProfile pGameProfile) {
      ResourceLocation resourcelocation = SKIN_BY_TYPE.get(pSkullType);
      if (pSkullType == SkullBlock.Types.PLAYER && pGameProfile != null) {
         Minecraft minecraft = Minecraft.getInstance();
         Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().getInsecureSkinInformation(pGameProfile);
         return map.containsKey(Type.SKIN) ? RenderType.entityTranslucent(minecraft.getSkinManager().registerTexture(map.get(Type.SKIN), Type.SKIN)) : RenderType.entityCutoutNoCull(DefaultPlayerSkin.getDefaultSkin(UUIDUtil.getOrCreatePlayerUUID(pGameProfile)));
      } else {
         return RenderType.entityCutoutNoCullZOffset(resourcelocation);
      }
   }
}
