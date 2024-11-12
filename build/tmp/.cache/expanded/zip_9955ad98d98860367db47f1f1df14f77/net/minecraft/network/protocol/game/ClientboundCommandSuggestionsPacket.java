package net.minecraft.network.protocol.game;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.Packet;

public class ClientboundCommandSuggestionsPacket implements Packet<ClientGamePacketListener> {
   private final int id;
   private final Suggestions suggestions;

   public ClientboundCommandSuggestionsPacket(int pId, Suggestions pSuggestions) {
      this.id = pId;
      this.suggestions = pSuggestions;
   }

   public ClientboundCommandSuggestionsPacket(FriendlyByteBuf pBuffer) {
      this.id = pBuffer.readVarInt();
      int i = pBuffer.readVarInt();
      int j = pBuffer.readVarInt();
      StringRange stringrange = StringRange.between(i, i + j);
      List<Suggestion> list = pBuffer.readList((p_178793_) -> {
         String s = p_178793_.readUtf();
         Component component = p_178793_.readNullable(FriendlyByteBuf::readComponent);
         return new Suggestion(stringrange, s, component);
      });
      this.suggestions = new Suggestions(stringrange, list);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.id);
      pBuffer.writeVarInt(this.suggestions.getRange().getStart());
      pBuffer.writeVarInt(this.suggestions.getRange().getLength());
      pBuffer.writeCollection(this.suggestions.getList(), (p_237617_, p_237618_) -> {
         p_237617_.writeUtf(p_237618_.getText());
         p_237617_.writeNullable(p_237618_.getTooltip(), (p_237614_, p_237615_) -> {
            p_237614_.writeComponent(ComponentUtils.fromMessage(p_237615_));
         });
      });
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleCommandSuggestions(this);
   }

   public int getId() {
      return this.id;
   }

   public Suggestions getSuggestions() {
      return this.suggestions;
   }
}