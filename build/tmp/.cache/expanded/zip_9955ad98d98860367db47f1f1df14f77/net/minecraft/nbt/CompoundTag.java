package net.minecraft.nbt;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;

public class CompoundTag implements Tag {
   public static final Codec<CompoundTag> CODEC = Codec.PASSTHROUGH.comapFlatMap((p_274781_) -> {
      Tag tag = p_274781_.convert(NbtOps.INSTANCE).getValue();
      return tag instanceof CompoundTag ? DataResult.success((CompoundTag)tag) : DataResult.error(() -> {
         return "Not a compound tag: " + tag;
      });
   }, (p_128412_) -> {
      return new Dynamic<>(NbtOps.INSTANCE, p_128412_);
   });
   private static final int SELF_SIZE_IN_BYTES = 48;
   private static final int MAP_ENTRY_SIZE_IN_BYTES = 32;
   public static final TagType<CompoundTag> TYPE = new TagType.VariableSize<CompoundTag>() {
      public CompoundTag load(DataInput p_128485_, int p_128486_, NbtAccounter p_128487_) throws IOException {
         p_128487_.accountBytes(48L);
         if (p_128486_ > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
         } else {
            Map<String, Tag> map = Maps.newHashMap();

            byte b0;
            while((b0 = CompoundTag.readNamedTagType(p_128485_, p_128487_)) != 0) {
               String s = CompoundTag.readNamedTagName(p_128485_, p_128487_);
               p_128487_.accountBytes((long)(28 + 2 * s.length()));
               p_128487_.accountBytes(4); //Forge: 4 extra bytes for the object allocation.
               Tag tag = CompoundTag.readNamedTagData(TagTypes.getType(b0), s, p_128485_, p_128486_ + 1, p_128487_);
               if (map.put(s, tag) == null) {
                  p_128487_.accountBytes(36L);
               }
            }

            return new CompoundTag(map);
         }
      }

      public StreamTagVisitor.ValueResult parse(DataInput p_197446_, StreamTagVisitor p_197447_) throws IOException {
         while(true) {
            byte b0;
            if ((b0 = p_197446_.readByte()) != 0) {
               TagType<?> tagtype = TagTypes.getType(b0);
               switch (p_197447_.visitEntry(tagtype)) {
                  case HALT:
                     return StreamTagVisitor.ValueResult.HALT;
                  case BREAK:
                     StringTag.skipString(p_197446_);
                     tagtype.skip(p_197446_);
                     break;
                  case SKIP:
                     StringTag.skipString(p_197446_);
                     tagtype.skip(p_197446_);
                     continue;
                  default:
                     String s = p_197446_.readUTF();
                     switch (p_197447_.visitEntry(tagtype, s)) {
                        case HALT:
                           return StreamTagVisitor.ValueResult.HALT;
                        case BREAK:
                           tagtype.skip(p_197446_);
                           break;
                        case SKIP:
                           tagtype.skip(p_197446_);
                           continue;
                        default:
                           switch (tagtype.parse(p_197446_, p_197447_)) {
                              case HALT:
                                 return StreamTagVisitor.ValueResult.HALT;
                              case BREAK:
                              default:
                                 continue;
                           }
                     }
               }
            }

            if (b0 != 0) {
               while((b0 = p_197446_.readByte()) != 0) {
                  StringTag.skipString(p_197446_);
                  TagTypes.getType(b0).skip(p_197446_);
               }
            }

            return p_197447_.visitContainerEnd();
         }
      }

      public void skip(DataInput p_197444_) throws IOException {
         byte b0;
         while((b0 = p_197444_.readByte()) != 0) {
            StringTag.skipString(p_197444_);
            TagTypes.getType(b0).skip(p_197444_);
         }

      }

      public String getName() {
         return "COMPOUND";
      }

      public String getPrettyName() {
         return "TAG_Compound";
      }
   };
   private final Map<String, Tag> tags;

   protected CompoundTag(Map<String, Tag> pTags) {
      this.tags = pTags;
   }

   public CompoundTag() {
      this(Maps.newHashMap());
   }

   public void write(DataOutput pOutput) throws IOException {
      for(String s : this.tags.keySet()) {
         Tag tag = this.tags.get(s);
         writeNamedTag(s, tag, pOutput);
      }

      pOutput.writeByte(0);
   }

   public int sizeInBytes() {
      int i = 48;

      for(Map.Entry<String, Tag> entry : this.tags.entrySet()) {
         i += 28 + 2 * entry.getKey().length();
         i += 36;
         i += entry.getValue().sizeInBytes();
      }

      return i;
   }

   public Set<String> getAllKeys() {
      return this.tags.keySet();
   }

   public byte getId() {
      return 10;
   }

   public TagType<CompoundTag> getType() {
      return TYPE;
   }

   public int size() {
      return this.tags.size();
   }

   @Nullable
   public Tag put(String pKey, Tag pValue) {
      if (pValue == null) throw new IllegalArgumentException("Invalid null NBT value with key " + pKey);
      return this.tags.put(pKey, pValue);
   }

   public void putByte(String pKey, byte pValue) {
      this.tags.put(pKey, ByteTag.valueOf(pValue));
   }

   public void putShort(String pKey, short pValue) {
      this.tags.put(pKey, ShortTag.valueOf(pValue));
   }

   public void putInt(String pKey, int pValue) {
      this.tags.put(pKey, IntTag.valueOf(pValue));
   }

   public void putLong(String pKey, long pValue) {
      this.tags.put(pKey, LongTag.valueOf(pValue));
   }

   public void putUUID(String pKey, UUID pValue) {
      this.tags.put(pKey, NbtUtils.createUUID(pValue));
   }

   public UUID getUUID(String pKey) {
      return NbtUtils.loadUUID(this.get(pKey));
   }

   public boolean hasUUID(String pKey) {
      Tag tag = this.get(pKey);
      return tag != null && tag.getType() == IntArrayTag.TYPE && ((IntArrayTag)tag).getAsIntArray().length == 4;
   }

   public void putFloat(String pKey, float pValue) {
      this.tags.put(pKey, FloatTag.valueOf(pValue));
   }

   public void putDouble(String pKey, double pValue) {
      this.tags.put(pKey, DoubleTag.valueOf(pValue));
   }

   public void putString(String pKey, String pValue) {
      this.tags.put(pKey, StringTag.valueOf(pValue));
   }

   public void putByteArray(String pKey, byte[] pValue) {
      this.tags.put(pKey, new ByteArrayTag(pValue));
   }

   public void putByteArray(String pKey, List<Byte> pValue) {
      this.tags.put(pKey, new ByteArrayTag(pValue));
   }

   public void putIntArray(String pKey, int[] pValue) {
      this.tags.put(pKey, new IntArrayTag(pValue));
   }

   public void putIntArray(String pKey, List<Integer> pValue) {
      this.tags.put(pKey, new IntArrayTag(pValue));
   }

   public void putLongArray(String pKey, long[] pValue) {
      this.tags.put(pKey, new LongArrayTag(pValue));
   }

   public void putLongArray(String pKey, List<Long> pValue) {
      this.tags.put(pKey, new LongArrayTag(pValue));
   }

   public void putBoolean(String pKey, boolean pValue) {
      this.tags.put(pKey, ByteTag.valueOf(pValue));
   }

   @Nullable
   public Tag get(String pKey) {
      return this.tags.get(pKey);
   }

   /**
    * Gets the byte identifier of the tag of the specified {@code key}, or {@code 0} if no tag exists for the {@code
    * key}.
    */
   public byte getTagType(String pKey) {
      Tag tag = this.tags.get(pKey);
      return tag == null ? 0 : tag.getId();
   }

   public boolean contains(String pKey) {
      return this.tags.containsKey(pKey);
   }

   /**
    * Returns whether the tag of the specified {@code key} is a particular {@code tagType}. If the {@code tagType} is
    * {@code 99}, all numeric tags will be checked against the type of the stored tag.
    */
   public boolean contains(String pKey, int pTagType) {
      int i = this.getTagType(pKey);
      if (i == pTagType) {
         return true;
      } else if (pTagType != 99) {
         return false;
      } else {
         return i == 1 || i == 2 || i == 3 || i == 4 || i == 5 || i == 6;
      }
   }

   public byte getByte(String pKey) {
      try {
         if (this.contains(pKey, 99)) {
            return ((NumericTag)this.tags.get(pKey)).getAsByte();
         }
      } catch (ClassCastException classcastexception) {
      }

      return 0;
   }

   public short getShort(String pKey) {
      try {
         if (this.contains(pKey, 99)) {
            return ((NumericTag)this.tags.get(pKey)).getAsShort();
         }
      } catch (ClassCastException classcastexception) {
      }

      return 0;
   }

   public int getInt(String pKey) {
      try {
         if (this.contains(pKey, 99)) {
            return ((NumericTag)this.tags.get(pKey)).getAsInt();
         }
      } catch (ClassCastException classcastexception) {
      }

      return 0;
   }

   public long getLong(String pKey) {
      try {
         if (this.contains(pKey, 99)) {
            return ((NumericTag)this.tags.get(pKey)).getAsLong();
         }
      } catch (ClassCastException classcastexception) {
      }

      return 0L;
   }

   public float getFloat(String pKey) {
      try {
         if (this.contains(pKey, 99)) {
            return ((NumericTag)this.tags.get(pKey)).getAsFloat();
         }
      } catch (ClassCastException classcastexception) {
      }

      return 0.0F;
   }

   public double getDouble(String pKey) {
      try {
         if (this.contains(pKey, 99)) {
            return ((NumericTag)this.tags.get(pKey)).getAsDouble();
         }
      } catch (ClassCastException classcastexception) {
      }

      return 0.0D;
   }

   public String getString(String pKey) {
      try {
         if (this.contains(pKey, 8)) {
            return this.tags.get(pKey).getAsString();
         }
      } catch (ClassCastException classcastexception) {
      }

      return "";
   }

   public byte[] getByteArray(String pKey) {
      try {
         if (this.contains(pKey, 7)) {
            return ((ByteArrayTag)this.tags.get(pKey)).getAsByteArray();
         }
      } catch (ClassCastException classcastexception) {
         throw new ReportedException(this.createReport(pKey, ByteArrayTag.TYPE, classcastexception));
      }

      return new byte[0];
   }

   public int[] getIntArray(String pKey) {
      try {
         if (this.contains(pKey, 11)) {
            return ((IntArrayTag)this.tags.get(pKey)).getAsIntArray();
         }
      } catch (ClassCastException classcastexception) {
         throw new ReportedException(this.createReport(pKey, IntArrayTag.TYPE, classcastexception));
      }

      return new int[0];
   }

   public long[] getLongArray(String pKey) {
      try {
         if (this.contains(pKey, 12)) {
            return ((LongArrayTag)this.tags.get(pKey)).getAsLongArray();
         }
      } catch (ClassCastException classcastexception) {
         throw new ReportedException(this.createReport(pKey, LongArrayTag.TYPE, classcastexception));
      }

      return new long[0];
   }

   public CompoundTag getCompound(String pKey) {
      try {
         if (this.contains(pKey, 10)) {
            return (CompoundTag)this.tags.get(pKey);
         }
      } catch (ClassCastException classcastexception) {
         throw new ReportedException(this.createReport(pKey, TYPE, classcastexception));
      }

      return new CompoundTag();
   }

   public ListTag getList(String pKey, int pTagType) {
      try {
         if (this.getTagType(pKey) == 9) {
            ListTag listtag = (ListTag)this.tags.get(pKey);
            if (!listtag.isEmpty() && listtag.getElementType() != pTagType) {
               return new ListTag();
            }

            return listtag;
         }
      } catch (ClassCastException classcastexception) {
         throw new ReportedException(this.createReport(pKey, ListTag.TYPE, classcastexception));
      }

      return new ListTag();
   }

   public boolean getBoolean(String pKey) {
      return this.getByte(pKey) != 0;
   }

   public void remove(String pKey) {
      this.tags.remove(pKey);
   }

   public String toString() {
      return this.getAsString();
   }

   public boolean isEmpty() {
      return this.tags.isEmpty();
   }

   private CrashReport createReport(String pTagName, TagType<?> pType, ClassCastException pException) {
      CrashReport crashreport = CrashReport.forThrowable(pException, "Reading NBT data");
      CrashReportCategory crashreportcategory = crashreport.addCategory("Corrupt NBT tag", 1);
      crashreportcategory.setDetail("Tag type found", () -> {
         return this.tags.get(pTagName).getType().getName();
      });
      crashreportcategory.setDetail("Tag type expected", pType::getName);
      crashreportcategory.setDetail("Tag name", pTagName);
      return crashreport;
   }

   /**
    * Creates a deep copy of the value held by this tag. Primitive and string tage will return the same tag instance
    * while all other objects will return a new tag instance with the copied data.
    */
   public CompoundTag copy() {
      Map<String, Tag> map = Maps.newHashMap(Maps.transformValues(this.tags, Tag::copy));
      return new CompoundTag(map);
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else {
         return pOther instanceof CompoundTag && Objects.equals(this.tags, ((CompoundTag)pOther).tags);
      }
   }

   public int hashCode() {
      return this.tags.hashCode();
   }

   private static void writeNamedTag(String pName, Tag pTag, DataOutput pOutput) throws IOException {
      pOutput.writeByte(pTag.getId());
      if (pTag.getId() != 0) {
         pOutput.writeUTF(pName);
         pTag.write(pOutput);
      }
   }

   static byte readNamedTagType(DataInput pInput, NbtAccounter pAccounter) throws IOException {
      pAccounter.accountBytes(2);
      return pInput.readByte();
   }

   static String readNamedTagName(DataInput pInput, NbtAccounter pAccounter) throws IOException {
      return pAccounter.readUTF(pInput.readUTF());
   }

   static Tag readNamedTagData(TagType<?> pType, String pName, DataInput pInput, int pDepth, NbtAccounter pAccounter) {
      try {
         return pType.load(pInput, pDepth, pAccounter);
      } catch (IOException ioexception) {
         CrashReport crashreport = CrashReport.forThrowable(ioexception, "Loading NBT data");
         CrashReportCategory crashreportcategory = crashreport.addCategory("NBT Tag");
         crashreportcategory.setDetail("Tag name", pName);
         crashreportcategory.setDetail("Tag type", pType.getName());
         throw new ReportedException(crashreport);
      }
   }

   /**
    * Copies all the tags of {@code other} into this tag, then returns itself.
    * @see #copy()
    */
   public CompoundTag merge(CompoundTag pOther) {
      for(String s : pOther.tags.keySet()) {
         Tag tag = pOther.tags.get(s);
         if (tag.getId() == 10) {
            if (this.contains(s, 10)) {
               CompoundTag compoundtag = this.getCompound(s);
               compoundtag.merge((CompoundTag)tag);
            } else {
               this.put(s, tag.copy());
            }
         } else {
            this.put(s, tag.copy());
         }
      }

      return this;
   }

   public void accept(TagVisitor pVisitor) {
      pVisitor.visitCompound(this);
   }

   protected Map<String, Tag> entries() {
      return Collections.unmodifiableMap(this.tags);
   }

   public StreamTagVisitor.ValueResult accept(StreamTagVisitor pVisitor) {
      for(Map.Entry<String, Tag> entry : this.tags.entrySet()) {
         Tag tag = entry.getValue();
         TagType<?> tagtype = tag.getType();
         StreamTagVisitor.EntryResult streamtagvisitor$entryresult = pVisitor.visitEntry(tagtype);
         switch (streamtagvisitor$entryresult) {
            case HALT:
               return StreamTagVisitor.ValueResult.HALT;
            case BREAK:
               return pVisitor.visitContainerEnd();
            case SKIP:
               break;
            default:
               streamtagvisitor$entryresult = pVisitor.visitEntry(tagtype, entry.getKey());
               switch (streamtagvisitor$entryresult) {
                  case HALT:
                     return StreamTagVisitor.ValueResult.HALT;
                  case BREAK:
                     return pVisitor.visitContainerEnd();
                  case SKIP:
                     break;
                  default:
                     StreamTagVisitor.ValueResult streamtagvisitor$valueresult = tag.accept(pVisitor);
                     switch (streamtagvisitor$valueresult) {
                        case HALT:
                           return StreamTagVisitor.ValueResult.HALT;
                        case BREAK:
                           return pVisitor.visitContainerEnd();
                     }
               }
         }
      }

      return pVisitor.visitContainerEnd();
   }
}
