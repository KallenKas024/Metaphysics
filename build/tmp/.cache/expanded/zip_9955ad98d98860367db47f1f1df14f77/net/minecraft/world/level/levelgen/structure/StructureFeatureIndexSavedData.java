package net.minecraft.world.level.levelgen.structure;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class StructureFeatureIndexSavedData extends SavedData {
   private static final String TAG_REMAINING_INDEXES = "Remaining";
   private static final String TAG_All_INDEXES = "All";
   private final LongSet all;
   private final LongSet remaining;

   private StructureFeatureIndexSavedData(LongSet pAll, LongSet pRemaining) {
      this.all = pAll;
      this.remaining = pRemaining;
   }

   public StructureFeatureIndexSavedData() {
      this(new LongOpenHashSet(), new LongOpenHashSet());
   }

   public static StructureFeatureIndexSavedData load(CompoundTag pTag) {
      return new StructureFeatureIndexSavedData(new LongOpenHashSet(pTag.getLongArray("All")), new LongOpenHashSet(pTag.getLongArray("Remaining")));
   }

   /**
    * Used to save the {@code SavedData} to a {@code CompoundTag}
    * @param pCompoundTag the {@code CompoundTag} to save the {@code SavedData} to
    */
   public CompoundTag save(CompoundTag pCompoundTag) {
      pCompoundTag.putLongArray("All", this.all.toLongArray());
      pCompoundTag.putLongArray("Remaining", this.remaining.toLongArray());
      return pCompoundTag;
   }

   public void addIndex(long pIndex) {
      this.all.add(pIndex);
      this.remaining.add(pIndex);
   }

   public boolean hasStartIndex(long pIndex) {
      return this.all.contains(pIndex);
   }

   public boolean hasUnhandledIndex(long pIndex) {
      return this.remaining.contains(pIndex);
   }

   public void removeIndex(long pIndex) {
      this.remaining.remove(pIndex);
   }

   public LongSet getAll() {
      return this.all;
   }
}