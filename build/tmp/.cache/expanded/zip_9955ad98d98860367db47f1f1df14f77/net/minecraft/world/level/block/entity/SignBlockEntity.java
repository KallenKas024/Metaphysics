package net.minecraft.world.level.block.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.FilteredText;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class SignBlockEntity extends BlockEntity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAX_TEXT_LINE_WIDTH = 90;
   private static final int TEXT_LINE_HEIGHT = 10;
   @Nullable
   private UUID playerWhoMayEdit;
   private SignText frontText = this.createDefaultSignText();
   private SignText backText = this.createDefaultSignText();
   private boolean isWaxed;

   public SignBlockEntity(BlockPos pPos, BlockState pBlockState) {
      this(BlockEntityType.SIGN, pPos, pBlockState);
   }

   public SignBlockEntity(BlockEntityType pType, BlockPos pPos, BlockState pBlockState) {
      super(pType, pPos, pBlockState);
   }

   protected SignText createDefaultSignText() {
      return new SignText();
   }

   public boolean isFacingFrontText(Player pPlayer) {
      Block $$2 = this.getBlockState().getBlock();
      if ($$2 instanceof SignBlock signblock) {
         Vec3 vec3 = signblock.getSignHitboxCenterPosition(this.getBlockState());
         double d0 = pPlayer.getX() - ((double)this.getBlockPos().getX() + vec3.x);
         double d1 = pPlayer.getZ() - ((double)this.getBlockPos().getZ() + vec3.z);
         float f = signblock.getYRotationDegrees(this.getBlockState());
         float f1 = (float)(Mth.atan2(d1, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
         return Mth.degreesDifferenceAbs(f, f1) <= 90.0F;
      } else {
         return false;
      }
   }

   public SignText getTextFacingPlayer(Player pPlayer) {
      return this.getText(this.isFacingFrontText(pPlayer));
   }

   public SignText getText(boolean pIsFrontText) {
      return pIsFrontText ? this.frontText : this.backText;
   }

   public SignText getFrontText() {
      return this.frontText;
   }

   public SignText getBackText() {
      return this.backText;
   }

   public int getTextLineHeight() {
      return 10;
   }

   public int getMaxTextLineWidth() {
      return 90;
   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      SignText.DIRECT_CODEC.encodeStart(NbtOps.INSTANCE, this.frontText).resultOrPartial(LOGGER::error).ifPresent((p_277417_) -> {
         pTag.put("front_text", p_277417_);
      });
      SignText.DIRECT_CODEC.encodeStart(NbtOps.INSTANCE, this.backText).resultOrPartial(LOGGER::error).ifPresent((p_277389_) -> {
         pTag.put("back_text", p_277389_);
      });
      pTag.putBoolean("is_waxed", this.isWaxed);
   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      if (pTag.contains("front_text")) {
         SignText.DIRECT_CODEC.parse(NbtOps.INSTANCE, pTag.getCompound("front_text")).resultOrPartial(LOGGER::error).ifPresent((p_278212_) -> {
            this.frontText = this.loadLines(p_278212_);
         });
      }

      if (pTag.contains("back_text")) {
         SignText.DIRECT_CODEC.parse(NbtOps.INSTANCE, pTag.getCompound("back_text")).resultOrPartial(LOGGER::error).ifPresent((p_278213_) -> {
            this.backText = this.loadLines(p_278213_);
         });
      }

      this.isWaxed = pTag.getBoolean("is_waxed");
   }

   private SignText loadLines(SignText pText) {
      for(int i = 0; i < 4; ++i) {
         Component component = this.loadLine(pText.getMessage(i, false));
         Component component1 = this.loadLine(pText.getMessage(i, true));
         pText = pText.setMessage(i, component, component1);
      }

      return pText;
   }

   private Component loadLine(Component pLineText) {
      Level level = this.level;
      if (level instanceof ServerLevel serverlevel) {
         try {
            return ComponentUtils.updateForEntity(createCommandSourceStack((Player)null, serverlevel, this.worldPosition), pLineText, (Entity)null, 0);
         } catch (CommandSyntaxException commandsyntaxexception) {
         }
      }

      return pLineText;
   }

   public void updateSignText(Player pPlayer, boolean pIsFrontText, List<FilteredText> pFilteredText) {
      if (!this.isWaxed() && pPlayer.getUUID().equals(this.getPlayerWhoMayEdit()) && this.level != null) {
         this.updateText((p_277776_) -> {
            return this.setMessages(pPlayer, pFilteredText, p_277776_);
         }, pIsFrontText);
         this.setAllowedPlayerEditor((UUID)null);
         this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
      } else {
         LOGGER.warn("Player {} just tried to change non-editable sign", (Object)pPlayer.getName().getString());
      }
   }

   public boolean updateText(UnaryOperator<SignText> pUpdater, boolean pIsFrontText) {
      SignText signtext = this.getText(pIsFrontText);
      return this.setText(pUpdater.apply(signtext), pIsFrontText);
   }

   private SignText setMessages(Player pPlayer, List<FilteredText> pFilteredText, SignText pText) {
      for(int i = 0; i < pFilteredText.size(); ++i) {
         FilteredText filteredtext = pFilteredText.get(i);
         Style style = pText.getMessage(i, pPlayer.isTextFilteringEnabled()).getStyle();
         if (pPlayer.isTextFilteringEnabled()) {
            pText = pText.setMessage(i, Component.literal(filteredtext.filteredOrEmpty()).setStyle(style));
         } else {
            pText = pText.setMessage(i, Component.literal(filteredtext.raw()).setStyle(style), Component.literal(filteredtext.filteredOrEmpty()).setStyle(style));
         }
      }

      return pText;
   }

   public boolean setText(SignText pText, boolean pIsFrontText) {
      return pIsFrontText ? this.setFrontText(pText) : this.setBackText(pText);
   }

   private boolean setBackText(SignText pText) {
      if (pText != this.backText) {
         this.backText = pText;
         this.markUpdated();
         return true;
      } else {
         return false;
      }
   }

   private boolean setFrontText(SignText pText) {
      if (pText != this.frontText) {
         this.frontText = pText;
         this.markUpdated();
         return true;
      } else {
         return false;
      }
   }

   public boolean canExecuteClickCommands(boolean pIsFrontText, Player pPlayer) {
      return this.isWaxed() && this.getText(pIsFrontText).hasAnyClickCommands(pPlayer);
   }

   public boolean executeClickCommandsIfPresent(Player pPlayer, Level pLevel, BlockPos pPos, boolean pFrontText) {
      boolean flag = false;

      for(Component component : this.getText(pFrontText).getMessages(pPlayer.isTextFilteringEnabled())) {
         Style style = component.getStyle();
         ClickEvent clickevent = style.getClickEvent();
         if (clickevent != null && clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
            pPlayer.getServer().getCommands().performPrefixedCommand(createCommandSourceStack(pPlayer, pLevel, pPos), clickevent.getValue());
            flag = true;
         }
      }

      return flag;
   }

   private static CommandSourceStack createCommandSourceStack(@Nullable Player pPlayer, Level pLevel, BlockPos pPos) {
      String s = pPlayer == null ? "Sign" : pPlayer.getName().getString();
      Component component = (Component)(pPlayer == null ? Component.literal("Sign") : pPlayer.getDisplayName());
      return new CommandSourceStack(CommandSource.NULL, Vec3.atCenterOf(pPos), Vec2.ZERO, (ServerLevel)pLevel, 2, s, component, pLevel.getServer(), pPlayer);
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   /**
    * Get an NBT compound to sync to the client with SPacketChunkData, used for initial loading of the chunk or when
    * many blocks change at once. This compound comes back to you clientside in {@link handleUpdateTag}
    */
   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
   }

   public boolean onlyOpCanSetNbt() {
      return true;
   }

   public void setAllowedPlayerEditor(@Nullable UUID pPlayWhoMayEdit) {
      this.playerWhoMayEdit = pPlayWhoMayEdit;
   }

   @Nullable
   public UUID getPlayerWhoMayEdit() {
      return this.playerWhoMayEdit;
   }

   private void markUpdated() {
      this.setChanged();
      this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
   }

   public boolean isWaxed() {
      return this.isWaxed;
   }

   public boolean setWaxed(boolean pIsWaxed) {
      if (this.isWaxed != pIsWaxed) {
         this.isWaxed = pIsWaxed;
         this.markUpdated();
         return true;
      } else {
         return false;
      }
   }

   public boolean playerIsTooFarAwayToEdit(UUID pUuid) {
      Player player = this.level.getPlayerByUUID(pUuid);
      return player == null || player.distanceToSqr((double)this.getBlockPos().getX(), (double)this.getBlockPos().getY(), (double)this.getBlockPos().getZ()) > 64.0D;
   }

   public static void tick(Level pLevel, BlockPos pPos, BlockState pState, SignBlockEntity pSign) {
      UUID uuid = pSign.getPlayerWhoMayEdit();
      if (uuid != null) {
         pSign.clearInvalidPlayerWhoMayEdit(pSign, pLevel, uuid);
      }

   }

   private void clearInvalidPlayerWhoMayEdit(SignBlockEntity pSign, Level pLevel, UUID pUuid) {
      if (pSign.playerIsTooFarAwayToEdit(pUuid)) {
         pSign.setAllowedPlayerEditor((UUID)null);
      }

   }

   @Override
   public net.minecraft.world.phys.AABB getRenderBoundingBox() {
      return new net.minecraft.world.phys.AABB(this.getBlockPos(), this.getBlockPos().offset(1, 1, 1));
   }
}
