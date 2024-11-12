package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class DebugPackets {
   private static final Logger LOGGER = LogUtils.getLogger();

   public static void sendGameTestAddMarker(ServerLevel pLevel, BlockPos pPos, String pText, int pColor, int pLifetimeMillis) {
      FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.buffer());
      friendlybytebuf.writeBlockPos(pPos);
      friendlybytebuf.writeInt(pColor);
      friendlybytebuf.writeUtf(pText);
      friendlybytebuf.writeInt(pLifetimeMillis);
      sendPacketToAllPlayers(pLevel, friendlybytebuf, ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_ADD_MARKER);
   }

   public static void sendGameTestClearPacket(ServerLevel pLevel) {
      FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.buffer());
      sendPacketToAllPlayers(pLevel, friendlybytebuf, ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_CLEAR);
   }

   public static void sendPoiPacketsForChunk(ServerLevel pLevel, ChunkPos pChunkPos) {
   }

   public static void sendPoiAddedPacket(ServerLevel pLevel, BlockPos pPos) {
      sendVillageSectionsPacket(pLevel, pPos);
   }

   public static void sendPoiRemovedPacket(ServerLevel pLevel, BlockPos pPos) {
      sendVillageSectionsPacket(pLevel, pPos);
   }

   public static void sendPoiTicketCountPacket(ServerLevel pLevel, BlockPos pPos) {
      sendVillageSectionsPacket(pLevel, pPos);
   }

   private static void sendVillageSectionsPacket(ServerLevel pLevel, BlockPos pPos) {
   }

   public static void sendPathFindingPacket(Level pLevel, Mob pMob, @Nullable Path pPath, float pMaxDistanceToWaypoint) {
   }

   public static void sendNeighborsUpdatePacket(Level pLevel, BlockPos pPos) {
   }

   public static void sendStructurePacket(WorldGenLevel pLevel, StructureStart pStructureStart) {
   }

   public static void sendGoalSelector(Level pLevel, Mob pMob, GoalSelector pGoalSelector) {
      if (pLevel instanceof ServerLevel) {
         ;
      }
   }

   public static void sendRaids(ServerLevel pLevel, Collection<Raid> pRaids) {
   }

   public static void sendEntityBrain(LivingEntity pLivingEntity) {
   }

   public static void sendBeeInfo(Bee pBee) {
   }

   public static void sendGameEventInfo(Level pLevel, GameEvent pGameEvent, Vec3 pPos) {
   }

   public static void sendGameEventListenerInfo(Level pLevel, GameEventListener pGameEventListener) {
   }

   public static void sendHiveInfo(Level pLevel, BlockPos pPos, BlockState pBlockState, BeehiveBlockEntity pHiveBlockEntity) {
   }

   private static void writeBrain(LivingEntity pLivingEntity, FriendlyByteBuf pBuffer) {
      Brain<?> brain = pLivingEntity.getBrain();
      long i = pLivingEntity.level().getGameTime();
      if (pLivingEntity instanceof InventoryCarrier) {
         Container container = ((InventoryCarrier)pLivingEntity).getInventory();
         pBuffer.writeUtf(container.isEmpty() ? "" : container.toString());
      } else {
         pBuffer.writeUtf("");
      }

      pBuffer.writeOptional(brain.hasMemoryValue(MemoryModuleType.PATH) ? brain.getMemory(MemoryModuleType.PATH) : Optional.empty(), (p_237912_, p_237913_) -> {
         p_237913_.writeToStream(p_237912_);
      });
      if (pLivingEntity instanceof Villager villager) {
         boolean flag = villager.wantsToSpawnGolem(i);
         pBuffer.writeBoolean(flag);
      } else {
         pBuffer.writeBoolean(false);
      }

      if (pLivingEntity.getType() == EntityType.WARDEN) {
         Warden warden = (Warden)pLivingEntity;
         pBuffer.writeInt(warden.getClientAngerLevel());
      } else {
         pBuffer.writeInt(-1);
      }

      pBuffer.writeCollection(brain.getActiveActivities(), (p_237909_, p_237910_) -> {
         p_237909_.writeUtf(p_237910_.getName());
      });
      Set<String> set = brain.getRunningBehaviors().stream().map(BehaviorControl::debugString).collect(Collectors.toSet());
      pBuffer.writeCollection(set, FriendlyByteBuf::writeUtf);
      pBuffer.writeCollection(getMemoryDescriptions(pLivingEntity, i), (p_237915_, p_237916_) -> {
         String s = StringUtil.truncateStringIfNecessary(p_237916_, 255, true);
         p_237915_.writeUtf(s);
      });
      if (pLivingEntity instanceof Villager) {
         Set<BlockPos> set1 = Stream.of(MemoryModuleType.JOB_SITE, MemoryModuleType.HOME, MemoryModuleType.MEETING_POINT).map(brain::getMemory).flatMap(Optional::stream).map(GlobalPos::pos).collect(Collectors.toSet());
         pBuffer.writeCollection(set1, FriendlyByteBuf::writeBlockPos);
      } else {
         pBuffer.writeVarInt(0);
      }

      if (pLivingEntity instanceof Villager) {
         Set<BlockPos> set2 = Stream.of(MemoryModuleType.POTENTIAL_JOB_SITE).map(brain::getMemory).flatMap(Optional::stream).map(GlobalPos::pos).collect(Collectors.toSet());
         pBuffer.writeCollection(set2, FriendlyByteBuf::writeBlockPos);
      } else {
         pBuffer.writeVarInt(0);
      }

      if (pLivingEntity instanceof Villager) {
         Map<UUID, Object2IntMap<GossipType>> map = ((Villager)pLivingEntity).getGossips().getGossipEntries();
         List<String> list = Lists.newArrayList();
         map.forEach((p_237900_, p_237901_) -> {
            String s = DebugEntityNameGenerator.getEntityName(p_237900_);
            p_237901_.forEach((p_237896_, p_237897_) -> {
               list.add(s + ": " + p_237896_ + ": " + p_237897_);
            });
         });
         pBuffer.writeCollection(list, FriendlyByteBuf::writeUtf);
      } else {
         pBuffer.writeVarInt(0);
      }

   }

   private static List<String> getMemoryDescriptions(LivingEntity pEntity, long pGameTime) {
      Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> map = pEntity.getBrain().getMemories();
      List<String> list = Lists.newArrayList();

      for(Map.Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> entry : map.entrySet()) {
         MemoryModuleType<?> memorymoduletype = entry.getKey();
         Optional<? extends ExpirableValue<?>> optional = entry.getValue();
         String s;
         if (optional.isPresent()) {
            ExpirableValue<?> expirablevalue = optional.get();
            Object object = expirablevalue.getValue();
            if (memorymoduletype == MemoryModuleType.HEARD_BELL_TIME) {
               long i = pGameTime - (Long)object;
               s = i + " ticks ago";
            } else if (expirablevalue.canExpire()) {
               s = getShortDescription((ServerLevel)pEntity.level(), object) + " (ttl: " + expirablevalue.getTimeToLive() + ")";
            } else {
               s = getShortDescription((ServerLevel)pEntity.level(), object);
            }
         } else {
            s = "-";
         }

         list.add(BuiltInRegistries.MEMORY_MODULE_TYPE.getKey(memorymoduletype).getPath() + ": " + s);
      }

      list.sort(String::compareTo);
      return list;
   }

   private static String getShortDescription(ServerLevel pLevel, @Nullable Object pObject) {
      if (pObject == null) {
         return "-";
      } else if (pObject instanceof UUID) {
         return getShortDescription(pLevel, pLevel.getEntity((UUID)pObject));
      } else if (pObject instanceof LivingEntity) {
         Entity entity1 = (Entity)pObject;
         return DebugEntityNameGenerator.getEntityName(entity1);
      } else if (pObject instanceof Nameable) {
         return ((Nameable)pObject).getName().getString();
      } else if (pObject instanceof WalkTarget) {
         return getShortDescription(pLevel, ((WalkTarget)pObject).getTarget());
      } else if (pObject instanceof EntityTracker) {
         return getShortDescription(pLevel, ((EntityTracker)pObject).getEntity());
      } else if (pObject instanceof GlobalPos) {
         return getShortDescription(pLevel, ((GlobalPos)pObject).pos());
      } else if (pObject instanceof BlockPosTracker) {
         return getShortDescription(pLevel, ((BlockPosTracker)pObject).currentBlockPosition());
      } else if (pObject instanceof DamageSource) {
         Entity entity = ((DamageSource)pObject).getEntity();
         return entity == null ? pObject.toString() : getShortDescription(pLevel, entity);
      } else if (!(pObject instanceof Collection)) {
         return pObject.toString();
      } else {
         List<String> list = Lists.newArrayList();

         for(Object object : (Iterable)pObject) {
            list.add(getShortDescription(pLevel, object));
         }

         return list.toString();
      }
   }

   private static void sendPacketToAllPlayers(ServerLevel pLevel, FriendlyByteBuf pBuffer, ResourceLocation pIdentifier) {
      Packet<?> packet = new ClientboundCustomPayloadPacket(pIdentifier, pBuffer);

      for(ServerPlayer serverplayer : pLevel.players()) {
         serverplayer.connection.send(packet);
      }

   }
}