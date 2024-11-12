package net.minecraft.world.level.saveddata.maps;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

public class MapFrame {
   private final BlockPos pos;
   private final int rotation;
   private final int entityId;

   public MapFrame(BlockPos pPos, int pRotation, int pEntityId) {
      this.pos = pPos;
      this.rotation = pRotation;
      this.entityId = pEntityId;
   }

   public static MapFrame load(CompoundTag pCompoundTag) {
      BlockPos blockpos = NbtUtils.readBlockPos(pCompoundTag.getCompound("Pos"));
      int i = pCompoundTag.getInt("Rotation");
      int j = pCompoundTag.getInt("EntityId");
      return new MapFrame(blockpos, i, j);
   }

   public CompoundTag save() {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.put("Pos", NbtUtils.writeBlockPos(this.pos));
      compoundtag.putInt("Rotation", this.rotation);
      compoundtag.putInt("EntityId", this.entityId);
      return compoundtag;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public int getRotation() {
      return this.rotation;
   }

   public int getEntityId() {
      return this.entityId;
   }

   public String getId() {
      return frameId(this.pos);
   }

   public static String frameId(BlockPos pPos) {
      return "frame-" + pPos.getX() + "," + pPos.getY() + "," + pPos.getZ();
   }
}