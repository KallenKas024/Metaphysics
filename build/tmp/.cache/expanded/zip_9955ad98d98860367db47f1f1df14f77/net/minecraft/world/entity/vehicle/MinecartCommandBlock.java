package net.minecraft.world.entity.vehicle;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MinecartCommandBlock extends AbstractMinecart {
   static final EntityDataAccessor<String> DATA_ID_COMMAND_NAME = SynchedEntityData.defineId(MinecartCommandBlock.class, EntityDataSerializers.STRING);
   static final EntityDataAccessor<Component> DATA_ID_LAST_OUTPUT = SynchedEntityData.defineId(MinecartCommandBlock.class, EntityDataSerializers.COMPONENT);
   private final BaseCommandBlock commandBlock = new MinecartCommandBlock.MinecartCommandBase();
   private static final int ACTIVATION_DELAY = 4;
   /** Cooldown before command block logic runs again in ticks */
   private int lastActivated;

   public MinecartCommandBlock(EntityType<? extends MinecartCommandBlock> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public MinecartCommandBlock(Level pLevel, double pX, double pY, double pZ) {
      super(EntityType.COMMAND_BLOCK_MINECART, pLevel, pX, pY, pZ);
   }

   protected Item getDropItem() {
      return Items.MINECART;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.getEntityData().define(DATA_ID_COMMAND_NAME, "");
      this.getEntityData().define(DATA_ID_LAST_OUTPUT, CommonComponents.EMPTY);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   protected void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.commandBlock.load(pCompound);
      this.getEntityData().set(DATA_ID_COMMAND_NAME, this.getCommandBlock().getCommand());
      this.getEntityData().set(DATA_ID_LAST_OUTPUT, this.getCommandBlock().getLastOutput());
   }

   protected void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      this.commandBlock.save(pCompound);
   }

   public AbstractMinecart.Type getMinecartType() {
      return AbstractMinecart.Type.COMMAND_BLOCK;
   }

   public BlockState getDefaultDisplayBlockState() {
      return Blocks.COMMAND_BLOCK.defaultBlockState();
   }

   public BaseCommandBlock getCommandBlock() {
      return this.commandBlock;
   }

   /**
    * Called every tick the minecart is on an activator rail.
    */
   public void activateMinecart(int pX, int pY, int pZ, boolean pReceivingPower) {
      if (pReceivingPower && this.tickCount - this.lastActivated >= 4) {
         this.getCommandBlock().performCommand(this.level());
         this.lastActivated = this.tickCount;
      }

   }

   public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
      InteractionResult ret = super.interact(pPlayer, pHand);
      if (ret.consumesAction()) return ret;
      return this.commandBlock.usedBy(pPlayer);
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
      super.onSyncedDataUpdated(pKey);
      if (DATA_ID_LAST_OUTPUT.equals(pKey)) {
         try {
            this.commandBlock.setLastOutput(this.getEntityData().get(DATA_ID_LAST_OUTPUT));
         } catch (Throwable throwable) {
         }
      } else if (DATA_ID_COMMAND_NAME.equals(pKey)) {
         this.commandBlock.setCommand(this.getEntityData().get(DATA_ID_COMMAND_NAME));
      }

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

   public class MinecartCommandBase extends BaseCommandBlock {
      public ServerLevel getLevel() {
         return (ServerLevel)MinecartCommandBlock.this.level();
      }

      public void onUpdated() {
         MinecartCommandBlock.this.getEntityData().set(MinecartCommandBlock.DATA_ID_COMMAND_NAME, this.getCommand());
         MinecartCommandBlock.this.getEntityData().set(MinecartCommandBlock.DATA_ID_LAST_OUTPUT, this.getLastOutput());
      }

      public Vec3 getPosition() {
         return MinecartCommandBlock.this.position();
      }

      public MinecartCommandBlock getMinecart() {
         return MinecartCommandBlock.this;
      }

      public CommandSourceStack createCommandSourceStack() {
         return new CommandSourceStack(this, MinecartCommandBlock.this.position(), MinecartCommandBlock.this.getRotationVector(), this.getLevel(), 2, this.getName().getString(), MinecartCommandBlock.this.getDisplayName(), this.getLevel().getServer(), MinecartCommandBlock.this);
      }

      public boolean isValid() {
         return !MinecartCommandBlock.this.isRemoved();
      }
   }
}
