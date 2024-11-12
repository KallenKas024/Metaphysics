package com.mojang.realmsclient.dto;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class UploadInfo extends ValueObject {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String DEFAULT_SCHEMA = "http://";
   private static final int DEFAULT_PORT = 8080;
   private static final Pattern URI_SCHEMA_PATTERN = Pattern.compile("^[a-zA-Z][-a-zA-Z0-9+.]+:");
   private final boolean worldClosed;
   @Nullable
   private final String token;
   private final URI uploadEndpoint;

   private UploadInfo(boolean pWorldClosed, @Nullable String pToken, URI pUploadEndpoint) {
      this.worldClosed = pWorldClosed;
      this.token = pToken;
      this.uploadEndpoint = pUploadEndpoint;
   }

   @Nullable
   public static UploadInfo parse(String pJson) {
      try {
         JsonParser jsonparser = new JsonParser();
         JsonObject jsonobject = jsonparser.parse(pJson).getAsJsonObject();
         String s = JsonUtils.getStringOr("uploadEndpoint", jsonobject, (String)null);
         if (s != null) {
            int i = JsonUtils.getIntOr("port", jsonobject, -1);
            URI uri = assembleUri(s, i);
            if (uri != null) {
               boolean flag = JsonUtils.getBooleanOr("worldClosed", jsonobject, false);
               String s1 = JsonUtils.getStringOr("token", jsonobject, (String)null);
               return new UploadInfo(flag, s1, uri);
            }
         }
      } catch (Exception exception) {
         LOGGER.error("Could not parse UploadInfo: {}", (Object)exception.getMessage());
      }

      return null;
   }

   @Nullable
   @VisibleForTesting
   public static URI assembleUri(String pUri, int pPort) {
      Matcher matcher = URI_SCHEMA_PATTERN.matcher(pUri);
      String s = ensureEndpointSchema(pUri, matcher);

      try {
         URI uri = new URI(s);
         int i = selectPortOrDefault(pPort, uri.getPort());
         return i != uri.getPort() ? new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), i, uri.getPath(), uri.getQuery(), uri.getFragment()) : uri;
      } catch (URISyntaxException urisyntaxexception) {
         LOGGER.warn("Failed to parse URI {}", s, urisyntaxexception);
         return null;
      }
   }

   private static int selectPortOrDefault(int pPort, int pDefaultPort) {
      if (pPort != -1) {
         return pPort;
      } else {
         return pDefaultPort != -1 ? pDefaultPort : 8080;
      }
   }

   private static String ensureEndpointSchema(String pUri, Matcher pMatcher) {
      return pMatcher.find() ? pUri : "http://" + pUri;
   }

   public static String createRequest(@Nullable String pToken) {
      JsonObject jsonobject = new JsonObject();
      if (pToken != null) {
         jsonobject.addProperty("token", pToken);
      }

      return jsonobject.toString();
   }

   @Nullable
   public String getToken() {
      return this.token;
   }

   public URI getUploadEndpoint() {
      return this.uploadEndpoint;
   }

   public boolean isWorldClosed() {
      return this.worldClosed;
   }
}