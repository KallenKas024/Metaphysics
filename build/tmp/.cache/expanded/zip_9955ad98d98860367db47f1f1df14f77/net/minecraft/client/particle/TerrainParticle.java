package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TerrainParticle extends TextureSheetParticle {
   private final BlockPos pos;
   private final float uo;
   private final float vo;

   public TerrainParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, BlockState pState) {
      this(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, pState, BlockPos.containing(pX, pY, pZ));
   }

   public TerrainParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, BlockState pState, BlockPos pPos) {
      super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
      this.pos = pPos;
      this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(pState));
      this.gravity = 1.0F;
      this.rCol = 0.6F;
      this.gCol = 0.6F;
      this.bCol = 0.6F;
      if (net.minecraftforge.client.extensions.common.IClientBlockExtensions.of(pState).areBreakingParticlesTinted(pState, pLevel, pPos)) {
         int i = Minecraft.getInstance().getBlockColors().getColor(pState, pLevel, pPos, 0);
         this.rCol *= (float)(i >> 16 & 255) / 255.0F;
         this.gCol *= (float)(i >> 8 & 255) / 255.0F;
         this.bCol *= (float)(i & 255) / 255.0F;
      }

      this.quadSize /= 2.0F;
      this.uo = this.random.nextFloat() * 3.0F;
      this.vo = this.random.nextFloat() * 3.0F;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.TERRAIN_SHEET;
   }

   protected float getU0() {
      return this.sprite.getU((double)((this.uo + 1.0F) / 4.0F * 16.0F));
   }

   protected float getU1() {
      return this.sprite.getU((double)(this.uo / 4.0F * 16.0F));
   }

   protected float getV0() {
      return this.sprite.getV((double)(this.vo / 4.0F * 16.0F));
   }

   protected float getV1() {
      return this.sprite.getV((double)((this.vo + 1.0F) / 4.0F * 16.0F));
   }

   public int getLightColor(float pPartialTick) {
      int i = super.getLightColor(pPartialTick);
      return i == 0 && this.level.hasChunkAt(this.pos) ? LevelRenderer.getLightColor(this.level, this.pos) : i;
   }

   @OnlyIn(Dist.CLIENT)
   public static class Provider implements ParticleProvider<BlockParticleOption> {
      public Particle createParticle(BlockParticleOption pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         BlockState blockstate = pType.getState();
         return !blockstate.isAir() && !blockstate.is(Blocks.MOVING_PISTON) ? (new TerrainParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, blockstate)).updateSprite(blockstate, pType.getPos()) : null;
      }
   }

   public Particle updateSprite(BlockState state, BlockPos pos) { //FORGE: we cannot assume that the x y z of the particles match the block pos of the block.
      if (pos != null) // There are cases where we are not able to obtain the correct source pos, and need to fallback to the non-model data version
         this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getTexture(state, level, pos));
      return this;
   }
}
