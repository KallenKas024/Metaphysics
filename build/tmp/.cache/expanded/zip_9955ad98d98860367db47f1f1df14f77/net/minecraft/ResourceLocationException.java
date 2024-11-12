package net.minecraft;

import org.apache.commons.lang3.StringEscapeUtils;

public class ResourceLocationException extends RuntimeException {
   public ResourceLocationException(String pMessage) {
      super(StringEscapeUtils.escapeJava(pMessage));
   }

   public ResourceLocationException(String pMessage, Throwable pCause) {
      super(StringEscapeUtils.escapeJava(pMessage), pCause);
   }
}