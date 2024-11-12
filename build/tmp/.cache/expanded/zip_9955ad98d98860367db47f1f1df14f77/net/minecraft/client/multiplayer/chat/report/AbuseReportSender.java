package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.exceptions.MinecraftClientHttpException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.yggdrasil.request.AbuseReportRequest;
import com.mojang.datafixers.util.Unit;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ThrowingComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface AbuseReportSender {
   static AbuseReportSender create(ReportEnvironment pEnvironment, UserApiService pUserApiService) {
      return new AbuseReportSender.Services(pEnvironment, pUserApiService);
   }

   CompletableFuture<Unit> send(UUID pId, AbuseReport pReport);

   boolean isEnabled();

   default AbuseReportLimits reportLimits() {
      return AbuseReportLimits.DEFAULTS;
   }

   @OnlyIn(Dist.CLIENT)
   public static class SendException extends ThrowingComponent {
      public SendException(Component p_239646_, Throwable p_239647_) {
         super(p_239646_, p_239647_);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static record Services(ReportEnvironment environment, UserApiService userApiService) implements AbuseReportSender {
      private static final Component SERVICE_UNAVAILABLE_TEXT = Component.translatable("gui.abuseReport.send.service_unavailable");
      private static final Component HTTP_ERROR_TEXT = Component.translatable("gui.abuseReport.send.http_error");
      private static final Component JSON_ERROR_TEXT = Component.translatable("gui.abuseReport.send.json_error");

      public CompletableFuture<Unit> send(UUID pId, AbuseReport pReport) {
         return CompletableFuture.supplyAsync(() -> {
            AbuseReportRequest abusereportrequest = new AbuseReportRequest(1, pId, pReport, this.environment.clientInfo(), this.environment.thirdPartyServerInfo(), this.environment.realmInfo());

            try {
               this.userApiService.reportAbuse(abusereportrequest);
               return Unit.INSTANCE;
            } catch (MinecraftClientHttpException minecraftclienthttpexception) {
               Component component1 = this.getHttpErrorDescription(minecraftclienthttpexception);
               throw new CompletionException(new AbuseReportSender.SendException(component1, minecraftclienthttpexception));
            } catch (MinecraftClientException minecraftclientexception) {
               Component component = this.getErrorDescription(minecraftclientexception);
               throw new CompletionException(new AbuseReportSender.SendException(component, minecraftclientexception));
            }
         }, Util.ioPool());
      }

      public boolean isEnabled() {
         return this.userApiService.canSendReports();
      }

      private Component getHttpErrorDescription(MinecraftClientHttpException pHttpException) {
         return Component.translatable("gui.abuseReport.send.error_message", pHttpException.getMessage());
      }

      private Component getErrorDescription(MinecraftClientException pException) {
         Component component;
         switch (pException.getType()) {
            case SERVICE_UNAVAILABLE:
               component = SERVICE_UNAVAILABLE_TEXT;
               break;
            case HTTP_ERROR:
               component = HTTP_ERROR_TEXT;
               break;
            case JSON_ERROR:
               component = JSON_ERROR_TEXT;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return component;
      }

      public AbuseReportLimits reportLimits() {
         return this.userApiService.getAbuseReportLimits();
      }
   }
}