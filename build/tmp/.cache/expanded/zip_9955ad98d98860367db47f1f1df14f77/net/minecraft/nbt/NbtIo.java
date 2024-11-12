package net.minecraft.nbt;

import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.util.FastBufferedInputStream;

public class NbtIo {
   public static CompoundTag readCompressed(File pFile) throws IOException {
      try (InputStream inputstream = new FileInputStream(pFile)) {
         return readCompressed(inputstream);
      }
   }

   private static DataInputStream createDecompressorStream(InputStream pZippedStream) throws IOException {
      return new DataInputStream(new FastBufferedInputStream(new GZIPInputStream(pZippedStream)));
   }

   /**
    * Reads a compressed compound tag from a GNU zipped file.
    * @see #readCompressed(File)
    */
   public static CompoundTag readCompressed(InputStream pZippedStream) throws IOException {
      try (DataInputStream datainputstream = createDecompressorStream(pZippedStream)) {
         return read(datainputstream, NbtAccounter.UNLIMITED);
      }
   }

   public static void parseCompressed(File pFile, StreamTagVisitor pVisitor) throws IOException {
      try (InputStream inputstream = new FileInputStream(pFile)) {
         parseCompressed(inputstream, pVisitor);
      }

   }

   public static void parseCompressed(InputStream pZippedStream, StreamTagVisitor pVisitor) throws IOException {
      try (DataInputStream datainputstream = createDecompressorStream(pZippedStream)) {
         parse(datainputstream, pVisitor);
      }

   }

   public static void writeCompressed(CompoundTag pCompoundTag, File pFile) throws IOException {
      try (OutputStream outputstream = new FileOutputStream(pFile)) {
         writeCompressed(pCompoundTag, outputstream);
      }

   }

   /**
    * Writes and compresses a compound tag to a GNU zipped file.
    * @see #writeCompressed(CompoundTag, File)
    */
   public static void writeCompressed(CompoundTag pCompoundTag, OutputStream pOutputStream) throws IOException {
      try (DataOutputStream dataoutputstream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(pOutputStream)))) {
         write(pCompoundTag, dataoutputstream);
      }

   }

   public static void write(CompoundTag pCompoundTag, File pFile) throws IOException {
      try (
         FileOutputStream fileoutputstream = new FileOutputStream(pFile);
         DataOutputStream dataoutputstream = new DataOutputStream(fileoutputstream);
      ) {
         write(pCompoundTag, dataoutputstream);
      }

   }

   @Nullable
   public static CompoundTag read(File pFile) throws IOException {
      if (!pFile.exists()) {
         return null;
      } else {
         CompoundTag compoundtag;
         try (
            FileInputStream fileinputstream = new FileInputStream(pFile);
            DataInputStream datainputstream = new DataInputStream(fileinputstream);
         ) {
            compoundtag = read(datainputstream, NbtAccounter.UNLIMITED);
         }

         return compoundtag;
      }
   }

   /**
    * Reads a compound tag from a file. The size of the file can be infinite.
    */
   public static CompoundTag read(DataInput pInput) throws IOException {
      return read(pInput, NbtAccounter.UNLIMITED);
   }

   /**
    * Reads a compound tag from a file. The size of the file is limited by the {@code accounter}.
    * @throws RuntimeException if the size of the file is larger than the maximum amount of bytes specified by the
    * {@code accounter}
    */
   public static CompoundTag read(DataInput pInput, NbtAccounter pAccounter) throws IOException {
      Tag tag = readUnnamedTag(pInput, 0, pAccounter);
      if (tag instanceof CompoundTag) {
         return (CompoundTag)tag;
      } else {
         throw new IOException("Root tag must be a named compound tag");
      }
   }

   public static void write(CompoundTag pCompoundTag, DataOutput pOutput) throws IOException {
      writeUnnamedTag(pCompoundTag, pOutput);
   }

   public static void parse(DataInput pInput, StreamTagVisitor pVisitor) throws IOException {
      TagType<?> tagtype = TagTypes.getType(pInput.readByte());
      if (tagtype == EndTag.TYPE) {
         if (pVisitor.visitRootEntry(EndTag.TYPE) == StreamTagVisitor.ValueResult.CONTINUE) {
            pVisitor.visitEnd();
         }

      } else {
         switch (pVisitor.visitRootEntry(tagtype)) {
            case HALT:
            default:
               break;
            case BREAK:
               StringTag.skipString(pInput);
               tagtype.skip(pInput);
               break;
            case CONTINUE:
               StringTag.skipString(pInput);
               tagtype.parse(pInput, pVisitor);
         }

      }
   }

   public static void writeUnnamedTag(Tag pTag, DataOutput pOutput) throws IOException {
      pOutput.writeByte(pTag.getId());
      if (pTag.getId() != 0) {
         pOutput.writeUTF("");
         pTag.write(pOutput);
      }
   }

   private static Tag readUnnamedTag(DataInput pInput, int pDepth, NbtAccounter pAccounter) throws IOException {
      byte b0 = pInput.readByte();
      pAccounter.accountBytes(1); // Forge: Count everything!
      if (b0 == 0) {
         return EndTag.INSTANCE;
      } else {
         pAccounter.readUTF(pInput.readUTF()); //Forge: Count this string.
         pAccounter.accountBytes(4); //Forge: 4 extra bytes for the object allocation.

         try {
            return TagTypes.getType(b0).load(pInput, pDepth, pAccounter);
         } catch (IOException ioexception) {
            CrashReport crashreport = CrashReport.forThrowable(ioexception, "Loading NBT data");
            CrashReportCategory crashreportcategory = crashreport.addCategory("NBT Tag");
            crashreportcategory.setDetail("Tag type", b0);
            throw new ReportedException(crashreport);
         }
      }
   }
}
