package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerInfo extends ValueObject implements ReflectionBasedSerialization {
   @SerializedName("name")
   private String name;
   @SerializedName("uuid")
   private String uuid;
   @SerializedName("operator")
   private boolean operator;
   @SerializedName("accepted")
   private boolean accepted;
   @SerializedName("online")
   private boolean online;

   public String getName() {
      return this.name;
   }

   public void setName(String pName) {
      this.name = pName;
   }

   public String getUuid() {
      return this.uuid;
   }

   public void setUuid(String pUuid) {
      this.uuid = pUuid;
   }

   public boolean isOperator() {
      return this.operator;
   }

   public void setOperator(boolean pOperator) {
      this.operator = pOperator;
   }

   public boolean getAccepted() {
      return this.accepted;
   }

   public void setAccepted(boolean pAccepted) {
      this.accepted = pAccepted;
   }

   public boolean getOnline() {
      return this.online;
   }

   public void setOnline(boolean pOnline) {
      this.online = pOnline;
   }
}