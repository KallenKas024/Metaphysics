package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class Particle {
   private static final AABB INITIAL_AABB = new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
   private static final double MAXIMUM_COLLISION_VELOCITY_SQUARED = Mth.square(100.0D);
   protected final ClientLevel level;
   protected double xo;
   protected double yo;
   protected double zo;
   protected double x;
   protected double y;
   protected double z;
   protected double xd;
   protected double yd;
   protected double zd;
   private AABB bb = INITIAL_AABB;
   protected boolean onGround;
   protected boolean hasPhysics = true;
   private boolean stoppedByCollision;
   protected boolean removed;
   protected float bbWidth = 0.6F;
   protected float bbHeight = 1.8F;
   protected final RandomSource random = RandomSource.create();
   protected int age;
   protected int lifetime;
   protected float gravity;
   protected float rCol = 1.0F;
   protected float gCol = 1.0F;
   protected float bCol = 1.0F;
   protected float alpha = 1.0F;
   protected float roll;
   protected float oRoll;
   protected float friction = 0.98F;
   protected boolean speedUpWhenYMotionIsBlocked = false;

   protected Particle(ClientLevel pLevel, double pX, double pY, double pZ) {
      this.level = pLevel;
      this.setSize(0.2F, 0.2F);
      this.setPos(pX, pY, pZ);
      this.xo = pX;
      this.yo = pY;
      this.zo = pZ;
      this.lifetime = (int)(4.0F / (this.random.nextFloat() * 0.9F + 0.1F));
   }

   public Particle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      this(pLevel, pX, pY, pZ);
      this.xd = pXSpeed + (Math.random() * 2.0D - 1.0D) * (double)0.4F;
      this.yd = pYSpeed + (Math.random() * 2.0D - 1.0D) * (double)0.4F;
      this.zd = pZSpeed + (Math.random() * 2.0D - 1.0D) * (double)0.4F;
      double d0 = (Math.random() + Math.random() + 1.0D) * (double)0.15F;
      double d1 = Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
      this.xd = this.xd / d1 * d0 * (double)0.4F;
      this.yd = this.yd / d1 * d0 * (double)0.4F + (double)0.1F;
      this.zd = this.zd / d1 * d0 * (double)0.4F;
   }

   public Particle setPower(float pMultiplier) {
      this.xd *= (double)pMultiplier;
      this.yd = (this.yd - (double)0.1F) * (double)pMultiplier + (double)0.1F;
      this.zd *= (double)pMultiplier;
      return this;
   }

   public void setParticleSpeed(double pXd, double pYd, double pZd) {
      this.xd = pXd;
      this.yd = pYd;
      this.zd = pZd;
   }

   public Particle scale(float pScale) {
      this.setSize(0.2F * pScale, 0.2F * pScale);
      return this;
   }

   public void setColor(float pParticleRed, float pParticleGreen, float pParticleBlue) {
      this.rCol = pParticleRed;
      this.gCol = pParticleGreen;
      this.bCol = pParticleBlue;
   }

   /**
    * Sets the particle alpha (float)
    */
   protected void setAlpha(float pAlpha) {
      this.alpha = pAlpha;
   }

   public void setLifetime(int pParticleLifeTime) {
      this.lifetime = pParticleLifeTime;
   }

   public int getLifetime() {
      return this.lifetime;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         this.yd -= 0.04D * (double)this.gravity;
         this.move(this.xd, this.yd, this.zd);
         if (this.speedUpWhenYMotionIsBlocked && this.y == this.yo) {
            this.xd *= 1.1D;
            this.zd *= 1.1D;
         }

         this.xd *= (double)this.friction;
         this.yd *= (double)this.friction;
         this.zd *= (double)this.friction;
         if (this.onGround) {
            this.xd *= (double)0.7F;
            this.zd *= (double)0.7F;
         }

      }
   }

   public abstract void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks);

   public abstract ParticleRenderType getRenderType();

   public String toString() {
      return this.getClass().getSimpleName() + ", Pos (" + this.x + "," + this.y + "," + this.z + "), RGBA (" + this.rCol + "," + this.gCol + "," + this.bCol + "," + this.alpha + "), Age " + this.age;
   }

   /**
    * Called to indicate that this particle effect has expired and should be discontinued.
    */
   public void remove() {
      this.removed = true;
   }

   protected void setSize(float pWidth, float pHeight) {
      if (pWidth != this.bbWidth || pHeight != this.bbHeight) {
         this.bbWidth = pWidth;
         this.bbHeight = pHeight;
         AABB aabb = this.getBoundingBox();
         double d0 = (aabb.minX + aabb.maxX - (double)pWidth) / 2.0D;
         double d1 = (aabb.minZ + aabb.maxZ - (double)pWidth) / 2.0D;
         this.setBoundingBox(new AABB(d0, aabb.minY, d1, d0 + (double)this.bbWidth, aabb.minY + (double)this.bbHeight, d1 + (double)this.bbWidth));
      }

   }

   public void setPos(double pX, double pY, double pZ) {
      this.x = pX;
      this.y = pY;
      this.z = pZ;
      float f = this.bbWidth / 2.0F;
      float f1 = this.bbHeight;
      this.setBoundingBox(new AABB(pX - (double)f, pY, pZ - (double)f, pX + (double)f, pY + (double)f1, pZ + (double)f));
   }

   public void move(double pX, double pY, double pZ) {
      if (!this.stoppedByCollision) {
         double d0 = pX;
         double d1 = pY;
         double d2 = pZ;
         if (this.hasPhysics && (pX != 0.0D || pY != 0.0D || pZ != 0.0D) && pX * pX + pY * pY + pZ * pZ < MAXIMUM_COLLISION_VELOCITY_SQUARED) {
            Vec3 vec3 = Entity.collideBoundingBox((Entity)null, new Vec3(pX, pY, pZ), this.getBoundingBox(), this.level, List.of());
            pX = vec3.x;
            pY = vec3.y;
            pZ = vec3.z;
         }

         if (pX != 0.0D || pY != 0.0D || pZ != 0.0D) {
            this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
            this.setLocationFromBoundingbox();
         }

         if (Math.abs(d1) >= (double)1.0E-5F && Math.abs(pY) < (double)1.0E-5F) {
            this.stoppedByCollision = true;
         }

         this.onGround = d1 != pY && d1 < 0.0D;
         if (d0 != pX) {
            this.xd = 0.0D;
         }

         if (d2 != pZ) {
            this.zd = 0.0D;
         }

      }
   }

   protected void setLocationFromBoundingbox() {
      AABB aabb = this.getBoundingBox();
      this.x = (aabb.minX + aabb.maxX) / 2.0D;
      this.y = aabb.minY;
      this.z = (aabb.minZ + aabb.maxZ) / 2.0D;
   }

   protected int getLightColor(float pPartialTick) {
      BlockPos blockpos = BlockPos.containing(this.x, this.y, this.z);
      return this.level.hasChunkAt(blockpos) ? LevelRenderer.getLightColor(this.level, blockpos) : 0;
   }

   /**
    * Returns {@code true} if this effect has not yet expired. "I feel happy! I feel happy!"
    */
   public boolean isAlive() {
      return !this.removed;
   }

   public AABB getBoundingBox() {
      return this.bb;
   }

   public void setBoundingBox(AABB pBb) {
      this.bb = pBb;
   }

   public Optional<ParticleGroup> getParticleGroup() {
      return Optional.empty();
   }

    /**
     * Forge added method that controls if a particle should be culled to it's bounding box.
     * Default behaviour is culling enabled
     */
    public boolean shouldCull() {
        return true;
    }

    public net.minecraft.world.phys.Vec3 getPos() {
       return new net.minecraft.world.phys.Vec3(this.x, this.y, this.z);
    }
}
