package net.minecraft.client.gui.screens.inventory;

import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MinecartCommandBlockEditScreen extends AbstractCommandBlockEditScreen {
   private final BaseCommandBlock commandBlock;

   public MinecartCommandBlockEditScreen(BaseCommandBlock pCommandBlock) {
      this.commandBlock = pCommandBlock;
   }

   public BaseCommandBlock getCommandBlock() {
      return this.commandBlock;
   }

   int getPreviousY() {
      return 150;
   }

   protected void init() {
      super.init();
      this.commandEdit.setValue(this.getCommandBlock().getCommand());
   }

   protected void populateAndSendPacket(BaseCommandBlock pCommandBlock) {
      if (pCommandBlock instanceof MinecartCommandBlock.MinecartCommandBase minecartcommandblock$minecartcommandbase) {
         this.minecraft.getConnection().send(new ServerboundSetCommandMinecartPacket(minecartcommandblock$minecartcommandbase.getMinecart().getId(), this.commandEdit.getValue(), pCommandBlock.isTrackOutput()));
      }

   }
}