package com.mojang.blaze3d.audio;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisAlloc;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

/**
 * An implementation of the {@link AudioStream} interface that reads Ogg Vorbis audio data from an {@link InputStream}.
 */
@OnlyIn(Dist.CLIENT)
public class OggAudioStream implements AudioStream {
   /** The expected maximum frame size in bytes. */
   private static final int EXPECTED_MAX_FRAME_SIZE = 8192;
   /** The handle for the Ogg Vorbis stream. */
   private long handle;
   /** The audio format of the Ogg Vorbis stream. */
   private final AudioFormat audioFormat;
   /** The input stream containing the Ogg Vorbis data. */
   private final InputStream input;
   /** The buffer used to read data from the input stream. */
   private ByteBuffer buffer = MemoryUtil.memAlloc(8192);

   public OggAudioStream(InputStream pInput) throws IOException {
      this.input = pInput;
      this.buffer.limit(0);

      try (MemoryStack memorystack = MemoryStack.stackPush()) {
         IntBuffer intbuffer = memorystack.mallocInt(1);
         IntBuffer intbuffer1 = memorystack.mallocInt(1);

         while(this.handle == 0L) {
            if (!this.refillFromStream()) {
               throw new IOException("Failed to find Ogg header");
            }

            int i = this.buffer.position();
            this.buffer.position(0);
            this.handle = STBVorbis.stb_vorbis_open_pushdata(this.buffer, intbuffer, intbuffer1, (STBVorbisAlloc)null);
            this.buffer.position(i);
            int j = intbuffer1.get(0);
            if (j == 1) {
               this.forwardBuffer();
            } else if (j != 0) {
               throw new IOException("Failed to read Ogg file " + j);
            }
         }

         this.buffer.position(this.buffer.position() + intbuffer.get(0));
         STBVorbisInfo stbvorbisinfo = STBVorbisInfo.mallocStack(memorystack);
         STBVorbis.stb_vorbis_get_info(this.handle, stbvorbisinfo);
         this.audioFormat = new AudioFormat((float)stbvorbisinfo.sample_rate(), 16, stbvorbisinfo.channels(), true, false);
      }

   }

   /**
    * Refills the buffer with data from the input stream.
    * @return {@code true} if the buffer was successfully refilled, {@code false} if the end of the input stream was
    * reached
    * @throws IOException if an I/O error occurs while reading the data from the input stream
    */
   private boolean refillFromStream() throws IOException {
      int i = this.buffer.limit();
      int j = this.buffer.capacity() - i;
      if (j == 0) {
         return true;
      } else {
         byte[] abyte = new byte[j];
         int k = this.input.read(abyte);
         if (k == -1) {
            return false;
         } else {
            int l = this.buffer.position();
            this.buffer.limit(i + k);
            this.buffer.position(i);
            this.buffer.put(abyte, 0, k);
            this.buffer.position(l);
            return true;
         }
      }
   }

   /**
    * Forwards the buffer to the next Ogg packet boundary.
    */
   private void forwardBuffer() {
      boolean flag = this.buffer.position() == 0;
      boolean flag1 = this.buffer.position() == this.buffer.limit();
      if (flag1 && !flag) {
         this.buffer.position(0);
         this.buffer.limit(0);
      } else {
         ByteBuffer bytebuffer = MemoryUtil.memAlloc(flag ? 2 * this.buffer.capacity() : this.buffer.capacity());
         bytebuffer.put(this.buffer);
         MemoryUtil.memFree(this.buffer);
         bytebuffer.flip();
         this.buffer = bytebuffer;
      }

   }

   /**
    * Reads the next audio frame from the Ogg Vorbis stream.
    * @return {@code true} if an audio frame was successfully read, {@code false} if the end of the stream was reached
    * @throws IOException if an I/O error occurs while reading the audio data
    * @param pOutput the output buffer to which the audio data should be written
    */
   private boolean readFrame(OggAudioStream.OutputConcat pOutput) throws IOException {
      if (this.handle == 0L) {
         return false;
      } else {
         try (MemoryStack memorystack = MemoryStack.stackPush()) {
            PointerBuffer pointerbuffer = memorystack.mallocPointer(1);
            IntBuffer intbuffer = memorystack.mallocInt(1);
            IntBuffer intbuffer1 = memorystack.mallocInt(1);

            while(true) {
               int i = STBVorbis.stb_vorbis_decode_frame_pushdata(this.handle, this.buffer, intbuffer, pointerbuffer, intbuffer1);
               this.buffer.position(this.buffer.position() + i);
               int j = STBVorbis.stb_vorbis_get_error(this.handle);
               if (j == 1) {
                  this.forwardBuffer();
                  if (!this.refillFromStream()) {
                     return false;
                  }
               } else {
                  if (j != 0) {
                     throw new IOException("Failed to read Ogg file " + j);
                  }

                  int k = intbuffer1.get(0);
                  if (k != 0) {
                     int l = intbuffer.get(0);
                     PointerBuffer pointerbuffer1 = pointerbuffer.getPointerBuffer(l);
                     if (l == 1) {
                        this.convertMono(pointerbuffer1.getFloatBuffer(0, k), pOutput);
                        return true;
                     } else if (l != 2) {
                        throw new IllegalStateException("Invalid number of channels: " + l);
                     } else {
                        this.convertStereo(pointerbuffer1.getFloatBuffer(0, k), pointerbuffer1.getFloatBuffer(1, k), pOutput);
                        return true;
                     }
                  }
               }
            }
         }
      }
   }

   /**
    * Converts mono audio data from the specified channel to a byte buffer and appends it to the specified output
    * buffer.
    * The method reads samples from the channel and adds them to the output buffer until there are no more samples
    * remaining in the channel.
    * @param pChannel the channel containing the mono audio data to convert
    * @param pOutput the output buffer to which the converted audio data should be appended
    */
   private void convertMono(FloatBuffer pChannel, OggAudioStream.OutputConcat pOutput) {
      while(pChannel.hasRemaining()) {
         pOutput.put(pChannel.get());
      }

   }

   /**
    * Converts stereo audio data from the specified left and right channels to a byte buffer and appends it to the
    * specified output buffer. The method reads samples from both channels and interleaves them in the output buffer
    * until there are no more samples remaining in either channel.
    * @param pLeftChannel the channel containing the left audio data to convert
    * @param pRightChannel the channel containing the right audio data to convert
    * @param pOutput the output buffer to which the converted audio data should be appended
    */
   private void convertStereo(FloatBuffer pLeftChannel, FloatBuffer pRightChannel, OggAudioStream.OutputConcat pOutput) {
      while(pLeftChannel.hasRemaining() && pRightChannel.hasRemaining()) {
         pOutput.put(pLeftChannel.get());
         pOutput.put(pRightChannel.get());
      }

   }

   public void close() throws IOException {
      if (this.handle != 0L) {
         STBVorbis.stb_vorbis_close(this.handle);
         this.handle = 0L;
      }

      MemoryUtil.memFree(this.buffer);
      this.input.close();
   }

   /**
    * {@return the {@linkplain AudioFormat} of the stream}
    */
   public AudioFormat getFormat() {
      return this.audioFormat;
   }

   /**
    * Reads audio data from the stream and returns a byte buffer containing at most the specified number of bytes.
    * The method reads audio frames from the stream and adds them to the output buffer until the buffer contains at
    * least the specified number of bytes or the end fo the stream is reached.
    * @return a byte buffer containing at most the specified number of bytes to read
    * @throws IOException if an I/O error occurs while reading the audio data
    * @param pSize the maximum number of bytes to read
    */
   public ByteBuffer read(int pSize) throws IOException {
      OggAudioStream.OutputConcat oggaudiostream$outputconcat = new OggAudioStream.OutputConcat(pSize + 8192);

      while(this.readFrame(oggaudiostream$outputconcat) && oggaudiostream$outputconcat.byteCount < pSize) {
      }

      return oggaudiostream$outputconcat.get();
   }

   /**
    * Reads all of the audio data from the stream and returns a byte buffer containing the entire audio data.
    * The method reads audio frames from the stream and adds them to the output buffer until the end of the stream is
    * reached.
    * @return a byte buffer containing the entire audio data
    * @throws IOException if an I/O error occurs while reading the audio data
    */
   public ByteBuffer readAll() throws IOException {
      OggAudioStream.OutputConcat oggaudiostream$outputconcat = new OggAudioStream.OutputConcat(16384);

      while(this.readFrame(oggaudiostream$outputconcat)) {
      }

      return oggaudiostream$outputconcat.get();
   }

   /**
    * An implementation of the {@link ByteBuffer} class that concatenates multiple byte buffers into a single buffer.
    * The class maintains a list of buffers and a byte count, and adds new data to the current buffer until it is full.
    * When the current buffer is full, it is added to the list and a new buffer is created. The `get()` method returns a
    * single byte buffer containing all of the data from the list of buffers.
    */
   @OnlyIn(Dist.CLIENT)
   static class OutputConcat {
      private final List<ByteBuffer> buffers = Lists.newArrayList();
      private final int bufferSize;
      int byteCount;
      private ByteBuffer currentBuffer;

      public OutputConcat(int pSize) {
         this.bufferSize = pSize + 1 & -2;
         this.createNewBuffer();
      }

      /**
       * Creates a new buffer and sets it as the current buffer.
       */
      private void createNewBuffer() {
         this.currentBuffer = BufferUtils.createByteBuffer(this.bufferSize);
      }

      /**
       * Adds a sample to the current buffer. If the buffer is full, the buffer is added to the list of buffers and a
       * new buffer is created.
       * @param pSample the audio sample to add to the buffer
       */
      public void put(float pSample) {
         if (this.currentBuffer.remaining() == 0) {
            this.currentBuffer.flip();
            this.buffers.add(this.currentBuffer);
            this.createNewBuffer();
         }

         int i = Mth.clamp((int)(pSample * 32767.5F - 0.5F), -32768, 32767);
         this.currentBuffer.putShort((short)i);
         this.byteCount += 2;
      }

      /**
       * {@return a single byte buffer containing all of the data from the list of buffers}
       */
      public ByteBuffer get() {
         this.currentBuffer.flip();
         if (this.buffers.isEmpty()) {
            return this.currentBuffer;
         } else {
            ByteBuffer bytebuffer = BufferUtils.createByteBuffer(this.byteCount);
            this.buffers.forEach(bytebuffer::put);
            bytebuffer.put(this.currentBuffer);
            bytebuffer.flip();
            return bytebuffer;
         }
      }
   }
}