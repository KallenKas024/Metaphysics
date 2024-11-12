package net.minecraft.world.entity.ai.gossip;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.DoublePredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import org.slf4j.Logger;

public class GossipContainer {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int DISCARD_THRESHOLD = 2;
   private final Map<UUID, GossipContainer.EntityGossips> gossips = Maps.newHashMap();

   @VisibleForDebug
   public Map<UUID, Object2IntMap<GossipType>> getGossipEntries() {
      Map<UUID, Object2IntMap<GossipType>> map = Maps.newHashMap();
      this.gossips.keySet().forEach((p_148167_) -> {
         GossipContainer.EntityGossips gossipcontainer$entitygossips = this.gossips.get(p_148167_);
         map.put(p_148167_, gossipcontainer$entitygossips.entries);
      });
      return map;
   }

   public void decay() {
      Iterator<GossipContainer.EntityGossips> iterator = this.gossips.values().iterator();

      while(iterator.hasNext()) {
         GossipContainer.EntityGossips gossipcontainer$entitygossips = iterator.next();
         gossipcontainer$entitygossips.decay();
         if (gossipcontainer$entitygossips.isEmpty()) {
            iterator.remove();
         }
      }

   }

   private Stream<GossipContainer.GossipEntry> unpack() {
      return this.gossips.entrySet().stream().flatMap((p_26185_) -> {
         return p_26185_.getValue().unpack(p_26185_.getKey());
      });
   }

   private Collection<GossipContainer.GossipEntry> selectGossipsForTransfer(RandomSource pRandom, int pAmount) {
      List<GossipContainer.GossipEntry> list = this.unpack().toList();
      if (list.isEmpty()) {
         return Collections.emptyList();
      } else {
         int[] aint = new int[list.size()];
         int i = 0;

         for(int j = 0; j < list.size(); ++j) {
            GossipContainer.GossipEntry gossipcontainer$gossipentry = list.get(j);
            i += Math.abs(gossipcontainer$gossipentry.weightedValue());
            aint[j] = i - 1;
         }

         Set<GossipContainer.GossipEntry> set = Sets.newIdentityHashSet();

         for(int i1 = 0; i1 < pAmount; ++i1) {
            int k = pRandom.nextInt(i);
            int l = Arrays.binarySearch(aint, k);
            set.add(list.get(l < 0 ? -l - 1 : l));
         }

         return set;
      }
   }

   private GossipContainer.EntityGossips getOrCreate(UUID pIdentifier) {
      return this.gossips.computeIfAbsent(pIdentifier, (p_26202_) -> {
         return new GossipContainer.EntityGossips();
      });
   }

   public void transferFrom(GossipContainer pContainer, RandomSource pRandomSource, int pAmount) {
      Collection<GossipContainer.GossipEntry> collection = pContainer.selectGossipsForTransfer(pRandomSource, pAmount);
      collection.forEach((p_26200_) -> {
         int i = p_26200_.value - p_26200_.type.decayPerTransfer;
         if (i >= 2) {
            this.getOrCreate(p_26200_.target).entries.mergeInt(p_26200_.type, i, GossipContainer::mergeValuesForTransfer);
         }

      });
   }

   public int getReputation(UUID pIdentifier, Predicate<GossipType> pGossip) {
      GossipContainer.EntityGossips gossipcontainer$entitygossips = this.gossips.get(pIdentifier);
      return gossipcontainer$entitygossips != null ? gossipcontainer$entitygossips.weightedValue(pGossip) : 0;
   }

   public long getCountForType(GossipType pGossipType, DoublePredicate pGossipPredicate) {
      return this.gossips.values().stream().filter((p_148174_) -> {
         return pGossipPredicate.test((double)(p_148174_.entries.getOrDefault(pGossipType, 0) * pGossipType.weight));
      }).count();
   }

   public void add(UUID pIdentifier, GossipType pGossipType, int pGossipValue) {
      GossipContainer.EntityGossips gossipcontainer$entitygossips = this.getOrCreate(pIdentifier);
      gossipcontainer$entitygossips.entries.mergeInt(pGossipType, pGossipValue, (p_186096_, p_186097_) -> {
         return this.mergeValuesForAddition(pGossipType, p_186096_, p_186097_);
      });
      gossipcontainer$entitygossips.makeSureValueIsntTooLowOrTooHigh(pGossipType);
      if (gossipcontainer$entitygossips.isEmpty()) {
         this.gossips.remove(pIdentifier);
      }

   }

   public void remove(UUID pIdentifier, GossipType pGossipType, int pGossipValue) {
      this.add(pIdentifier, pGossipType, -pGossipValue);
   }

   public void remove(UUID pIdentifier, GossipType pGossipType) {
      GossipContainer.EntityGossips gossipcontainer$entitygossips = this.gossips.get(pIdentifier);
      if (gossipcontainer$entitygossips != null) {
         gossipcontainer$entitygossips.remove(pGossipType);
         if (gossipcontainer$entitygossips.isEmpty()) {
            this.gossips.remove(pIdentifier);
         }
      }

   }

   public void remove(GossipType pGossipType) {
      Iterator<GossipContainer.EntityGossips> iterator = this.gossips.values().iterator();

      while(iterator.hasNext()) {
         GossipContainer.EntityGossips gossipcontainer$entitygossips = iterator.next();
         gossipcontainer$entitygossips.remove(pGossipType);
         if (gossipcontainer$entitygossips.isEmpty()) {
            iterator.remove();
         }
      }

   }

   public <T> T store(DynamicOps<T> pOps) {
      return GossipContainer.GossipEntry.LIST_CODEC.encodeStart(pOps, this.unpack().toList()).resultOrPartial((p_262900_) -> {
         LOGGER.warn("Failed to serialize gossips: {}", (Object)p_262900_);
      }).orElseGet(pOps::emptyList);
   }

   public void update(Dynamic<?> pDynamic) {
      GossipContainer.GossipEntry.LIST_CODEC.decode(pDynamic).resultOrPartial((p_262901_) -> {
         LOGGER.warn("Failed to deserialize gossips: {}", (Object)p_262901_);
      }).stream().flatMap((p_262899_) -> {
         return p_262899_.getFirst().stream();
      }).forEach((p_26162_) -> {
         this.getOrCreate(p_26162_.target).entries.put(p_26162_.type, p_26162_.value);
      });
   }

   /**
    * Returns the greater of two int values
    */
   private static int mergeValuesForTransfer(int p_26159_, int p_26160_) {
      return Math.max(p_26159_, p_26160_);
   }

   private int mergeValuesForAddition(GossipType pGossipType, int pExisting, int pAdditive) {
      int i = pExisting + pAdditive;
      return i > pGossipType.max ? Math.max(pGossipType.max, pExisting) : i;
   }

   static class EntityGossips {
      final Object2IntMap<GossipType> entries = new Object2IntOpenHashMap<>();

      public int weightedValue(Predicate<GossipType> pGossipType) {
         return this.entries.object2IntEntrySet().stream().filter((p_26224_) -> {
            return pGossipType.test(p_26224_.getKey());
         }).mapToInt((p_26214_) -> {
            return p_26214_.getIntValue() * (p_26214_.getKey()).weight;
         }).sum();
      }

      public Stream<GossipContainer.GossipEntry> unpack(UUID pIdentifier) {
         return this.entries.object2IntEntrySet().stream().map((p_26219_) -> {
            return new GossipContainer.GossipEntry(pIdentifier, p_26219_.getKey(), p_26219_.getIntValue());
         });
      }

      public void decay() {
         ObjectIterator<Object2IntMap.Entry<GossipType>> objectiterator = this.entries.object2IntEntrySet().iterator();

         while(objectiterator.hasNext()) {
            Object2IntMap.Entry<GossipType> entry = objectiterator.next();
            int i = entry.getIntValue() - (entry.getKey()).decayPerDay;
            if (i < 2) {
               objectiterator.remove();
            } else {
               entry.setValue(i);
            }
         }

      }

      public boolean isEmpty() {
         return this.entries.isEmpty();
      }

      public void makeSureValueIsntTooLowOrTooHigh(GossipType pGossipType) {
         int i = this.entries.getInt(pGossipType);
         if (i > pGossipType.max) {
            this.entries.put(pGossipType, pGossipType.max);
         }

         if (i < 2) {
            this.remove(pGossipType);
         }

      }

      public void remove(GossipType pGossipType) {
         this.entries.removeInt(pGossipType);
      }
   }

   static record GossipEntry(UUID target, GossipType type, int value) {
      public static final Codec<GossipContainer.GossipEntry> CODEC = RecordCodecBuilder.create((p_263007_) -> {
         return p_263007_.group(UUIDUtil.CODEC.fieldOf("Target").forGetter(GossipContainer.GossipEntry::target), GossipType.CODEC.fieldOf("Type").forGetter(GossipContainer.GossipEntry::type), ExtraCodecs.POSITIVE_INT.fieldOf("Value").forGetter(GossipContainer.GossipEntry::value)).apply(p_263007_, GossipContainer.GossipEntry::new);
      });
      public static final Codec<List<GossipContainer.GossipEntry>> LIST_CODEC = CODEC.listOf();

      public int weightedValue() {
         return this.value * this.type.weight;
      }
   }
}