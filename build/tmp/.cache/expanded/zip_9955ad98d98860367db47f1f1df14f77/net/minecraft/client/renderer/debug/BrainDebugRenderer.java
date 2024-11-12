package net.minecraft.client.renderer.debug;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class BrainDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final boolean SHOW_NAME_FOR_ALL = true;
   private static final boolean SHOW_PROFESSION_FOR_ALL = false;
   private static final boolean SHOW_BEHAVIORS_FOR_ALL = false;
   private static final boolean SHOW_ACTIVITIES_FOR_ALL = false;
   private static final boolean SHOW_INVENTORY_FOR_ALL = false;
   private static final boolean SHOW_GOSSIPS_FOR_ALL = false;
   private static final boolean SHOW_PATH_FOR_ALL = false;
   private static final boolean SHOW_HEALTH_FOR_ALL = false;
   private static final boolean SHOW_WANTS_GOLEM_FOR_ALL = true;
   private static final boolean SHOW_ANGER_LEVEL_FOR_ALL = false;
   private static final boolean SHOW_NAME_FOR_SELECTED = true;
   private static final boolean SHOW_PROFESSION_FOR_SELECTED = true;
   private static final boolean SHOW_BEHAVIORS_FOR_SELECTED = true;
   private static final boolean SHOW_ACTIVITIES_FOR_SELECTED = true;
   private static final boolean SHOW_MEMORIES_FOR_SELECTED = true;
   private static final boolean SHOW_INVENTORY_FOR_SELECTED = true;
   private static final boolean SHOW_GOSSIPS_FOR_SELECTED = true;
   private static final boolean SHOW_PATH_FOR_SELECTED = true;
   private static final boolean SHOW_HEALTH_FOR_SELECTED = true;
   private static final boolean SHOW_WANTS_GOLEM_FOR_SELECTED = true;
   private static final boolean SHOW_ANGER_LEVEL_FOR_SELECTED = true;
   private static final boolean SHOW_POI_INFO = true;
   private static final int MAX_RENDER_DIST_FOR_BRAIN_INFO = 30;
   private static final int MAX_RENDER_DIST_FOR_POI_INFO = 30;
   private static final int MAX_TARGETING_DIST = 8;
   private static final float TEXT_SCALE = 0.02F;
   private static final int WHITE = -1;
   private static final int YELLOW = -256;
   private static final int CYAN = -16711681;
   private static final int GREEN = -16711936;
   private static final int GRAY = -3355444;
   private static final int PINK = -98404;
   private static final int RED = -65536;
   private static final int ORANGE = -23296;
   private final Minecraft minecraft;
   private final Map<BlockPos, BrainDebugRenderer.PoiInfo> pois = Maps.newHashMap();
   private final Map<UUID, BrainDebugRenderer.BrainDump> brainDumpsPerEntity = Maps.newHashMap();
   @Nullable
   private UUID lastLookedAtUuid;

   public BrainDebugRenderer(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   public void clear() {
      this.pois.clear();
      this.brainDumpsPerEntity.clear();
      this.lastLookedAtUuid = null;
   }

   public void addPoi(BrainDebugRenderer.PoiInfo pPoiInfo) {
      this.pois.put(pPoiInfo.pos, pPoiInfo);
   }

   public void removePoi(BlockPos pPos) {
      this.pois.remove(pPos);
   }

   public void setFreeTicketCount(BlockPos pPos, int pFreeTicketCount) {
      BrainDebugRenderer.PoiInfo braindebugrenderer$poiinfo = this.pois.get(pPos);
      if (braindebugrenderer$poiinfo == null) {
         LOGGER.warn("Strange, setFreeTicketCount was called for an unknown POI: {}", (Object)pPos);
      } else {
         braindebugrenderer$poiinfo.freeTicketCount = pFreeTicketCount;
      }
   }

   public void addOrUpdateBrainDump(BrainDebugRenderer.BrainDump pBrainDump) {
      this.brainDumpsPerEntity.put(pBrainDump.uuid, pBrainDump);
   }

   public void removeBrainDump(int pId) {
      this.brainDumpsPerEntity.values().removeIf((p_173814_) -> {
         return p_173814_.id == pId;
      });
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, double pCamX, double pCamY, double pCamZ) {
      this.clearRemovedEntities();
      this.doRender(pPoseStack, pBuffer, pCamX, pCamY, pCamZ);
      if (!this.minecraft.player.isSpectator()) {
         this.updateLastLookedAtUuid();
      }

   }

   private void clearRemovedEntities() {
      this.brainDumpsPerEntity.entrySet().removeIf((p_113263_) -> {
         Entity entity = this.minecraft.level.getEntity((p_113263_.getValue()).id);
         return entity == null || entity.isRemoved();
      });
   }

   private void doRender(PoseStack pPoseStack, MultiBufferSource pBuffer, double pX, double pY, double pZ) {
      BlockPos blockpos = BlockPos.containing(pX, pY, pZ);
      this.brainDumpsPerEntity.values().forEach((p_269714_) -> {
         if (this.isPlayerCloseEnoughToMob(p_269714_)) {
            this.renderBrainInfo(pPoseStack, pBuffer, p_269714_, pX, pY, pZ);
         }

      });

      for(BlockPos blockpos1 : this.pois.keySet()) {
         if (blockpos.closerThan(blockpos1, 30.0D)) {
            highlightPoi(pPoseStack, pBuffer, blockpos1);
         }
      }

      this.pois.values().forEach((p_269718_) -> {
         if (blockpos.closerThan(p_269718_.pos, 30.0D)) {
            this.renderPoiInfo(pPoseStack, pBuffer, p_269718_);
         }

      });
      this.getGhostPois().forEach((p_269707_, p_269708_) -> {
         if (blockpos.closerThan(p_269707_, 30.0D)) {
            this.renderGhostPoi(pPoseStack, pBuffer, p_269707_, p_269708_);
         }

      });
   }

   private static void highlightPoi(PoseStack pPoseStack, MultiBufferSource pBuffer, BlockPos pPos) {
      float f = 0.05F;
      DebugRenderer.renderFilledBox(pPoseStack, pBuffer, pPos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
   }

   private void renderGhostPoi(PoseStack pPoseStack, MultiBufferSource pBuffer, BlockPos pPos, List<String> p_270882_) {
      float f = 0.05F;
      DebugRenderer.renderFilledBox(pPoseStack, pBuffer, pPos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
      renderTextOverPos(pPoseStack, pBuffer, "" + p_270882_, pPos, 0, -256);
      renderTextOverPos(pPoseStack, pBuffer, "Ghost POI", pPos, 1, -65536);
   }

   private void renderPoiInfo(PoseStack pPoseStack, MultiBufferSource pBuffer, BrainDebugRenderer.PoiInfo pPoiInfo) {
      int i = 0;
      Set<String> set = this.getTicketHolderNames(pPoiInfo);
      if (set.size() < 4) {
         renderTextOverPoi(pPoseStack, pBuffer, "Owners: " + set, pPoiInfo, i, -256);
      } else {
         renderTextOverPoi(pPoseStack, pBuffer, set.size() + " ticket holders", pPoiInfo, i, -256);
      }

      ++i;
      Set<String> set1 = this.getPotentialTicketHolderNames(pPoiInfo);
      if (set1.size() < 4) {
         renderTextOverPoi(pPoseStack, pBuffer, "Candidates: " + set1, pPoiInfo, i, -23296);
      } else {
         renderTextOverPoi(pPoseStack, pBuffer, set1.size() + " potential owners", pPoiInfo, i, -23296);
      }

      ++i;
      renderTextOverPoi(pPoseStack, pBuffer, "Free tickets: " + pPoiInfo.freeTicketCount, pPoiInfo, i, -256);
      ++i;
      renderTextOverPoi(pPoseStack, pBuffer, pPoiInfo.type, pPoiInfo, i, -1);
   }

   private void renderPath(PoseStack pPoseStack, MultiBufferSource pBuffer, BrainDebugRenderer.BrainDump pBrainDump, double pX, double pY, double pZ) {
      if (pBrainDump.path != null) {
         PathfindingRenderer.renderPath(pPoseStack, pBuffer, pBrainDump.path, 0.5F, false, false, pX, pY, pZ);
      }

   }

   private void renderBrainInfo(PoseStack pPoseStack, MultiBufferSource pBuffer, BrainDebugRenderer.BrainDump pBrainDump, double pX, double pY, double pZ) {
      boolean flag = this.isMobSelected(pBrainDump);
      int i = 0;
      renderTextOverMob(pPoseStack, pBuffer, pBrainDump.pos, i, pBrainDump.name, -1, 0.03F);
      ++i;
      if (flag) {
         renderTextOverMob(pPoseStack, pBuffer, pBrainDump.pos, i, pBrainDump.profession + " " + pBrainDump.xp + " xp", -1, 0.02F);
         ++i;
      }

      if (flag) {
         int j = pBrainDump.health < pBrainDump.maxHealth ? -23296 : -1;
         renderTextOverMob(pPoseStack, pBuffer, pBrainDump.pos, i, "health: " + String.format(Locale.ROOT, "%.1f", pBrainDump.health) + " / " + String.format(Locale.ROOT, "%.1f", pBrainDump.maxHealth), j, 0.02F);
         ++i;
      }

      if (flag && !pBrainDump.inventory.equals("")) {
         renderTextOverMob(pPoseStack, pBuffer, pBrainDump.pos, i, pBrainDump.inventory, -98404, 0.02F);
         ++i;
      }

      if (flag) {
         for(String s : pBrainDump.behaviors) {
            renderTextOverMob(pPoseStack, pBuffer, pBrainDump.pos, i, s, -16711681, 0.02F);
            ++i;
         }
      }

      if (flag) {
         for(String s1 : pBrainDump.activities) {
            renderTextOverMob(pPoseStack, pBuffer, pBrainDump.pos, i, s1, -16711936, 0.02F);
            ++i;
         }
      }

      if (pBrainDump.wantsGolem) {
         renderTextOverMob(pPoseStack, pBuffer, pBrainDump.pos, i, "Wants Golem", -23296, 0.02F);
         ++i;
      }

      if (flag && pBrainDump.angerLevel != -1) {
         renderTextOverMob(pPoseStack, pBuffer, pBrainDump.pos, i, "Anger Level: " + pBrainDump.angerLevel, -98404, 0.02F);
         ++i;
      }

      if (flag) {
         for(String s2 : pBrainDump.gossips) {
            if (s2.startsWith(pBrainDump.name)) {
               renderTextOverMob(pPoseStack, pBuffer, pBrainDump.pos, i, s2, -1, 0.02F);
            } else {
               renderTextOverMob(pPoseStack, pBuffer, pBrainDump.pos, i, s2, -23296, 0.02F);
            }

            ++i;
         }
      }

      if (flag) {
         for(String s3 : Lists.reverse(pBrainDump.memories)) {
            renderTextOverMob(pPoseStack, pBuffer, pBrainDump.pos, i, s3, -3355444, 0.02F);
            ++i;
         }
      }

      if (flag) {
         this.renderPath(pPoseStack, pBuffer, pBrainDump, pX, pY, pZ);
      }

   }

   private static void renderTextOverPoi(PoseStack pPoseStack, MultiBufferSource pBuffer, String pText, BrainDebugRenderer.PoiInfo pPoiInfo, int pLayer, int pColor) {
      renderTextOverPos(pPoseStack, pBuffer, pText, pPoiInfo.pos, pLayer, pColor);
   }

   private static void renderTextOverPos(PoseStack pPoseStack, MultiBufferSource pBuffer, String pText, BlockPos pPos, int pLayer, int pColor) {
      double d0 = 1.3D;
      double d1 = 0.2D;
      double d2 = (double)pPos.getX() + 0.5D;
      double d3 = (double)pPos.getY() + 1.3D + (double)pLayer * 0.2D;
      double d4 = (double)pPos.getZ() + 0.5D;
      DebugRenderer.renderFloatingText(pPoseStack, pBuffer, pText, d2, d3, d4, pColor, 0.02F, true, 0.0F, true);
   }

   private static void renderTextOverMob(PoseStack pPoseStack, MultiBufferSource pBuffer, Position pPos, int pLayer, String pText, int pColor, float pScale) {
      double d0 = 2.4D;
      double d1 = 0.25D;
      BlockPos blockpos = BlockPos.containing(pPos);
      double d2 = (double)blockpos.getX() + 0.5D;
      double d3 = pPos.y() + 2.4D + (double)pLayer * 0.25D;
      double d4 = (double)blockpos.getZ() + 0.5D;
      float f = 0.5F;
      DebugRenderer.renderFloatingText(pPoseStack, pBuffer, pText, d2, d3, d4, pColor, pScale, false, 0.5F, true);
   }

   private Set<String> getTicketHolderNames(BrainDebugRenderer.PoiInfo pPoiInfo) {
      return this.getTicketHolders(pPoiInfo.pos).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
   }

   private Set<String> getPotentialTicketHolderNames(BrainDebugRenderer.PoiInfo pPoiInfo) {
      return this.getPotentialTicketHolders(pPoiInfo.pos).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
   }

   private boolean isMobSelected(BrainDebugRenderer.BrainDump pBrainDump) {
      return Objects.equals(this.lastLookedAtUuid, pBrainDump.uuid);
   }

   private boolean isPlayerCloseEnoughToMob(BrainDebugRenderer.BrainDump pBrainDump) {
      Player player = this.minecraft.player;
      BlockPos blockpos = BlockPos.containing(player.getX(), pBrainDump.pos.y(), player.getZ());
      BlockPos blockpos1 = BlockPos.containing(pBrainDump.pos);
      return blockpos.closerThan(blockpos1, 30.0D);
   }

   private Collection<UUID> getTicketHolders(BlockPos pPos) {
      return this.brainDumpsPerEntity.values().stream().filter((p_113278_) -> {
         return p_113278_.hasPoi(pPos);
      }).map(BrainDebugRenderer.BrainDump::getUuid).collect(Collectors.toSet());
   }

   private Collection<UUID> getPotentialTicketHolders(BlockPos pPos) {
      return this.brainDumpsPerEntity.values().stream().filter((p_113235_) -> {
         return p_113235_.hasPotentialPoi(pPos);
      }).map(BrainDebugRenderer.BrainDump::getUuid).collect(Collectors.toSet());
   }

   private Map<BlockPos, List<String>> getGhostPois() {
      Map<BlockPos, List<String>> map = Maps.newHashMap();

      for(BrainDebugRenderer.BrainDump braindebugrenderer$braindump : this.brainDumpsPerEntity.values()) {
         for(BlockPos blockpos : Iterables.concat(braindebugrenderer$braindump.pois, braindebugrenderer$braindump.potentialPois)) {
            if (!this.pois.containsKey(blockpos)) {
               map.computeIfAbsent(blockpos, (p_113292_) -> {
                  return Lists.newArrayList();
               }).add(braindebugrenderer$braindump.name);
            }
         }
      }

      return map;
   }

   private void updateLastLookedAtUuid() {
      DebugRenderer.getTargetedEntity(this.minecraft.getCameraEntity(), 8).ifPresent((p_113212_) -> {
         this.lastLookedAtUuid = p_113212_.getUUID();
      });
   }

   @OnlyIn(Dist.CLIENT)
   public static class BrainDump {
      public final UUID uuid;
      public final int id;
      public final String name;
      public final String profession;
      public final int xp;
      public final float health;
      public final float maxHealth;
      public final Position pos;
      public final String inventory;
      public final Path path;
      public final boolean wantsGolem;
      public final int angerLevel;
      public final List<String> activities = Lists.newArrayList();
      public final List<String> behaviors = Lists.newArrayList();
      public final List<String> memories = Lists.newArrayList();
      public final List<String> gossips = Lists.newArrayList();
      public final Set<BlockPos> pois = Sets.newHashSet();
      public final Set<BlockPos> potentialPois = Sets.newHashSet();

      public BrainDump(UUID pUuid, int pId, String pName, String pProfession, int pXp, float pHealth, float pMaxHealth, Position pPos, String pInventory, @Nullable Path pPath, boolean pWantsGolem, int pAngerLevel) {
         this.uuid = pUuid;
         this.id = pId;
         this.name = pName;
         this.profession = pProfession;
         this.xp = pXp;
         this.health = pHealth;
         this.maxHealth = pMaxHealth;
         this.pos = pPos;
         this.inventory = pInventory;
         this.path = pPath;
         this.wantsGolem = pWantsGolem;
         this.angerLevel = pAngerLevel;
      }

      boolean hasPoi(BlockPos pPos) {
         return this.pois.stream().anyMatch(pPos::equals);
      }

      boolean hasPotentialPoi(BlockPos pPos) {
         return this.potentialPois.contains(pPos);
      }

      public UUID getUuid() {
         return this.uuid;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class PoiInfo {
      public final BlockPos pos;
      public String type;
      public int freeTicketCount;

      public PoiInfo(BlockPos pPos, String pType, int pFreeTicketCount) {
         this.pos = pPos;
         this.type = pType;
         this.freeTicketCount = pFreeTicketCount;
      }
   }
}