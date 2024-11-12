package net.minecraft.server;

import com.google.common.collect.Lists;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

public class ChainedJsonException extends IOException {
   private final List<ChainedJsonException.Entry> entries = Lists.newArrayList();
   private final String message;

   public ChainedJsonException(String pMessage) {
      this.entries.add(new ChainedJsonException.Entry());
      this.message = pMessage;
   }

   public ChainedJsonException(String pMessage, Throwable pCause) {
      super(pCause);
      this.entries.add(new ChainedJsonException.Entry());
      this.message = pMessage;
   }

   public void prependJsonKey(String pKey) {
      this.entries.get(0).addJsonKey(pKey);
   }

   public void setFilenameAndFlush(String pFilename) {
      (this.entries.get(0)).filename = pFilename;
      this.entries.add(0, new ChainedJsonException.Entry());
   }

   public String getMessage() {
      return "Invalid " + this.entries.get(this.entries.size() - 1) + ": " + this.message;
   }

   public static ChainedJsonException forException(Exception pException) {
      if (pException instanceof ChainedJsonException) {
         return (ChainedJsonException)pException;
      } else {
         String s = pException.getMessage();
         if (pException instanceof FileNotFoundException) {
            s = "File not found";
         }

         return new ChainedJsonException(s, pException);
      }
   }

   public static class Entry {
      @Nullable
      String filename;
      private final List<String> jsonKeys = Lists.newArrayList();

      Entry() {
      }

      void addJsonKey(String pKey) {
         this.jsonKeys.add(0, pKey);
      }

      @Nullable
      public String getFilename() {
         return this.filename;
      }

      public String getJsonKeys() {
         return StringUtils.join(this.jsonKeys, "->");
      }

      public String toString() {
         if (this.filename != null) {
            return this.jsonKeys.isEmpty() ? this.filename : this.filename + " " + this.getJsonKeys();
         } else {
            return this.jsonKeys.isEmpty() ? "(Unknown file)" : "(Unknown file) " + this.getJsonKeys();
         }
      }
   }
}