package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BrushItem extends Item {
   public static final int ANIMATION_DURATION = 10;
   private static final int USE_DURATION = 200;
   private static final double MAX_BRUSH_DISTANCE = Math.sqrt(ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE) - 1.0D;

   public BrushItem(Item.Properties pProperties) {
      super(pProperties);
   }

   /**
    * Called when this item is used when targeting a Block
    */
   public InteractionResult useOn(UseOnContext pContext) {
      Player player = pContext.getPlayer();
      if (player != null && this.calculateHitResult(player).getType() == HitResult.Type.BLOCK) {
         player.startUsingItem(pContext.getHand());
      }

      return InteractionResult.CONSUME;
   }

   /**
    * Returns the action that specifies what animation to play when the item is being used.
    */
   public UseAnim getUseAnimation(ItemStack pStack) {
      return UseAnim.BRUSH;
   }

   /**
    * How long it takes to use or consume an item
    */
   public int getUseDuration(ItemStack pStack) {
      return 200;
   }

   /**
    * Called as the item is being used by an entity.
    */
   public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
      if (pRemainingUseDuration >= 0 && pLivingEntity instanceof Player player) {
         HitResult hitresult = this.calculateHitResult(pLivingEntity);
         if (hitresult instanceof BlockHitResult blockhitresult) {
            if (hitresult.getType() == HitResult.Type.BLOCK) {
               int i = this.getUseDuration(pStack) - pRemainingUseDuration + 1;
               boolean flag = i % 10 == 5;
               if (flag) {
                  BlockPos blockpos = blockhitresult.getBlockPos();
                  BlockState blockstate = pLevel.getBlockState(blockpos);
                  HumanoidArm humanoidarm = pLivingEntity.getUsedItemHand() == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
                  this.spawnDustParticles(pLevel, blockhitresult, blockstate, pLivingEntity.getViewVector(0.0F), humanoidarm);
                  Block $$18 = blockstate.getBlock();
                  SoundEvent soundevent;
                  if ($$18 instanceof BrushableBlock) {
                     BrushableBlock brushableblock = (BrushableBlock)$$18;
                     soundevent = brushableblock.getBrushSound();
                  } else {
                     soundevent = SoundEvents.BRUSH_GENERIC;
                  }

                  pLevel.playSound(player, blockpos, soundevent, SoundSource.BLOCKS);
                  if (!pLevel.isClientSide()) {
                     BlockEntity blockentity = pLevel.getBlockEntity(blockpos);
                     if (blockentity instanceof BrushableBlockEntity) {
                        BrushableBlockEntity brushableblockentity = (BrushableBlockEntity)blockentity;
                        boolean flag1 = brushableblockentity.brush(pLevel.getGameTime(), player, blockhitresult.getDirection());
                        if (flag1) {
                           EquipmentSlot equipmentslot = pStack.equals(player.getItemBySlot(EquipmentSlot.OFFHAND)) ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
                           pStack.hurtAndBreak(1, pLivingEntity, (p_279044_) -> {
                              p_279044_.broadcastBreakEvent(equipmentslot);
                           });
                        }
                     }
                  }
               }

               return;
            }
         }

         pLivingEntity.releaseUsingItem();
      } else {
         pLivingEntity.releaseUsingItem();
      }
   }

   private HitResult calculateHitResult(LivingEntity pEntity) {
      return ProjectileUtil.getHitResultOnViewVector(pEntity, (p_281111_) -> {
         return !p_281111_.isSpectator() && p_281111_.isPickable();
      }, MAX_BRUSH_DISTANCE);
   }

   public void spawnDustParticles(Level pLevel, BlockHitResult pHitResult, BlockState pState, Vec3 pPos, HumanoidArm pArm) {
      double d0 = 3.0D;
      int i = pArm == HumanoidArm.RIGHT ? 1 : -1;
      int j = pLevel.getRandom().nextInt(7, 12);
      BlockParticleOption blockparticleoption = new BlockParticleOption(ParticleTypes.BLOCK, pState);
      Direction direction = pHitResult.getDirection();
      BrushItem.DustParticlesDelta brushitem$dustparticlesdelta = BrushItem.DustParticlesDelta.fromDirection(pPos, direction);
      Vec3 vec3 = pHitResult.getLocation();

      for(int k = 0; k < j; ++k) {
         pLevel.addParticle(blockparticleoption, vec3.x - (double)(direction == Direction.WEST ? 1.0E-6F : 0.0F), vec3.y, vec3.z - (double)(direction == Direction.NORTH ? 1.0E-6F : 0.0F), brushitem$dustparticlesdelta.xd() * (double)i * 3.0D * pLevel.getRandom().nextDouble(), 0.0D, brushitem$dustparticlesdelta.zd() * (double)i * 3.0D * pLevel.getRandom().nextDouble());
      }

   }

   static record DustParticlesDelta(double xd, double yd, double zd) {
      private static final double ALONG_SIDE_DELTA = 1.0D;
      private static final double OUT_FROM_SIDE_DELTA = 0.1D;

      public static BrushItem.DustParticlesDelta fromDirection(Vec3 pPos, Direction pDirection) {
         double d0 = 0.0D;
         BrushItem.DustParticlesDelta brushitem$dustparticlesdelta;
         switch (pDirection) {
            case DOWN:
            case UP:
               brushitem$dustparticlesdelta = new BrushItem.DustParticlesDelta(pPos.z(), 0.0D, -pPos.x());
               break;
            case NORTH:
               brushitem$dustparticlesdelta = new BrushItem.DustParticlesDelta(1.0D, 0.0D, -0.1D);
               break;
            case SOUTH:
               brushitem$dustparticlesdelta = new BrushItem.DustParticlesDelta(-1.0D, 0.0D, 0.1D);
               break;
            case WEST:
               brushitem$dustparticlesdelta = new BrushItem.DustParticlesDelta(-0.1D, 0.0D, -1.0D);
               break;
            case EAST:
               brushitem$dustparticlesdelta = new BrushItem.DustParticlesDelta(0.1D, 0.0D, 1.0D);
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return brushitem$dustparticlesdelta;
      }
   }
}