package net.minecraft.world.level.saveddata.maps;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

public class MapItemSavedData extends SavedData {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAP_SIZE = 128;
   private static final int HALF_MAP_SIZE = 64;
   public static final int MAX_SCALE = 4;
   public static final int TRACKED_DECORATION_LIMIT = 256;
   public final int centerX;
   public final int centerZ;
   public final ResourceKey<Level> dimension;
   private final boolean trackingPosition;
   private final boolean unlimitedTracking;
   public final byte scale;
   public byte[] colors = new byte[16384];
   public final boolean locked;
   private final List<MapItemSavedData.HoldingPlayer> carriedBy = Lists.newArrayList();
   private final Map<Player, MapItemSavedData.HoldingPlayer> carriedByPlayers = Maps.newHashMap();
   private final Map<String, MapBanner> bannerMarkers = Maps.newHashMap();
   final Map<String, MapDecoration> decorations = Maps.newLinkedHashMap();
   private final Map<String, MapFrame> frameMarkers = Maps.newHashMap();
   private int trackedDecorationCount;

   private MapItemSavedData(int pX, int pZ, byte pScale, boolean pTrackingPosition, boolean pUnlimitedTracking, boolean pLocked, ResourceKey<Level> pDimension) {
      this.scale = pScale;
      this.centerX = pX;
      this.centerZ = pZ;
      this.dimension = pDimension;
      this.trackingPosition = pTrackingPosition;
      this.unlimitedTracking = pUnlimitedTracking;
      this.locked = pLocked;
      this.setDirty();
   }

   public static MapItemSavedData createFresh(double pX, double pZ, byte pScale, boolean pTrackingPosition, boolean pUnlimitedTracking, ResourceKey<Level> pDimension) {
      int i = 128 * (1 << pScale);
      int j = Mth.floor((pX + 64.0D) / (double)i);
      int k = Mth.floor((pZ + 64.0D) / (double)i);
      int l = j * i + i / 2 - 64;
      int i1 = k * i + i / 2 - 64;
      return new MapItemSavedData(l, i1, pScale, pTrackingPosition, pUnlimitedTracking, false, pDimension);
   }

   public static MapItemSavedData createForClient(byte pScale, boolean pLocked, ResourceKey<Level> pDimension) {
      return new MapItemSavedData(0, 0, pScale, false, false, pLocked, pDimension);
   }

   public static MapItemSavedData load(CompoundTag pCompoundTag) {
      ResourceKey<Level> resourcekey = DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, pCompoundTag.get("dimension"))).resultOrPartial(LOGGER::error).orElseThrow(() -> {
         return new IllegalArgumentException("Invalid map dimension: " + pCompoundTag.get("dimension"));
      });
      int i = pCompoundTag.getInt("xCenter");
      int j = pCompoundTag.getInt("zCenter");
      byte b0 = (byte)Mth.clamp(pCompoundTag.getByte("scale"), 0, 4);
      boolean flag = !pCompoundTag.contains("trackingPosition", 1) || pCompoundTag.getBoolean("trackingPosition");
      boolean flag1 = pCompoundTag.getBoolean("unlimitedTracking");
      boolean flag2 = pCompoundTag.getBoolean("locked");
      MapItemSavedData mapitemsaveddata = new MapItemSavedData(i, j, b0, flag, flag1, flag2, resourcekey);
      byte[] abyte = pCompoundTag.getByteArray("colors");
      if (abyte.length == 16384) {
         mapitemsaveddata.colors = abyte;
      }

      ListTag listtag = pCompoundTag.getList("banners", 10);

      for(int k = 0; k < listtag.size(); ++k) {
         MapBanner mapbanner = MapBanner.load(listtag.getCompound(k));
         mapitemsaveddata.bannerMarkers.put(mapbanner.getId(), mapbanner);
         mapitemsaveddata.addDecoration(mapbanner.getDecoration(), (LevelAccessor)null, mapbanner.getId(), (double)mapbanner.getPos().getX(), (double)mapbanner.getPos().getZ(), 180.0D, mapbanner.getName());
      }

      ListTag listtag1 = pCompoundTag.getList("frames", 10);

      for(int l = 0; l < listtag1.size(); ++l) {
         MapFrame mapframe = MapFrame.load(listtag1.getCompound(l));
         mapitemsaveddata.frameMarkers.put(mapframe.getId(), mapframe);
         mapitemsaveddata.addDecoration(MapDecoration.Type.FRAME, (LevelAccessor)null, "frame-" + mapframe.getEntityId(), (double)mapframe.getPos().getX(), (double)mapframe.getPos().getZ(), (double)mapframe.getRotation(), (Component)null);
      }

      return mapitemsaveddata;
   }

   /**
    * Used to save the {@code SavedData} to a {@code CompoundTag}
    * @param pCompoundTag the {@code CompoundTag} to save the {@code SavedData} to
    */
   public CompoundTag save(CompoundTag pCompound) {
      ResourceLocation.CODEC.encodeStart(NbtOps.INSTANCE, this.dimension.location()).resultOrPartial(LOGGER::error).ifPresent((p_77954_) -> {
         pCompound.put("dimension", p_77954_);
      });
      pCompound.putInt("xCenter", this.centerX);
      pCompound.putInt("zCenter", this.centerZ);
      pCompound.putByte("scale", this.scale);
      pCompound.putByteArray("colors", this.colors);
      pCompound.putBoolean("trackingPosition", this.trackingPosition);
      pCompound.putBoolean("unlimitedTracking", this.unlimitedTracking);
      pCompound.putBoolean("locked", this.locked);
      ListTag listtag = new ListTag();

      for(MapBanner mapbanner : this.bannerMarkers.values()) {
         listtag.add(mapbanner.save());
      }

      pCompound.put("banners", listtag);
      ListTag listtag1 = new ListTag();

      for(MapFrame mapframe : this.frameMarkers.values()) {
         listtag1.add(mapframe.save());
      }

      pCompound.put("frames", listtag1);
      return pCompound;
   }

   public MapItemSavedData locked() {
      MapItemSavedData mapitemsaveddata = new MapItemSavedData(this.centerX, this.centerZ, this.scale, this.trackingPosition, this.unlimitedTracking, true, this.dimension);
      mapitemsaveddata.bannerMarkers.putAll(this.bannerMarkers);
      mapitemsaveddata.decorations.putAll(this.decorations);
      mapitemsaveddata.trackedDecorationCount = this.trackedDecorationCount;
      System.arraycopy(this.colors, 0, mapitemsaveddata.colors, 0, this.colors.length);
      mapitemsaveddata.setDirty();
      return mapitemsaveddata;
   }

   public MapItemSavedData scaled(int pScalar) {
      return createFresh((double)this.centerX, (double)this.centerZ, (byte)Mth.clamp(this.scale + pScalar, 0, 4), this.trackingPosition, this.unlimitedTracking, this.dimension);
   }

   /**
    * Adds the player passed to the list of visible players and checks to see which players are visible
    */
   public void tickCarriedBy(Player pPlayer, ItemStack pMapStack) {
      if (!this.carriedByPlayers.containsKey(pPlayer)) {
         MapItemSavedData.HoldingPlayer mapitemsaveddata$holdingplayer = new MapItemSavedData.HoldingPlayer(pPlayer);
         this.carriedByPlayers.put(pPlayer, mapitemsaveddata$holdingplayer);
         this.carriedBy.add(mapitemsaveddata$holdingplayer);
      }

      if (!pPlayer.getInventory().contains(pMapStack)) {
         this.removeDecoration(pPlayer.getName().getString());
      }

      for(int i = 0; i < this.carriedBy.size(); ++i) {
         MapItemSavedData.HoldingPlayer mapitemsaveddata$holdingplayer1 = this.carriedBy.get(i);
         String s = mapitemsaveddata$holdingplayer1.player.getName().getString();
         if (!mapitemsaveddata$holdingplayer1.player.isRemoved() && (mapitemsaveddata$holdingplayer1.player.getInventory().contains(pMapStack) || pMapStack.isFramed())) {
            if (!pMapStack.isFramed() && mapitemsaveddata$holdingplayer1.player.level().dimension() == this.dimension && this.trackingPosition) {
               this.addDecoration(MapDecoration.Type.PLAYER, mapitemsaveddata$holdingplayer1.player.level(), s, mapitemsaveddata$holdingplayer1.player.getX(), mapitemsaveddata$holdingplayer1.player.getZ(), (double)mapitemsaveddata$holdingplayer1.player.getYRot(), (Component)null);
            }
         } else {
            this.carriedByPlayers.remove(mapitemsaveddata$holdingplayer1.player);
            this.carriedBy.remove(mapitemsaveddata$holdingplayer1);
            this.removeDecoration(s);
         }
      }

      if (pMapStack.isFramed() && this.trackingPosition) {
         ItemFrame itemframe = pMapStack.getFrame();
         BlockPos blockpos = itemframe.getPos();
         MapFrame mapframe1 = this.frameMarkers.get(MapFrame.frameId(blockpos));
         if (mapframe1 != null && itemframe.getId() != mapframe1.getEntityId() && this.frameMarkers.containsKey(mapframe1.getId())) {
            this.removeDecoration("frame-" + mapframe1.getEntityId());
         }

         MapFrame mapframe = new MapFrame(blockpos, itemframe.getDirection().get2DDataValue() * 90, itemframe.getId());
         this.addDecoration(MapDecoration.Type.FRAME, pPlayer.level(), "frame-" + itemframe.getId(), (double)blockpos.getX(), (double)blockpos.getZ(), (double)(itemframe.getDirection().get2DDataValue() * 90), (Component)null);
         this.frameMarkers.put(mapframe.getId(), mapframe);
      }

      CompoundTag compoundtag = pMapStack.getTag();
      if (compoundtag != null && compoundtag.contains("Decorations", 9)) {
         ListTag listtag = compoundtag.getList("Decorations", 10);

         for(int j = 0; j < listtag.size(); ++j) {
            CompoundTag compoundtag1 = listtag.getCompound(j);
            if (!this.decorations.containsKey(compoundtag1.getString("id"))) {
               this.addDecoration(MapDecoration.Type.byIcon(compoundtag1.getByte("type")), pPlayer.level(), compoundtag1.getString("id"), compoundtag1.getDouble("x"), compoundtag1.getDouble("z"), compoundtag1.getDouble("rot"), (Component)null);
            }
         }
      }

   }

   private void removeDecoration(String pIdentifier) {
      MapDecoration mapdecoration = this.decorations.remove(pIdentifier);
      if (mapdecoration != null && mapdecoration.getType().shouldTrackCount()) {
         --this.trackedDecorationCount;
      }

      this.setDecorationsDirty();
   }

   public static void addTargetDecoration(ItemStack pMap, BlockPos pTarget, String pDecorationName, MapDecoration.Type pType) {
      ListTag listtag;
      if (pMap.hasTag() && pMap.getTag().contains("Decorations", 9)) {
         listtag = pMap.getTag().getList("Decorations", 10);
      } else {
         listtag = new ListTag();
         pMap.addTagElement("Decorations", listtag);
      }

      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putByte("type", pType.getIcon());
      compoundtag.putString("id", pDecorationName);
      compoundtag.putDouble("x", (double)pTarget.getX());
      compoundtag.putDouble("z", (double)pTarget.getZ());
      compoundtag.putDouble("rot", 180.0D);
      listtag.add(compoundtag);
      if (pType.hasMapColor()) {
         CompoundTag compoundtag1 = pMap.getOrCreateTagElement("display");
         compoundtag1.putInt("MapColor", pType.getMapColor());
      }

   }

   private void addDecoration(MapDecoration.Type pType, @Nullable LevelAccessor pLevel, String pDecorationName, double pLevelX, double pLevelZ, double pRotation, @Nullable Component pName) {
      int i = 1 << this.scale;
      float f = (float)(pLevelX - (double)this.centerX) / (float)i;
      float f1 = (float)(pLevelZ - (double)this.centerZ) / (float)i;
      byte b0 = (byte)((int)((double)(f * 2.0F) + 0.5D));
      byte b1 = (byte)((int)((double)(f1 * 2.0F) + 0.5D));
      int j = 63;
      byte b2;
      if (f >= -63.0F && f1 >= -63.0F && f <= 63.0F && f1 <= 63.0F) {
         pRotation += pRotation < 0.0D ? -8.0D : 8.0D;
         b2 = (byte)((int)(pRotation * 16.0D / 360.0D));
         if (this.dimension == Level.NETHER && pLevel != null) {
            int l = (int)(pLevel.getLevelData().getDayTime() / 10L);
            b2 = (byte)(l * l * 34187121 + l * 121 >> 15 & 15);
         }
      } else {
         if (pType != MapDecoration.Type.PLAYER) {
            this.removeDecoration(pDecorationName);
            return;
         }

         int k = 320;
         if (Math.abs(f) < 320.0F && Math.abs(f1) < 320.0F) {
            pType = MapDecoration.Type.PLAYER_OFF_MAP;
         } else {
            if (!this.unlimitedTracking) {
               this.removeDecoration(pDecorationName);
               return;
            }

            pType = MapDecoration.Type.PLAYER_OFF_LIMITS;
         }

         b2 = 0;
         if (f <= -63.0F) {
            b0 = -128;
         }

         if (f1 <= -63.0F) {
            b1 = -128;
         }

         if (f >= 63.0F) {
            b0 = 127;
         }

         if (f1 >= 63.0F) {
            b1 = 127;
         }
      }

      MapDecoration mapdecoration1 = new MapDecoration(pType, b0, b1, b2, pName);
      MapDecoration mapdecoration = this.decorations.put(pDecorationName, mapdecoration1);
      if (!mapdecoration1.equals(mapdecoration)) {
         if (mapdecoration != null && mapdecoration.getType().shouldTrackCount()) {
            --this.trackedDecorationCount;
         }

         if (pType.shouldTrackCount()) {
            ++this.trackedDecorationCount;
         }

         this.setDecorationsDirty();
      }

   }

   @Nullable
   public Packet<?> getUpdatePacket(int pMapId, Player pPlayer) {
      MapItemSavedData.HoldingPlayer mapitemsaveddata$holdingplayer = this.carriedByPlayers.get(pPlayer);
      return mapitemsaveddata$holdingplayer == null ? null : mapitemsaveddata$holdingplayer.nextUpdatePacket(pMapId);
   }

   private void setColorsDirty(int pX, int pZ) {
      this.setDirty();

      for(MapItemSavedData.HoldingPlayer mapitemsaveddata$holdingplayer : this.carriedBy) {
         mapitemsaveddata$holdingplayer.markColorsDirty(pX, pZ);
      }

   }

   private void setDecorationsDirty() {
      this.setDirty();
      this.carriedBy.forEach(MapItemSavedData.HoldingPlayer::markDecorationsDirty);
   }

   public MapItemSavedData.HoldingPlayer getHoldingPlayer(Player pPlayer) {
      MapItemSavedData.HoldingPlayer mapitemsaveddata$holdingplayer = this.carriedByPlayers.get(pPlayer);
      if (mapitemsaveddata$holdingplayer == null) {
         mapitemsaveddata$holdingplayer = new MapItemSavedData.HoldingPlayer(pPlayer);
         this.carriedByPlayers.put(pPlayer, mapitemsaveddata$holdingplayer);
         this.carriedBy.add(mapitemsaveddata$holdingplayer);
      }

      return mapitemsaveddata$holdingplayer;
   }

   public boolean toggleBanner(LevelAccessor pAccessor, BlockPos pPos) {
      double d0 = (double)pPos.getX() + 0.5D;
      double d1 = (double)pPos.getZ() + 0.5D;
      int i = 1 << this.scale;
      double d2 = (d0 - (double)this.centerX) / (double)i;
      double d3 = (d1 - (double)this.centerZ) / (double)i;
      int j = 63;
      if (d2 >= -63.0D && d3 >= -63.0D && d2 <= 63.0D && d3 <= 63.0D) {
         MapBanner mapbanner = MapBanner.fromWorld(pAccessor, pPos);
         if (mapbanner == null) {
            return false;
         }

         if (this.bannerMarkers.remove(mapbanner.getId(), mapbanner)) {
            this.removeDecoration(mapbanner.getId());
            return true;
         }

         if (!this.isTrackedCountOverLimit(256)) {
            this.bannerMarkers.put(mapbanner.getId(), mapbanner);
            this.addDecoration(mapbanner.getDecoration(), pAccessor, mapbanner.getId(), d0, d1, 180.0D, mapbanner.getName());
            return true;
         }
      }

      return false;
   }

   public void checkBanners(BlockGetter pReader, int pX, int pZ) {
      Iterator<MapBanner> iterator = this.bannerMarkers.values().iterator();

      while(iterator.hasNext()) {
         MapBanner mapbanner = iterator.next();
         if (mapbanner.getPos().getX() == pX && mapbanner.getPos().getZ() == pZ) {
            MapBanner mapbanner1 = MapBanner.fromWorld(pReader, mapbanner.getPos());
            if (!mapbanner.equals(mapbanner1)) {
               iterator.remove();
               this.removeDecoration(mapbanner.getId());
            }
         }
      }

   }

   public Collection<MapBanner> getBanners() {
      return this.bannerMarkers.values();
   }

   public void removedFromFrame(BlockPos pPos, int pEntityId) {
      this.removeDecoration("frame-" + pEntityId);
      this.frameMarkers.remove(MapFrame.frameId(pPos));
   }

   public boolean updateColor(int pX, int pZ, byte pColor) {
      byte b0 = this.colors[pX + pZ * 128];
      if (b0 != pColor) {
         this.setColor(pX, pZ, pColor);
         return true;
      } else {
         return false;
      }
   }

   public void setColor(int pX, int pZ, byte pColor) {
      this.colors[pX + pZ * 128] = pColor;
      this.setColorsDirty(pX, pZ);
   }

   public boolean isExplorationMap() {
      for(MapDecoration mapdecoration : this.decorations.values()) {
         if (mapdecoration.getType() == MapDecoration.Type.MANSION || mapdecoration.getType() == MapDecoration.Type.MONUMENT) {
            return true;
         }
      }

      return false;
   }

   public void addClientSideDecorations(List<MapDecoration> pDecorations) {
      this.decorations.clear();
      this.trackedDecorationCount = 0;

      for(int i = 0; i < pDecorations.size(); ++i) {
         MapDecoration mapdecoration = pDecorations.get(i);
         this.decorations.put("icon-" + i, mapdecoration);
         if (mapdecoration.getType().shouldTrackCount()) {
            ++this.trackedDecorationCount;
         }
      }

   }

   public Iterable<MapDecoration> getDecorations() {
      return this.decorations.values();
   }

   public boolean isTrackedCountOverLimit(int pTrackedCount) {
      return this.trackedDecorationCount >= pTrackedCount;
   }

   public class HoldingPlayer {
      public final Player player;
      private boolean dirtyData = true;
      /** The lowest dirty x value */
      private int minDirtyX;
      /** The lowest dirty z value */
      private int minDirtyY;
      /** The highest dirty x value */
      private int maxDirtyX = 127;
      /** The highest dirty z value */
      private int maxDirtyY = 127;
      private boolean dirtyDecorations = true;
      private int tick;
      public int step;

      HoldingPlayer(Player pPlayer) {
         this.player = pPlayer;
      }

      private MapItemSavedData.MapPatch createPatch() {
         int i = this.minDirtyX;
         int j = this.minDirtyY;
         int k = this.maxDirtyX + 1 - this.minDirtyX;
         int l = this.maxDirtyY + 1 - this.minDirtyY;
         byte[] abyte = new byte[k * l];

         for(int i1 = 0; i1 < k; ++i1) {
            for(int j1 = 0; j1 < l; ++j1) {
               abyte[i1 + j1 * k] = MapItemSavedData.this.colors[i + i1 + (j + j1) * 128];
            }
         }

         return new MapItemSavedData.MapPatch(i, j, k, l, abyte);
      }

      @Nullable
      Packet<?> nextUpdatePacket(int pMapId) {
         MapItemSavedData.MapPatch mapitemsaveddata$mappatch;
         if (this.dirtyData) {
            this.dirtyData = false;
            mapitemsaveddata$mappatch = this.createPatch();
         } else {
            mapitemsaveddata$mappatch = null;
         }

         Collection<MapDecoration> collection;
         if (this.dirtyDecorations && this.tick++ % 5 == 0) {
            this.dirtyDecorations = false;
            collection = MapItemSavedData.this.decorations.values();
         } else {
            collection = null;
         }

         return collection == null && mapitemsaveddata$mappatch == null ? null : new ClientboundMapItemDataPacket(pMapId, MapItemSavedData.this.scale, MapItemSavedData.this.locked, collection, mapitemsaveddata$mappatch);
      }

      void markColorsDirty(int pX, int pZ) {
         if (this.dirtyData) {
            this.minDirtyX = Math.min(this.minDirtyX, pX);
            this.minDirtyY = Math.min(this.minDirtyY, pZ);
            this.maxDirtyX = Math.max(this.maxDirtyX, pX);
            this.maxDirtyY = Math.max(this.maxDirtyY, pZ);
         } else {
            this.dirtyData = true;
            this.minDirtyX = pX;
            this.minDirtyY = pZ;
            this.maxDirtyX = pX;
            this.maxDirtyY = pZ;
         }

      }

      private void markDecorationsDirty() {
         this.dirtyDecorations = true;
      }
   }

   public static class MapPatch {
      public final int startX;
      public final int startY;
      public final int width;
      public final int height;
      public final byte[] mapColors;

      public MapPatch(int pStartX, int pStartY, int pWidth, int pHeight, byte[] pMapColors) {
         this.startX = pStartX;
         this.startY = pStartY;
         this.width = pWidth;
         this.height = pHeight;
         this.mapColors = pMapColors;
      }

      public void applyToMap(MapItemSavedData pSavedData) {
         for(int i = 0; i < this.width; ++i) {
            for(int j = 0; j < this.height; ++j) {
               pSavedData.setColor(this.startX + i, this.startY + j, this.mapColors[i + j * this.width]);
            }
         }

      }
   }
}