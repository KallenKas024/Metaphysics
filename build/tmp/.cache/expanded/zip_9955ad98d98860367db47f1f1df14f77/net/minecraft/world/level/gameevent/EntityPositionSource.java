package net.minecraft.world.level.gameevent;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityPositionSource implements PositionSource {
   public static final Codec<EntityPositionSource> CODEC = RecordCodecBuilder.create((p_253607_) -> {
      return p_253607_.group(UUIDUtil.CODEC.fieldOf("source_entity").forGetter(EntityPositionSource::getUuid), Codec.FLOAT.fieldOf("y_offset").orElse(0.0F).forGetter((p_223666_) -> {
         return p_223666_.yOffset;
      })).apply(p_253607_, (p_223672_, p_223673_) -> {
         return new EntityPositionSource(Either.right(Either.left(p_223672_)), p_223673_);
      });
   });
   private Either<Entity, Either<UUID, Integer>> entityOrUuidOrId;
   final float yOffset;

   public EntityPositionSource(Entity pEntity, float pYOffset) {
      this(Either.left(pEntity), pYOffset);
   }

   EntityPositionSource(Either<Entity, Either<UUID, Integer>> pEntityOrUuidOrId, float pYOffset) {
      this.entityOrUuidOrId = pEntityOrUuidOrId;
      this.yOffset = pYOffset;
   }

   public Optional<Vec3> getPosition(Level pLevel) {
      if (this.entityOrUuidOrId.left().isEmpty()) {
         this.resolveEntity(pLevel);
      }

      return this.entityOrUuidOrId.left().map((p_223676_) -> {
         return p_223676_.position().add(0.0D, (double)this.yOffset, 0.0D);
      });
   }

   private void resolveEntity(Level pLevel) {
      this.entityOrUuidOrId.map(Optional::of, (p_223657_) -> {
         return Optional.ofNullable(p_223657_.map((p_223660_) -> {
            Entity entity;
            if (pLevel instanceof ServerLevel serverlevel) {
               entity = serverlevel.getEntity(p_223660_);
            } else {
               entity = null;
            }

            return entity;
         }, pLevel::getEntity));
      }).ifPresent((p_223654_) -> {
         this.entityOrUuidOrId = Either.left(p_223654_);
      });
   }

   private UUID getUuid() {
      return this.entityOrUuidOrId.map(Entity::getUUID, (p_223680_) -> {
         return p_223680_.map(Function.identity(), (p_223668_) -> {
            throw new RuntimeException("Unable to get entityId from uuid");
         });
      });
   }

   int getId() {
      return this.entityOrUuidOrId.map(Entity::getId, (p_223662_) -> {
         return p_223662_.map((p_223670_) -> {
            throw new IllegalStateException("Unable to get entityId from uuid");
         }, Function.identity());
      });
   }

   public PositionSourceType<?> getType() {
      return PositionSourceType.ENTITY;
   }

   public static class Type implements PositionSourceType<EntityPositionSource> {
      /**
       * Reads a PositionSource from the byte buffer.
       * @return The PositionSource that was read.
       * @param pByteBuf The byte buffer to read from.
       */
      public EntityPositionSource read(FriendlyByteBuf p_157741_) {
         return new EntityPositionSource(Either.right(Either.right(p_157741_.readVarInt())), p_157741_.readFloat());
      }

      /**
       * Writes a PositionSource to a byte buffer.
       * @param pByteBuf The byte buffer to write to.
       * @param pSource The PositionSource to write.
       */
      public void write(FriendlyByteBuf p_157743_, EntityPositionSource p_157744_) {
         p_157743_.writeVarInt(p_157744_.getId());
         p_157743_.writeFloat(p_157744_.yOffset);
      }

      /**
       * Gets a codec that can handle the serialization of PositionSources of this type.
       * @return A codec that can serialize PositionSources of this type.
       */
      public Codec<EntityPositionSource> codec() {
         return EntityPositionSource.CODEC;
      }
   }
}