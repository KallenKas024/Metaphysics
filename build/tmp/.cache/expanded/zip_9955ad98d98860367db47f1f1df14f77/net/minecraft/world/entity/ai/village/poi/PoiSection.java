package net.minecraft.world.entity.ai.village.poi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.util.VisibleForDebug;
import org.slf4j.Logger;

public class PoiSection {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Short2ObjectMap<PoiRecord> records = new Short2ObjectOpenHashMap<>();
   private final Map<Holder<PoiType>, Set<PoiRecord>> byType = Maps.newHashMap();
   private final Runnable setDirty;
   private boolean isValid;

   public static Codec<PoiSection> codec(Runnable pExecutable) {
      return RecordCodecBuilder.<PoiSection>create((p_27299_) -> {
         return p_27299_.group(RecordCodecBuilder.point(pExecutable), Codec.BOOL.optionalFieldOf("Valid", Boolean.valueOf(false)).forGetter((p_148681_) -> {
            return p_148681_.isValid;
         }), PoiRecord.codec(pExecutable).listOf().fieldOf("Records").forGetter((p_148675_) -> {
            return ImmutableList.copyOf(p_148675_.records.values());
         })).apply(p_27299_, PoiSection::new);
      }).orElseGet(Util.prefix("Failed to read POI section: ", LOGGER::error), () -> {
         return new PoiSection(pExecutable, false, ImmutableList.of());
      });
   }

   public PoiSection(Runnable pSetDirty) {
      this(pSetDirty, true, ImmutableList.of());
   }

   private PoiSection(Runnable p_27269_, boolean p_27270_, List<PoiRecord> p_27271_) {
      this.setDirty = p_27269_;
      this.isValid = p_27270_;
      p_27271_.forEach(this::add);
   }

   public Stream<PoiRecord> getRecords(Predicate<Holder<PoiType>> pTypePredicate, PoiManager.Occupancy pStatus) {
      return this.byType.entrySet().stream().filter((p_27309_) -> {
         return pTypePredicate.test(p_27309_.getKey());
      }).flatMap((p_27301_) -> {
         return p_27301_.getValue().stream();
      }).filter(pStatus.getTest());
   }

   public void add(BlockPos pPos, Holder<PoiType> pType) {
      if (this.add(new PoiRecord(pPos, pType, this.setDirty))) {
         LOGGER.debug("Added POI of type {} @ {}", pType.unwrapKey().map((p_218020_) -> {
            return p_218020_.location().toString();
         }).orElse("[unregistered]"), pPos);
         this.setDirty.run();
      }

   }

   private boolean add(PoiRecord p_27274_) {
      BlockPos blockpos = p_27274_.getPos();
      Holder<PoiType> holder = p_27274_.getPoiType();
      short short1 = SectionPos.sectionRelativePos(blockpos);
      PoiRecord poirecord = this.records.get(short1);
      if (poirecord != null) {
         if (holder.equals(poirecord.getPoiType())) {
            return false;
         }

         Util.logAndPauseIfInIde("POI data mismatch: already registered at " + blockpos);
      }

      this.records.put(short1, p_27274_);
      this.byType.computeIfAbsent(holder, (p_218029_) -> {
         return Sets.newHashSet();
      }).add(p_27274_);
      return true;
   }

   public void remove(BlockPos pPos) {
      PoiRecord poirecord = this.records.remove(SectionPos.sectionRelativePos(pPos));
      if (poirecord == null) {
         LOGGER.error("POI data mismatch: never registered at {}", (Object)pPos);
      } else {
         this.byType.get(poirecord.getPoiType()).remove(poirecord);
         LOGGER.debug("Removed POI of type {} @ {}", LogUtils.defer(poirecord::getPoiType), LogUtils.defer(poirecord::getPos));
         this.setDirty.run();
      }
   }

   /** @deprecated */
   @Deprecated
   @VisibleForDebug
   public int getFreeTickets(BlockPos pPos) {
      return this.getPoiRecord(pPos).map(PoiRecord::getFreeTickets).orElse(0);
   }

   public boolean release(BlockPos pPos) {
      PoiRecord poirecord = this.records.get(SectionPos.sectionRelativePos(pPos));
      if (poirecord == null) {
         throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("POI never registered at " + pPos));
      } else {
         boolean flag = poirecord.releaseTicket();
         this.setDirty.run();
         return flag;
      }
   }

   public boolean exists(BlockPos pPos, Predicate<Holder<PoiType>> pTypePredicate) {
      return this.getType(pPos).filter(pTypePredicate).isPresent();
   }

   public Optional<Holder<PoiType>> getType(BlockPos pPos) {
      return this.getPoiRecord(pPos).map(PoiRecord::getPoiType);
   }

   private Optional<PoiRecord> getPoiRecord(BlockPos pPos) {
      return Optional.ofNullable(this.records.get(SectionPos.sectionRelativePos(pPos)));
   }

   public void refresh(Consumer<BiConsumer<BlockPos, Holder<PoiType>>> pPosToTypeConsumer) {
      if (!this.isValid) {
         Short2ObjectMap<PoiRecord> short2objectmap = new Short2ObjectOpenHashMap<>(this.records);
         this.clear();
         pPosToTypeConsumer.accept((p_218032_, p_218033_) -> {
            short short1 = SectionPos.sectionRelativePos(p_218032_);
            PoiRecord poirecord = short2objectmap.computeIfAbsent(short1, (p_218027_) -> {
               return new PoiRecord(p_218032_, p_218033_, this.setDirty);
            });
            this.add(poirecord);
         });
         this.isValid = true;
         this.setDirty.run();
      }

   }

   private void clear() {
      this.records.clear();
      this.byType.clear();
   }

   boolean isValid() {
      return this.isValid;
   }
}