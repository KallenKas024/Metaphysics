package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.yggdrasil.request.AbuseReportRequest;
import com.mojang.realmsclient.dto.RealmsServer;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record ReportEnvironment(String clientVersion, @Nullable ReportEnvironment.Server server) {
   public static ReportEnvironment local() {
      return create((ReportEnvironment.Server)null);
   }

   public static ReportEnvironment thirdParty(String pIp) {
      return create(new ReportEnvironment.Server.ThirdParty(pIp));
   }

   public static ReportEnvironment realm(RealmsServer pRealmsServer) {
      return create(new ReportEnvironment.Server.Realm(pRealmsServer));
   }

   public static ReportEnvironment create(@Nullable ReportEnvironment.Server pServer) {
      return new ReportEnvironment(getClientVersion(), pServer);
   }

   public AbuseReportRequest.ClientInfo clientInfo() {
      return new AbuseReportRequest.ClientInfo(this.clientVersion, Locale.getDefault().toLanguageTag());
   }

   @Nullable
   public AbuseReportRequest.ThirdPartyServerInfo thirdPartyServerInfo() {
      ReportEnvironment.Server reportenvironment$server = this.server;
      if (reportenvironment$server instanceof ReportEnvironment.Server.ThirdParty reportenvironment$server$thirdparty) {
         return new AbuseReportRequest.ThirdPartyServerInfo(reportenvironment$server$thirdparty.ip);
      } else {
         return null;
      }
   }

   @Nullable
   public AbuseReportRequest.RealmInfo realmInfo() {
      ReportEnvironment.Server reportenvironment$server = this.server;
      if (reportenvironment$server instanceof ReportEnvironment.Server.Realm reportenvironment$server$realm) {
         return new AbuseReportRequest.RealmInfo(String.valueOf(reportenvironment$server$realm.realmId()), reportenvironment$server$realm.slotId());
      } else {
         return null;
      }
   }

   private static String getClientVersion() {
      StringBuilder stringbuilder = new StringBuilder();
      stringbuilder.append("1.20.1");
      if (Minecraft.checkModStatus().shouldReportAsModified()) {
         stringbuilder.append(" (modded)");
      }

      return stringbuilder.toString();
   }

   @OnlyIn(Dist.CLIENT)
   public interface Server {
      @OnlyIn(Dist.CLIENT)
      public static record Realm(long realmId, int slotId) implements ReportEnvironment.Server {
         public Realm(RealmsServer pRealmsServer) {
            this(pRealmsServer.id, pRealmsServer.activeSlot);
         }
      }

      @OnlyIn(Dist.CLIENT)
      public static record ThirdParty(String ip) implements ReportEnvironment.Server {
      }
   }
}