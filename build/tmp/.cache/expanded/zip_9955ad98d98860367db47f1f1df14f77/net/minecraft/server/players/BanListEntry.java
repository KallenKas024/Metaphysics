package net.minecraft.server.players;

import com.google.gson.JsonObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;

public abstract class BanListEntry<T> extends StoredUserEntry<T> {
   public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
   public static final String EXPIRES_NEVER = "forever";
   protected final Date created;
   protected final String source;
   @Nullable
   protected final Date expires;
   protected final String reason;

   public BanListEntry(T pUser, @Nullable Date pCreated, @Nullable String pSource, @Nullable Date pExpires, @Nullable String pReason) {
      super(pUser);
      this.created = pCreated == null ? new Date() : pCreated;
      this.source = pSource == null ? "(Unknown)" : pSource;
      this.expires = pExpires;
      this.reason = pReason == null ? "Banned by an operator." : pReason;
   }

   protected BanListEntry(T pUser, JsonObject pEntryData) {
      super(pUser);

      Date date;
      try {
         date = pEntryData.has("created") ? DATE_FORMAT.parse(pEntryData.get("created").getAsString()) : new Date();
      } catch (ParseException parseexception1) {
         date = new Date();
      }

      this.created = date;
      this.source = pEntryData.has("source") ? pEntryData.get("source").getAsString() : "(Unknown)";

      Date date1;
      try {
         date1 = pEntryData.has("expires") ? DATE_FORMAT.parse(pEntryData.get("expires").getAsString()) : null;
      } catch (ParseException parseexception) {
         date1 = null;
      }

      this.expires = date1;
      this.reason = pEntryData.has("reason") ? pEntryData.get("reason").getAsString() : "Banned by an operator.";
   }

   public Date getCreated() {
      return this.created;
   }

   public String getSource() {
      return this.source;
   }

   @Nullable
   public Date getExpires() {
      return this.expires;
   }

   public String getReason() {
      return this.reason;
   }

   public abstract Component getDisplayName();

   boolean hasExpired() {
      return this.expires == null ? false : this.expires.before(new Date());
   }

   protected void serialize(JsonObject pData) {
      pData.addProperty("created", DATE_FORMAT.format(this.created));
      pData.addProperty("source", this.source);
      pData.addProperty("expires", this.expires == null ? "forever" : DATE_FORMAT.format(this.expires));
      pData.addProperty("reason", this.reason);
   }
}