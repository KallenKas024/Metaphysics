package net.minecraft.network.protocol.game;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ClientboundBlockEntityDataPacket implements Packet<ClientGamePacketListener> {
   private final BlockPos pos;
   /** Used only for vanilla block entities */
   private final BlockEntityType<?> type;
   @Nullable
   private final CompoundTag tag;

   public static ClientboundBlockEntityDataPacket create(BlockEntity pBlockEntity, Function<BlockEntity, CompoundTag> pTagGetter) {
      return new ClientboundBlockEntityDataPacket(pBlockEntity.getBlockPos(), pBlockEntity.getType(), pTagGetter.apply(pBlockEntity));
   }

   public static ClientboundBlockEntityDataPacket create(BlockEntity pBlockEntity) {
      return create(pBlockEntity, BlockEntity::getUpdateTag);
   }

   private ClientboundBlockEntityDataPacket(BlockPos pPos, BlockEntityType<?> pType, CompoundTag pTag) {
      this.pos = pPos;
      this.type = pType;
      this.tag = pTag.isEmpty() ? null : pTag;
   }

   public ClientboundBlockEntityDataPacket(FriendlyByteBuf pBuffer) {
      this.pos = pBuffer.readBlockPos();
      this.type = pBuffer.readById(BuiltInRegistries.BLOCK_ENTITY_TYPE);
      this.tag = pBuffer.readNbt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeId(BuiltInRegistries.BLOCK_ENTITY_TYPE, this.type);
      pBuffer.writeNbt(this.tag);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleBlockEntityData(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public BlockEntityType<?> getType() {
      return this.type;
   }

   @Nullable
   public CompoundTag getTag() {
      return this.tag;
   }
}