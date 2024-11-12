package net.minecraft.util;

import java.io.IOException;
import java.io.InputStream;

public class FastBufferedInputStream extends InputStream {
   private static final int DEFAULT_BUFFER_SIZE = 8192;
   private final InputStream in;
   private final byte[] buffer;
   private int limit;
   private int position;

   public FastBufferedInputStream(InputStream pIn) {
      this(pIn, 8192);
   }

   public FastBufferedInputStream(InputStream pIn, int pBufferSize) {
      this.in = pIn;
      this.buffer = new byte[pBufferSize];
   }

   public int read() throws IOException {
      if (this.position >= this.limit) {
         this.fill();
         if (this.position >= this.limit) {
            return -1;
         }
      }

      return Byte.toUnsignedInt(this.buffer[this.position++]);
   }

   public int read(byte[] pBuffer, int pOffset, int pLength) throws IOException {
      int i = this.bytesInBuffer();
      if (i <= 0) {
         if (pLength >= this.buffer.length) {
            return this.in.read(pBuffer, pOffset, pLength);
         }

         this.fill();
         i = this.bytesInBuffer();
         if (i <= 0) {
            return -1;
         }
      }

      if (pLength > i) {
         pLength = i;
      }

      System.arraycopy(this.buffer, this.position, pBuffer, pOffset, pLength);
      this.position += pLength;
      return pLength;
   }

   public long skip(long pAmount) throws IOException {
      if (pAmount <= 0L) {
         return 0L;
      } else {
         long i = (long)this.bytesInBuffer();
         if (i <= 0L) {
            return this.in.skip(pAmount);
         } else {
            if (pAmount > i) {
               pAmount = i;
            }

            this.position = (int)((long)this.position + pAmount);
            return pAmount;
         }
      }
   }

   public int available() throws IOException {
      return this.bytesInBuffer() + this.in.available();
   }

   public void close() throws IOException {
      this.in.close();
   }

   private int bytesInBuffer() {
      return this.limit - this.position;
   }

   private void fill() throws IOException {
      this.limit = 0;
      this.position = 0;
      int i = this.in.read(this.buffer, 0, this.buffer.length);
      if (i > 0) {
         this.limit = i;
      }

   }
}