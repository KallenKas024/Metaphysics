package net.minecraft.server.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class DemoMode extends ServerPlayerGameMode {
   public static final int DEMO_DAYS = 5;
   public static final int TOTAL_PLAY_TICKS = 120500;
   private boolean displayedIntro;
   private boolean demoHasEnded;
   private int demoEndedReminder;
   private int gameModeTicks;

   public DemoMode(ServerPlayer pPlayer) {
      super(pPlayer);
   }

   public void tick() {
      super.tick();
      ++this.gameModeTicks;
      long i = this.level.getGameTime();
      long j = i / 24000L + 1L;
      if (!this.displayedIntro && this.gameModeTicks > 20) {
         this.displayedIntro = true;
         this.player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.DEMO_EVENT, 0.0F));
      }

      this.demoHasEnded = i > 120500L;
      if (this.demoHasEnded) {
         ++this.demoEndedReminder;
      }

      if (i % 24000L == 500L) {
         if (j <= 6L) {
            if (j == 6L) {
               this.player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.DEMO_EVENT, 104.0F));
            } else {
               this.player.sendSystemMessage(Component.translatable("demo.day." + j));
            }
         }
      } else if (j == 1L) {
         if (i == 100L) {
            this.player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.DEMO_EVENT, 101.0F));
         } else if (i == 175L) {
            this.player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.DEMO_EVENT, 102.0F));
         } else if (i == 250L) {
            this.player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.DEMO_EVENT, 103.0F));
         }
      } else if (j == 5L && i % 24000L == 22000L) {
         this.player.sendSystemMessage(Component.translatable("demo.day.warning"));
      }

   }

   /**
    * Sends a message to the player reminding them that this is the demo version
    */
   private void outputDemoReminder() {
      if (this.demoEndedReminder > 100) {
         this.player.sendSystemMessage(Component.translatable("demo.reminder"));
         this.demoEndedReminder = 0;
      }

   }

   public void handleBlockBreakAction(BlockPos pPos, ServerboundPlayerActionPacket.Action pAction, Direction pFace, int pMaxBuildHeight, int pSequence) {
      if (this.demoHasEnded) {
         this.outputDemoReminder();
      } else {
         super.handleBlockBreakAction(pPos, pAction, pFace, pMaxBuildHeight, pSequence);
      }
   }

   public InteractionResult useItem(ServerPlayer pPlayer, Level pLevel, ItemStack pStack, InteractionHand pHand) {
      if (this.demoHasEnded) {
         this.outputDemoReminder();
         return InteractionResult.PASS;
      } else {
         return super.useItem(pPlayer, pLevel, pStack, pHand);
      }
   }

   public InteractionResult useItemOn(ServerPlayer pPlayer, Level pLevel, ItemStack pStack, InteractionHand pHand, BlockHitResult pHitResult) {
      if (this.demoHasEnded) {
         this.outputDemoReminder();
         return InteractionResult.PASS;
      } else {
         return super.useItemOn(pPlayer, pLevel, pStack, pHand, pHitResult);
      }
   }
}