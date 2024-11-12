package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public record ClientboundLoginPacket(int playerId, boolean hardcore, GameType gameType, @Nullable GameType previousGameType, Set<ResourceKey<Level>> levels, RegistryAccess.Frozen registryHolder, ResourceKey<DimensionType> dimensionType, ResourceKey<Level> dimension, long seed, int maxPlayers, int chunkRadius, int simulationDistance, boolean reducedDebugInfo, boolean showDeathScreen, boolean isDebug, boolean isFlat, Optional<GlobalPos> lastDeathLocation, int portalCooldown) implements Packet<ClientGamePacketListener> {
   private static final RegistryOps<Tag> BUILTIN_CONTEXT_OPS = RegistryOps.create(NbtOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));

   public ClientboundLoginPacket(FriendlyByteBuf pBuffer) {
      this(pBuffer.readInt(), pBuffer.readBoolean(), GameType.byId(pBuffer.readByte()), GameType.byNullableId(pBuffer.readByte()), pBuffer.readCollection(Sets::newHashSetWithExpectedSize, (p_258210_) -> {
         return p_258210_.readResourceKey(Registries.DIMENSION);
      }), pBuffer.readWithCodec(BUILTIN_CONTEXT_OPS, RegistrySynchronization.NETWORK_CODEC).freeze(), pBuffer.readResourceKey(Registries.DIMENSION_TYPE), pBuffer.readResourceKey(Registries.DIMENSION), pBuffer.readLong(), pBuffer.readVarInt(), pBuffer.readVarInt(), pBuffer.readVarInt(), pBuffer.readBoolean(), pBuffer.readBoolean(), pBuffer.readBoolean(), pBuffer.readBoolean(), pBuffer.readOptional(FriendlyByteBuf::readGlobalPos), pBuffer.readVarInt());
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeInt(this.playerId);
      pBuffer.writeBoolean(this.hardcore);
      pBuffer.writeByte(this.gameType.getId());
      pBuffer.writeByte(GameType.getNullableId(this.previousGameType));
      pBuffer.writeCollection(this.levels, FriendlyByteBuf::writeResourceKey);
      pBuffer.writeWithCodec(BUILTIN_CONTEXT_OPS, RegistrySynchronization.NETWORK_CODEC, this.registryHolder);
      pBuffer.writeResourceKey(this.dimensionType);
      pBuffer.writeResourceKey(this.dimension);
      pBuffer.writeLong(this.seed);
      pBuffer.writeVarInt(this.maxPlayers);
      pBuffer.writeVarInt(this.chunkRadius);
      pBuffer.writeVarInt(this.simulationDistance);
      pBuffer.writeBoolean(this.reducedDebugInfo);
      pBuffer.writeBoolean(this.showDeathScreen);
      pBuffer.writeBoolean(this.isDebug);
      pBuffer.writeBoolean(this.isFlat);
      pBuffer.writeOptional(this.lastDeathLocation, FriendlyByteBuf::writeGlobalPos);
      pBuffer.writeVarInt(this.portalCooldown);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleLogin(this);
   }
}