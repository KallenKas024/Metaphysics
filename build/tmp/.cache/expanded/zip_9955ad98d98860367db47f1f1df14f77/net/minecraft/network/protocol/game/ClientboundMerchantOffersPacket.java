package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.trading.MerchantOffers;

public class ClientboundMerchantOffersPacket implements Packet<ClientGamePacketListener> {
   private final int containerId;
   private final MerchantOffers offers;
   private final int villagerLevel;
   private final int villagerXp;
   private final boolean showProgress;
   private final boolean canRestock;

   public ClientboundMerchantOffersPacket(int pContainerId, MerchantOffers pOffers, int pVillagerLevel, int pVillagerXp, boolean pShowProgress, boolean pCanRestock) {
      this.containerId = pContainerId;
      this.offers = pOffers;
      this.villagerLevel = pVillagerLevel;
      this.villagerXp = pVillagerXp;
      this.showProgress = pShowProgress;
      this.canRestock = pCanRestock;
   }

   public ClientboundMerchantOffersPacket(FriendlyByteBuf pBuffer) {
      this.containerId = pBuffer.readVarInt();
      this.offers = MerchantOffers.createFromStream(pBuffer);
      this.villagerLevel = pBuffer.readVarInt();
      this.villagerXp = pBuffer.readVarInt();
      this.showProgress = pBuffer.readBoolean();
      this.canRestock = pBuffer.readBoolean();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.containerId);
      this.offers.writeToStream(pBuffer);
      pBuffer.writeVarInt(this.villagerLevel);
      pBuffer.writeVarInt(this.villagerXp);
      pBuffer.writeBoolean(this.showProgress);
      pBuffer.writeBoolean(this.canRestock);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleMerchantOffers(this);
   }

   public int getContainerId() {
      return this.containerId;
   }

   public MerchantOffers getOffers() {
      return this.offers;
   }

   public int getVillagerLevel() {
      return this.villagerLevel;
   }

   public int getVillagerXp() {
      return this.villagerXp;
   }

   public boolean showProgress() {
      return this.showProgress;
   }

   public boolean canRestock() {
      return this.canRestock;
   }
}