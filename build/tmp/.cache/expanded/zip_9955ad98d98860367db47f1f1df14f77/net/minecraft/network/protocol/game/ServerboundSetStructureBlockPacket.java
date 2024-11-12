package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;

public class ServerboundSetStructureBlockPacket implements Packet<ServerGamePacketListener> {
   private static final int FLAG_IGNORE_ENTITIES = 1;
   private static final int FLAG_SHOW_AIR = 2;
   private static final int FLAG_SHOW_BOUNDING_BOX = 4;
   private final BlockPos pos;
   private final StructureBlockEntity.UpdateType updateType;
   private final StructureMode mode;
   private final String name;
   private final BlockPos offset;
   private final Vec3i size;
   private final Mirror mirror;
   private final Rotation rotation;
   private final String data;
   private final boolean ignoreEntities;
   private final boolean showAir;
   private final boolean showBoundingBox;
   private final float integrity;
   private final long seed;

   public ServerboundSetStructureBlockPacket(BlockPos pPos, StructureBlockEntity.UpdateType pUpdateType, StructureMode pMode, String pName, BlockPos pOffset, Vec3i pSize, Mirror pMirror, Rotation pRotation, String pData, boolean pIgnoreEntities, boolean pShowAir, boolean pShowBoundingBox, float pIntegrity, long pSeed) {
      this.pos = pPos;
      this.updateType = pUpdateType;
      this.mode = pMode;
      this.name = pName;
      this.offset = pOffset;
      this.size = pSize;
      this.mirror = pMirror;
      this.rotation = pRotation;
      this.data = pData;
      this.ignoreEntities = pIgnoreEntities;
      this.showAir = pShowAir;
      this.showBoundingBox = pShowBoundingBox;
      this.integrity = pIntegrity;
      this.seed = pSeed;
   }

   public ServerboundSetStructureBlockPacket(FriendlyByteBuf pBuffer) {
      this.pos = pBuffer.readBlockPos();
      this.updateType = pBuffer.readEnum(StructureBlockEntity.UpdateType.class);
      this.mode = pBuffer.readEnum(StructureMode.class);
      this.name = pBuffer.readUtf();
      int i = 48;
      this.offset = new BlockPos(Mth.clamp(pBuffer.readByte(), -48, 48), Mth.clamp(pBuffer.readByte(), -48, 48), Mth.clamp(pBuffer.readByte(), -48, 48));
      int j = 48;
      this.size = new Vec3i(Mth.clamp(pBuffer.readByte(), 0, 48), Mth.clamp(pBuffer.readByte(), 0, 48), Mth.clamp(pBuffer.readByte(), 0, 48));
      this.mirror = pBuffer.readEnum(Mirror.class);
      this.rotation = pBuffer.readEnum(Rotation.class);
      this.data = pBuffer.readUtf(128);
      this.integrity = Mth.clamp(pBuffer.readFloat(), 0.0F, 1.0F);
      this.seed = pBuffer.readVarLong();
      int k = pBuffer.readByte();
      this.ignoreEntities = (k & 1) != 0;
      this.showAir = (k & 2) != 0;
      this.showBoundingBox = (k & 4) != 0;
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeEnum(this.updateType);
      pBuffer.writeEnum(this.mode);
      pBuffer.writeUtf(this.name);
      pBuffer.writeByte(this.offset.getX());
      pBuffer.writeByte(this.offset.getY());
      pBuffer.writeByte(this.offset.getZ());
      pBuffer.writeByte(this.size.getX());
      pBuffer.writeByte(this.size.getY());
      pBuffer.writeByte(this.size.getZ());
      pBuffer.writeEnum(this.mirror);
      pBuffer.writeEnum(this.rotation);
      pBuffer.writeUtf(this.data);
      pBuffer.writeFloat(this.integrity);
      pBuffer.writeVarLong(this.seed);
      int i = 0;
      if (this.ignoreEntities) {
         i |= 1;
      }

      if (this.showAir) {
         i |= 2;
      }

      if (this.showBoundingBox) {
         i |= 4;
      }

      pBuffer.writeByte(i);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleSetStructureBlock(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public StructureBlockEntity.UpdateType getUpdateType() {
      return this.updateType;
   }

   public StructureMode getMode() {
      return this.mode;
   }

   public String getName() {
      return this.name;
   }

   public BlockPos getOffset() {
      return this.offset;
   }

   public Vec3i getSize() {
      return this.size;
   }

   public Mirror getMirror() {
      return this.mirror;
   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public String getData() {
      return this.data;
   }

   public boolean isIgnoreEntities() {
      return this.ignoreEntities;
   }

   public boolean isShowAir() {
      return this.showAir;
   }

   public boolean isShowBoundingBox() {
      return this.showBoundingBox;
   }

   public float getIntegrity() {
      return this.integrity;
   }

   public long getSeed() {
      return this.seed;
   }
}