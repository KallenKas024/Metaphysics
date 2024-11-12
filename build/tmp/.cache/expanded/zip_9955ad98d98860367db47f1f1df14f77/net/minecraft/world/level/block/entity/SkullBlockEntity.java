package net.minecraft.world.level.block.entity;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Services;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SkullBlockEntity extends BlockEntity {
   public static final String TAG_SKULL_OWNER = "SkullOwner";
   public static final String TAG_NOTE_BLOCK_SOUND = "note_block_sound";
   @Nullable
   private static GameProfileCache profileCache;
   @Nullable
   private static MinecraftSessionService sessionService;
   @Nullable
   private static Executor mainThreadExecutor;
   @Nullable
   private GameProfile owner;
   @Nullable
   private ResourceLocation noteBlockSound;
   private int animationTickCount;
   private boolean isAnimating;

   public SkullBlockEntity(BlockPos pPos, BlockState pBlockState) {
      super(BlockEntityType.SKULL, pPos, pBlockState);
   }

   public static void setup(Services pServices, Executor pMainThreadExecutor) {
      profileCache = pServices.profileCache();
      sessionService = pServices.sessionService();
      mainThreadExecutor = pMainThreadExecutor;
   }

   public static void clear() {
      profileCache = null;
      sessionService = null;
      mainThreadExecutor = null;
   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      if (this.owner != null) {
         CompoundTag compoundtag = new CompoundTag();
         NbtUtils.writeGameProfile(compoundtag, this.owner);
         pTag.put("SkullOwner", compoundtag);
      }

      if (this.noteBlockSound != null) {
         pTag.putString("note_block_sound", this.noteBlockSound.toString());
      }

   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      if (pTag.contains("SkullOwner", 10)) {
         this.setOwner(NbtUtils.readGameProfile(pTag.getCompound("SkullOwner")));
      } else if (pTag.contains("ExtraType", 8)) {
         String s = pTag.getString("ExtraType");
         if (!StringUtil.isNullOrEmpty(s)) {
            this.setOwner(new GameProfile((UUID)null, s));
         }
      }

      if (pTag.contains("note_block_sound", 8)) {
         this.noteBlockSound = ResourceLocation.tryParse(pTag.getString("note_block_sound"));
      }

   }

   public static void animation(Level pLevel, BlockPos pPos, BlockState pState, SkullBlockEntity pBlockEntity) {
      if (pLevel.hasNeighborSignal(pPos)) {
         pBlockEntity.isAnimating = true;
         ++pBlockEntity.animationTickCount;
      } else {
         pBlockEntity.isAnimating = false;
      }

   }

   public float getAnimation(float pPartialTick) {
      return this.isAnimating ? (float)this.animationTickCount + pPartialTick : (float)this.animationTickCount;
   }

   @Nullable
   public GameProfile getOwnerProfile() {
      return this.owner;
   }

   @Nullable
   public ResourceLocation getNoteBlockSound() {
      return this.noteBlockSound;
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   /**
    * Get an NBT compound to sync to the client with SPacketChunkData, used for initial loading of the chunk or when
    * many blocks change at once. This compound comes back to you clientside in {@link handleUpdateTag}
    */
   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
   }

   public void setOwner(@Nullable GameProfile pOwner) {
      synchronized(this) {
         this.owner = pOwner;
      }

      this.updateOwnerProfile();
   }

   private void updateOwnerProfile() {
      updateGameprofile(this.owner, (p_155747_) -> {
         this.owner = p_155747_;
         this.setChanged();
      });
   }

   public static void updateGameprofile(@Nullable GameProfile pProfile, Consumer<GameProfile> pProfileConsumer) {
      if (pProfile != null && !StringUtil.isNullOrEmpty(pProfile.getName()) && (!pProfile.isComplete() || !pProfile.getProperties().containsKey("textures")) && profileCache != null && sessionService != null) {
         profileCache.getAsync(pProfile.getName(), (p_182470_) -> {
            Util.backgroundExecutor().execute(() -> {
               Util.ifElse(p_182470_, (p_276255_) -> {
                  Property property = Iterables.getFirst(p_276255_.getProperties().get("textures"), (Property)null);
                  if (property == null) {
                     MinecraftSessionService minecraftsessionservice = sessionService;
                     if (minecraftsessionservice == null) {
                        return;
                     }

                     p_276255_ = minecraftsessionservice.fillProfileProperties(p_276255_, true);
                  }

                  GameProfile gameprofile = p_276255_;
                  Executor executor = mainThreadExecutor;
                  if (executor != null) {
                     executor.execute(() -> {
                        GameProfileCache gameprofilecache = profileCache;
                        if (gameprofilecache != null) {
                           gameprofilecache.add(gameprofile);
                           pProfileConsumer.accept(gameprofile);
                        }

                     });
                  }

               }, () -> {
                  Executor executor = mainThreadExecutor;
                  if (executor != null) {
                     executor.execute(() -> {
                        pProfileConsumer.accept(pProfile);
                     });
                  }

               });
            });
         });
      } else {
         pProfileConsumer.accept(pProfile);
      }
   }
}