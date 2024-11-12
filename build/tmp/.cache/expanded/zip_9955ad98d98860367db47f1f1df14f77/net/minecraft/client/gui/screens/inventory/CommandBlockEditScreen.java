package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CommandBlockEditScreen extends AbstractCommandBlockEditScreen {
   private final CommandBlockEntity autoCommandBlock;
   private CycleButton<CommandBlockEntity.Mode> modeButton;
   private CycleButton<Boolean> conditionalButton;
   private CycleButton<Boolean> autoexecButton;
   private CommandBlockEntity.Mode mode = CommandBlockEntity.Mode.REDSTONE;
   private boolean conditional;
   private boolean autoexec;

   public CommandBlockEditScreen(CommandBlockEntity pAutoCommandBlock) {
      this.autoCommandBlock = pAutoCommandBlock;
   }

   BaseCommandBlock getCommandBlock() {
      return this.autoCommandBlock.getCommandBlock();
   }

   int getPreviousY() {
      return 135;
   }

   protected void init() {
      super.init();
      this.modeButton = this.addRenderableWidget(CycleButton.<CommandBlockEntity.Mode>builder((p_287312_) -> {
         MutableComponent mutablecomponent;
         switch (p_287312_) {
            case SEQUENCE:
               mutablecomponent = Component.translatable("advMode.mode.sequence");
               break;
            case AUTO:
               mutablecomponent = Component.translatable("advMode.mode.auto");
               break;
            case REDSTONE:
               mutablecomponent = Component.translatable("advMode.mode.redstone");
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return mutablecomponent;
      }).withValues(CommandBlockEntity.Mode.values()).displayOnlyValue().withInitialValue(this.mode).create(this.width / 2 - 50 - 100 - 4, 165, 100, 20, Component.translatable("advMode.mode"), (p_169721_, p_169722_) -> {
         this.mode = p_169722_;
      }));
      this.conditionalButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("advMode.mode.conditional"), Component.translatable("advMode.mode.unconditional")).displayOnlyValue().withInitialValue(this.conditional).create(this.width / 2 - 50, 165, 100, 20, Component.translatable("advMode.type"), (p_169727_, p_169728_) -> {
         this.conditional = p_169728_;
      }));
      this.autoexecButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("advMode.mode.autoexec.bat"), Component.translatable("advMode.mode.redstoneTriggered")).displayOnlyValue().withInitialValue(this.autoexec).create(this.width / 2 + 50 + 4, 165, 100, 20, Component.translatable("advMode.triggering"), (p_169724_, p_169725_) -> {
         this.autoexec = p_169725_;
      }));
      this.enableControls(false);
   }

   private void enableControls(boolean pActive) {
      this.doneButton.active = pActive;
      this.outputButton.active = pActive;
      this.modeButton.active = pActive;
      this.conditionalButton.active = pActive;
      this.autoexecButton.active = pActive;
   }

   public void updateGui() {
      BaseCommandBlock basecommandblock = this.autoCommandBlock.getCommandBlock();
      this.commandEdit.setValue(basecommandblock.getCommand());
      boolean flag = basecommandblock.isTrackOutput();
      this.mode = this.autoCommandBlock.getMode();
      this.conditional = this.autoCommandBlock.isConditional();
      this.autoexec = this.autoCommandBlock.isAutomatic();
      this.outputButton.setValue(flag);
      this.modeButton.setValue(this.mode);
      this.conditionalButton.setValue(this.conditional);
      this.autoexecButton.setValue(this.autoexec);
      this.updatePreviousOutput(flag);
      this.enableControls(true);
   }

   public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
      super.resize(pMinecraft, pWidth, pHeight);
      this.enableControls(true);
   }

   protected void populateAndSendPacket(BaseCommandBlock pCommandBlock) {
      this.minecraft.getConnection().send(new ServerboundSetCommandBlockPacket(BlockPos.containing(pCommandBlock.getPosition()), this.commandEdit.getValue(), this.mode, pCommandBlock.isTrackOutput(), this.conditional, this.autoexec));
   }
}