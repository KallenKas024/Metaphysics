package net.minecraft.client.multiplayer.prediction;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockStatePredictionHandler implements AutoCloseable {
   private final Long2ObjectOpenHashMap<BlockStatePredictionHandler.ServerVerifiedState> serverVerifiedStates = new Long2ObjectOpenHashMap<>();
   private int currentSequenceNr;
   private boolean isPredicting;

   public void retainKnownServerState(BlockPos pPos, BlockState pState, LocalPlayer pPlayer) {
      this.serverVerifiedStates.compute(pPos.asLong(), (p_289242_, p_289243_) -> {
         return p_289243_ != null ? p_289243_.setSequence(this.currentSequenceNr) : new BlockStatePredictionHandler.ServerVerifiedState(this.currentSequenceNr, pState, pPlayer.position());
      });
   }

   public boolean updateKnownServerState(BlockPos pPos, BlockState pState) {
      BlockStatePredictionHandler.ServerVerifiedState blockstatepredictionhandler$serververifiedstate = this.serverVerifiedStates.get(pPos.asLong());
      if (blockstatepredictionhandler$serververifiedstate == null) {
         return false;
      } else {
         blockstatepredictionhandler$serververifiedstate.setBlockState(pState);
         return true;
      }
   }

   public void endPredictionsUpTo(int pSequence, ClientLevel pLevel) {
      ObjectIterator<Long2ObjectMap.Entry<BlockStatePredictionHandler.ServerVerifiedState>> objectiterator = this.serverVerifiedStates.long2ObjectEntrySet().iterator();

      while(objectiterator.hasNext()) {
         Long2ObjectMap.Entry<BlockStatePredictionHandler.ServerVerifiedState> entry = objectiterator.next();
         BlockStatePredictionHandler.ServerVerifiedState blockstatepredictionhandler$serververifiedstate = entry.getValue();
         if (blockstatepredictionhandler$serververifiedstate.sequence <= pSequence) {
            BlockPos blockpos = BlockPos.of(entry.getLongKey());
            objectiterator.remove();
            pLevel.syncBlockState(blockpos, blockstatepredictionhandler$serververifiedstate.blockState, blockstatepredictionhandler$serververifiedstate.playerPos);
         }
      }

   }

   public BlockStatePredictionHandler startPredicting() {
      ++this.currentSequenceNr;
      this.isPredicting = true;
      return this;
   }

   public void close() {
      this.isPredicting = false;
   }

   public int currentSequence() {
      return this.currentSequenceNr;
   }

   public boolean isPredicting() {
      return this.isPredicting;
   }

   @OnlyIn(Dist.CLIENT)
   static class ServerVerifiedState {
      final Vec3 playerPos;
      int sequence;
      BlockState blockState;

      ServerVerifiedState(int pSequence, BlockState pBlockState, Vec3 pPlayerPos) {
         this.sequence = pSequence;
         this.blockState = pBlockState;
         this.playerPos = pPlayerPos;
      }

      BlockStatePredictionHandler.ServerVerifiedState setSequence(int pSequence) {
         this.sequence = pSequence;
         return this;
      }

      void setBlockState(BlockState pBlockState) {
         this.blockState = pBlockState;
      }
   }
}