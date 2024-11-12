package net.minecraft.world.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MinecartSpawner extends AbstractMinecart {
   private final BaseSpawner spawner = new BaseSpawner() {
      public void broadcastEvent(Level p_150342_, BlockPos p_150343_, int p_150344_) {
         p_150342_.broadcastEntityEvent(MinecartSpawner.this, (byte)p_150344_);
      }

      @Override
      @org.jetbrains.annotations.Nullable
      public net.minecraft.world.entity.Entity getSpawnerEntity() {
         return MinecartSpawner.this;
      }
   };
   private final Runnable ticker;

   public MinecartSpawner(EntityType<? extends MinecartSpawner> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.ticker = this.createTicker(pLevel);
   }

   public MinecartSpawner(Level pLevel, double pX, double pY, double pZ) {
      super(EntityType.SPAWNER_MINECART, pLevel, pX, pY, pZ);
      this.ticker = this.createTicker(pLevel);
   }

   protected Item getDropItem() {
      return Items.MINECART;
   }

   private Runnable createTicker(Level pLevel) {
      return pLevel instanceof ServerLevel ? () -> {
         this.spawner.serverTick((ServerLevel)pLevel, this.blockPosition());
      } : () -> {
         this.spawner.clientTick(pLevel, this.blockPosition());
      };
   }

   public AbstractMinecart.Type getMinecartType() {
      return AbstractMinecart.Type.SPAWNER;
   }

   public BlockState getDefaultDisplayBlockState() {
      return Blocks.SPAWNER.defaultBlockState();
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   protected void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.spawner.load(this.level(), this.blockPosition(), pCompound);
   }

   protected void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      this.spawner.save(pCompound);
   }

   /**
    * Handles an entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
    */
   public void handleEntityEvent(byte pId) {
      this.spawner.onEventTriggered(this.level(), pId);
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      this.ticker.run();
   }

   public BaseSpawner getSpawner() {
      return this.spawner;
   }

   /**
    * Checks if players can use this entity to access operator (permission level 2) commands either directly or
    * indirectly, such as give or setblock. A similar method exists for entities at {@link
    * net.minecraft.world.entity.Entity#onlyOpCanSetNbt()}.<p>For example, {@link
    * net.minecraft.world.entity.vehicle.MinecartCommandBlock#onlyOpCanSetNbt() command block minecarts} and {@link
    * net.minecraft.world.entity.vehicle.MinecartSpawner#onlyOpCanSetNbt() mob spawner minecarts} (spawning command
    * block minecarts or drops) are considered accessible.</p>@return true if this entity offers ways for unauthorized
    * players to use restricted commands
    */
   public boolean onlyOpCanSetNbt() {
      return true;
   }
}
