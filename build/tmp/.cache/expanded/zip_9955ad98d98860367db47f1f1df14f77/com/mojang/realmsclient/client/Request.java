package com.mojang.realmsclient.client;

import com.mojang.realmsclient.exception.RealmsHttpException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class Request<T extends Request<T>> {
   protected HttpURLConnection connection;
   private boolean connected;
   protected String url;
   private static final int DEFAULT_READ_TIMEOUT = 60000;
   private static final int DEFAULT_CONNECT_TIMEOUT = 5000;

   public Request(String pUrl, int pConnectTimeout, int pReadTimeout) {
      try {
         this.url = pUrl;
         Proxy proxy = RealmsClientConfig.getProxy();
         if (proxy != null) {
            this.connection = (HttpURLConnection)(new URL(pUrl)).openConnection(proxy);
         } else {
            this.connection = (HttpURLConnection)(new URL(pUrl)).openConnection();
         }

         this.connection.setConnectTimeout(pConnectTimeout);
         this.connection.setReadTimeout(pReadTimeout);
      } catch (MalformedURLException malformedurlexception) {
         throw new RealmsHttpException(malformedurlexception.getMessage(), malformedurlexception);
      } catch (IOException ioexception) {
         throw new RealmsHttpException(ioexception.getMessage(), ioexception);
      }
   }

   public void cookie(String pKey, String pValue) {
      cookie(this.connection, pKey, pValue);
   }

   public static void cookie(HttpURLConnection pConnection, String pKey, String pValue) {
      String s = pConnection.getRequestProperty("Cookie");
      if (s == null) {
         pConnection.setRequestProperty("Cookie", pKey + "=" + pValue);
      } else {
         pConnection.setRequestProperty("Cookie", s + ";" + pKey + "=" + pValue);
      }

   }

   public T header(String pKey, String pValue) {
      this.connection.addRequestProperty(pKey, pValue);
      return (T)this;
   }

   public int getRetryAfterHeader() {
      return getRetryAfterHeader(this.connection);
   }

   public static int getRetryAfterHeader(HttpURLConnection pConnection) {
      String s = pConnection.getHeaderField("Retry-After");

      try {
         return Integer.valueOf(s);
      } catch (Exception exception) {
         return 5;
      }
   }

   public int responseCode() {
      try {
         this.connect();
         return this.connection.getResponseCode();
      } catch (Exception exception) {
         throw new RealmsHttpException(exception.getMessage(), exception);
      }
   }

   public String text() {
      try {
         this.connect();
         String s;
         if (this.responseCode() >= 400) {
            s = this.read(this.connection.getErrorStream());
         } else {
            s = this.read(this.connection.getInputStream());
         }

         this.dispose();
         return s;
      } catch (IOException ioexception) {
         throw new RealmsHttpException(ioexception.getMessage(), ioexception);
      }
   }

   private String read(@Nullable InputStream pIn) throws IOException {
      if (pIn == null) {
         return "";
      } else {
         InputStreamReader inputstreamreader = new InputStreamReader(pIn, StandardCharsets.UTF_8);
         StringBuilder stringbuilder = new StringBuilder();

         for(int i = inputstreamreader.read(); i != -1; i = inputstreamreader.read()) {
            stringbuilder.append((char)i);
         }

         return stringbuilder.toString();
      }
   }

   private void dispose() {
      byte[] abyte = new byte[1024];

      try {
         InputStream inputstream = this.connection.getInputStream();

         while(inputstream.read(abyte) > 0) {
         }

         inputstream.close();
         return;
      } catch (Exception exception) {
         try {
            InputStream inputstream1 = this.connection.getErrorStream();
            if (inputstream1 != null) {
               while(inputstream1.read(abyte) > 0) {
               }

               inputstream1.close();
               return;
            }
         } catch (IOException ioexception) {
            return;
         }
      } finally {
         if (this.connection != null) {
            this.connection.disconnect();
         }

      }

   }

   protected T connect() {
      if (this.connected) {
         return (T)this;
      } else {
         T t = this.doConnect();
         this.connected = true;
         return t;
      }
   }

   protected abstract T doConnect();

   public static Request<?> get(String pUrl) {
      return new Request.Get(pUrl, 5000, 60000);
   }

   public static Request<?> get(String pUrl, int pConnectTimeout, int pReadTimeout) {
      return new Request.Get(pUrl, pConnectTimeout, pReadTimeout);
   }

   public static Request<?> post(String pUrl, String pContent) {
      return new Request.Post(pUrl, pContent, 5000, 60000);
   }

   public static Request<?> post(String pUrl, String pContent, int pConnectTimeout, int pReadTimeout) {
      return new Request.Post(pUrl, pContent, pConnectTimeout, pReadTimeout);
   }

   public static Request<?> delete(String pUrl) {
      return new Request.Delete(pUrl, 5000, 60000);
   }

   public static Request<?> put(String pUrl, String pContent) {
      return new Request.Put(pUrl, pContent, 5000, 60000);
   }

   public static Request<?> put(String pUrl, String pContent, int pConnectTimeout, int pReadTimeout) {
      return new Request.Put(pUrl, pContent, pConnectTimeout, pReadTimeout);
   }

   public String getHeader(String pName) {
      return getHeader(this.connection, pName);
   }

   public static String getHeader(HttpURLConnection pConnection, String pName) {
      try {
         return pConnection.getHeaderField(pName);
      } catch (Exception exception) {
         return "";
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Delete extends Request<Request.Delete> {
      public Delete(String p_87359_, int p_87360_, int p_87361_) {
         super(p_87359_, p_87360_, p_87361_);
      }

      public Request.Delete doConnect() {
         try {
            this.connection.setDoOutput(true);
            this.connection.setRequestMethod("DELETE");
            this.connection.connect();
            return this;
         } catch (Exception exception) {
            throw new RealmsHttpException(exception.getMessage(), exception);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Get extends Request<Request.Get> {
      public Get(String p_87365_, int p_87366_, int p_87367_) {
         super(p_87365_, p_87366_, p_87367_);
      }

      public Request.Get doConnect() {
         try {
            this.connection.setDoInput(true);
            this.connection.setDoOutput(true);
            this.connection.setUseCaches(false);
            this.connection.setRequestMethod("GET");
            return this;
         } catch (Exception exception) {
            throw new RealmsHttpException(exception.getMessage(), exception);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Post extends Request<Request.Post> {
      private final String content;

      public Post(String pUrl, String pContent, int pConnectTimeout, int pReadTimeout) {
         super(pUrl, pConnectTimeout, pReadTimeout);
         this.content = pContent;
      }

      public Request.Post doConnect() {
         try {
            if (this.content != null) {
               this.connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            }

            this.connection.setDoInput(true);
            this.connection.setDoOutput(true);
            this.connection.setUseCaches(false);
            this.connection.setRequestMethod("POST");
            OutputStream outputstream = this.connection.getOutputStream();
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream, "UTF-8");
            outputstreamwriter.write(this.content);
            outputstreamwriter.close();
            outputstream.flush();
            return this;
         } catch (Exception exception) {
            throw new RealmsHttpException(exception.getMessage(), exception);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Put extends Request<Request.Put> {
      private final String content;

      public Put(String pUrl, String pContent, int pConnectTimeout, int pReadTimeout) {
         super(pUrl, pConnectTimeout, pReadTimeout);
         this.content = pContent;
      }

      public Request.Put doConnect() {
         try {
            if (this.content != null) {
               this.connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            }

            this.connection.setDoOutput(true);
            this.connection.setDoInput(true);
            this.connection.setRequestMethod("PUT");
            OutputStream outputstream = this.connection.getOutputStream();
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream, "UTF-8");
            outputstreamwriter.write(this.content);
            outputstreamwriter.close();
            outputstream.flush();
            return this;
         } catch (Exception exception) {
            throw new RealmsHttpException(exception.getMessage(), exception);
         }
      }
   }
}