package com.mojang.realmsclient.exception;

import com.mojang.realmsclient.client.RealmsError;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsServiceException extends Exception {
   public final int httpResultCode;
   public final String rawResponse;
   @Nullable
   public final RealmsError realmsError;

   public RealmsServiceException(int pHttpResultCode, String pHttpResponseContent, RealmsError pRealmsError) {
      super(pHttpResponseContent);
      this.httpResultCode = pHttpResultCode;
      this.rawResponse = pHttpResponseContent;
      this.realmsError = pRealmsError;
   }

   public RealmsServiceException(int pHttpResultCode, String pRawResponse) {
      super(pRawResponse);
      this.httpResultCode = pHttpResultCode;
      this.rawResponse = pRawResponse;
      this.realmsError = null;
   }

   public String getMessage() {
      if (this.realmsError != null) {
         String s = "mco.errorMessage." + this.realmsError.getErrorCode();
         String s1 = I18n.exists(s) ? I18n.get(s) : this.realmsError.getErrorMessage();
         return String.format(Locale.ROOT, "Realms service error (%d/%d) %s", this.httpResultCode, this.realmsError.getErrorCode(), s1);
      } else {
         return String.format(Locale.ROOT, "Realms service error (%d) %s", this.httpResultCode, this.rawResponse);
      }
   }

   public int realmsErrorCodeOrDefault(int pDefaultErrorCode) {
      return this.realmsError != null ? this.realmsError.getErrorCode() : pDefaultErrorCode;
   }
}