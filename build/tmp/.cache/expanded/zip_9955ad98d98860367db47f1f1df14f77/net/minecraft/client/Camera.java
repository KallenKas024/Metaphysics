package net.minecraft.client;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class Camera {
   private boolean initialized;
   private BlockGetter level;
   private Entity entity;
   private Vec3 position = Vec3.ZERO;
   private final BlockPos.MutableBlockPos blockPosition = new BlockPos.MutableBlockPos();
   private final Vector3f forwards = new Vector3f(0.0F, 0.0F, 1.0F);
   private final Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
   private final Vector3f left = new Vector3f(1.0F, 0.0F, 0.0F);
   private float xRot;
   private float yRot;
   private final Quaternionf rotation = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
   private boolean detached;
   private float eyeHeight;
   private float eyeHeightOld;
   public static final float FOG_DISTANCE_SCALE = 0.083333336F;

   public void setup(BlockGetter pLevel, Entity pEntity, boolean pDetached, boolean pThirdPersonReverse, float pPartialTick) {
      this.initialized = true;
      this.level = pLevel;
      this.entity = pEntity;
      this.detached = pDetached;
      this.setRotation(pEntity.getViewYRot(pPartialTick), pEntity.getViewXRot(pPartialTick));
      this.setPosition(Mth.lerp((double)pPartialTick, pEntity.xo, pEntity.getX()), Mth.lerp((double)pPartialTick, pEntity.yo, pEntity.getY()) + (double)Mth.lerp(pPartialTick, this.eyeHeightOld, this.eyeHeight), Mth.lerp((double)pPartialTick, pEntity.zo, pEntity.getZ()));
      if (pDetached) {
         if (pThirdPersonReverse) {
            this.setRotation(this.yRot + 180.0F, -this.xRot);
         }

         this.move(-this.getMaxZoom(4.0D), 0.0D, 0.0D);
      } else if (pEntity instanceof LivingEntity && ((LivingEntity)pEntity).isSleeping()) {
         Direction direction = ((LivingEntity)pEntity).getBedOrientation();
         this.setRotation(direction != null ? direction.toYRot() - 180.0F : 0.0F, 0.0F);
         this.move(0.0D, 0.3D, 0.0D);
      }

   }

   public void tick() {
      if (this.entity != null) {
         this.eyeHeightOld = this.eyeHeight;
         this.eyeHeight += (this.entity.getEyeHeight() - this.eyeHeight) * 0.5F;
      }

   }

   /**
    * Checks for collision of the third person camera and returns the distance
    */
   private double getMaxZoom(double pStartingDistance) {
      for(int i = 0; i < 8; ++i) {
         float f = (float)((i & 1) * 2 - 1);
         float f1 = (float)((i >> 1 & 1) * 2 - 1);
         float f2 = (float)((i >> 2 & 1) * 2 - 1);
         f *= 0.1F;
         f1 *= 0.1F;
         f2 *= 0.1F;
         Vec3 vec3 = this.position.add((double)f, (double)f1, (double)f2);
         Vec3 vec31 = new Vec3(this.position.x - (double)this.forwards.x() * pStartingDistance + (double)f, this.position.y - (double)this.forwards.y() * pStartingDistance + (double)f1, this.position.z - (double)this.forwards.z() * pStartingDistance + (double)f2);
         HitResult hitresult = this.level.clip(new ClipContext(vec3, vec31, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, this.entity));
         if (hitresult.getType() != HitResult.Type.MISS) {
            double d0 = hitresult.getLocation().distanceTo(this.position);
            if (d0 < pStartingDistance) {
               pStartingDistance = d0;
            }
         }
      }

      return pStartingDistance;
   }

   /**
    * Moves the render position relative to the view direction, for third person camera
    */
   protected void move(double pDistanceOffset, double pVerticalOffset, double pHorizontalOffset) {
      double d0 = (double)this.forwards.x() * pDistanceOffset + (double)this.up.x() * pVerticalOffset + (double)this.left.x() * pHorizontalOffset;
      double d1 = (double)this.forwards.y() * pDistanceOffset + (double)this.up.y() * pVerticalOffset + (double)this.left.y() * pHorizontalOffset;
      double d2 = (double)this.forwards.z() * pDistanceOffset + (double)this.up.z() * pVerticalOffset + (double)this.left.z() * pHorizontalOffset;
      this.setPosition(new Vec3(this.position.x + d0, this.position.y + d1, this.position.z + d2));
   }

   protected void setRotation(float pYRot, float pXRot) {
      this.xRot = pXRot;
      this.yRot = pYRot;
      this.rotation.rotationYXZ(-pYRot * ((float)Math.PI / 180F), pXRot * ((float)Math.PI / 180F), 0.0F);
      this.forwards.set(0.0F, 0.0F, 1.0F).rotate(this.rotation);
      this.up.set(0.0F, 1.0F, 0.0F).rotate(this.rotation);
      this.left.set(1.0F, 0.0F, 0.0F).rotate(this.rotation);
   }

   /**
    * Sets the position and blockpos of the active render
    */
   protected void setPosition(double pX, double pY, double pZ) {
      this.setPosition(new Vec3(pX, pY, pZ));
   }

   protected void setPosition(Vec3 pPos) {
      this.position = pPos;
      this.blockPosition.set(pPos.x, pPos.y, pPos.z);
   }

   public Vec3 getPosition() {
      return this.position;
   }

   public BlockPos getBlockPosition() {
      return this.blockPosition;
   }

   public float getXRot() {
      return this.xRot;
   }

   public float getYRot() {
      return this.yRot;
   }

   public Quaternionf rotation() {
      return this.rotation;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public boolean isDetached() {
      return this.detached;
   }

   public Camera.NearPlane getNearPlane() {
      Minecraft minecraft = Minecraft.getInstance();
      double d0 = (double)minecraft.getWindow().getWidth() / (double)minecraft.getWindow().getHeight();
      double d1 = Math.tan((double)((float)minecraft.options.fov().get().intValue() * ((float)Math.PI / 180F)) / 2.0D) * (double)0.05F;
      double d2 = d1 * d0;
      Vec3 vec3 = (new Vec3(this.forwards)).scale((double)0.05F);
      Vec3 vec31 = (new Vec3(this.left)).scale(d2);
      Vec3 vec32 = (new Vec3(this.up)).scale(d1);
      return new Camera.NearPlane(vec3, vec31, vec32);
   }

   public FogType getFluidInCamera() {
      if (!this.initialized) {
         return FogType.NONE;
      } else {
         FluidState fluidstate = this.level.getFluidState(this.blockPosition);
         if (fluidstate.is(FluidTags.WATER) && this.position.y < (double)((float)this.blockPosition.getY() + fluidstate.getHeight(this.level, this.blockPosition))) {
            return FogType.WATER;
         } else {
            Camera.NearPlane camera$nearplane = this.getNearPlane();

            for(Vec3 vec3 : Arrays.asList(camera$nearplane.forward, camera$nearplane.getTopLeft(), camera$nearplane.getTopRight(), camera$nearplane.getBottomLeft(), camera$nearplane.getBottomRight())) {
               Vec3 vec31 = this.position.add(vec3);
               BlockPos blockpos = BlockPos.containing(vec31);
               FluidState fluidstate1 = this.level.getFluidState(blockpos);
               if (fluidstate1.is(FluidTags.LAVA)) {
                  if (vec31.y <= (double)(fluidstate1.getHeight(this.level, blockpos) + (float)blockpos.getY())) {
                     return FogType.LAVA;
                  }
               } else {
                  BlockState blockstate = this.level.getBlockState(blockpos);
                  if (blockstate.is(Blocks.POWDER_SNOW)) {
                     return FogType.POWDER_SNOW;
                  }
               }
            }

            return FogType.NONE;
         }
      }
   }

   public final Vector3f getLookVector() {
      return this.forwards;
   }

   public final Vector3f getUpVector() {
      return this.up;
   }

   public final Vector3f getLeftVector() {
      return this.left;
   }

   public void reset() {
      this.level = null;
      this.entity = null;
      this.initialized = false;
   }

   public void setAnglesInternal(float yaw, float pitch) {
      this.yRot = yaw;
      this.xRot = pitch;
   }

   public net.minecraft.world.level.block.state.BlockState getBlockAtCamera() {
      if (!this.initialized)
         return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
      else
         return this.level.getBlockState(this.blockPosition).getStateAtViewpoint(this.level, this.blockPosition, this.position);
   }

   @OnlyIn(Dist.CLIENT)
   public static class NearPlane {
      final Vec3 forward;
      private final Vec3 left;
      private final Vec3 up;

      NearPlane(Vec3 pForward, Vec3 pLeft, Vec3 pUp) {
         this.forward = pForward;
         this.left = pLeft;
         this.up = pUp;
      }

      public Vec3 getTopLeft() {
         return this.forward.add(this.up).add(this.left);
      }

      public Vec3 getTopRight() {
         return this.forward.add(this.up).subtract(this.left);
      }

      public Vec3 getBottomLeft() {
         return this.forward.subtract(this.up).add(this.left);
      }

      public Vec3 getBottomRight() {
         return this.forward.subtract(this.up).subtract(this.left);
      }

      public Vec3 getPointOnPlane(float pLeftScale, float pUpScale) {
         return this.forward.add(this.up.scale((double)pUpScale)).subtract(this.left.scale((double)pLeftScale));
      }
   }
}
