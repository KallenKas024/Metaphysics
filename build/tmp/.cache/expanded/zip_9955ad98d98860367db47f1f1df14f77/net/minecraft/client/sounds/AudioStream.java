package net.minecraft.client.sounds;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.sound.sampled.AudioFormat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface AudioStream extends Closeable {
   /**
    * {@return the {@linkplain AudioFormat} of the stream}
    */
   AudioFormat getFormat();

   /**
    * Reads audio data from the stream and returns a byte buffer containing at most the specified number of bytes.
    * The method reads audio frames from the stream and adds them to the output buffer until the buffer contains at
    * least the specified number of bytes or the end fo the stream is reached.
    * @return a byte buffer containing at most the specified number of bytes to read
    * @throws IOException if an I/O error occurs while reading the audio data
    * @param pSize the maximum number of bytes to read
    */
   ByteBuffer read(int pSize) throws IOException;
}