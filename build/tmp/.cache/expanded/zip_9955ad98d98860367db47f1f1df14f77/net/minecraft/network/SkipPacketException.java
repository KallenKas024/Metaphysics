package net.minecraft.network;

import io.netty.handler.codec.EncoderException;

/**
 * Used to signify that a packet encoding error is not fatal. If a packet fails to encode, but {@link
 * net.minecraft.network.protocol.Packet#isSkippable} returns true, then this exception is thrown instead and {@link
 * Connection} will log a message instead of closing the connection.
 */
public class SkipPacketException extends EncoderException {
   public SkipPacketException(Throwable pCause) {
      super(pCause);
   }
}