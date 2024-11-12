package net.minecraft.world.level;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public abstract class BaseCommandBlock implements CommandSource {
   /** The formatting for the timestamp on commands run. */
   private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
   private static final Component DEFAULT_NAME = Component.literal("@");
   private long lastExecution = -1L;
   private boolean updateLastExecution = true;
   /** The number of successful commands run. (used for redstone output) */
   private int successCount;
   private boolean trackOutput = true;
   /** The previously run command. */
   @Nullable
   private Component lastOutput;
   /** The command stored in the command block. */
   private String command = "";
   /** The custom name of the command block. (defaults to "@") */
   private Component name = DEFAULT_NAME;

   /**
    * Returns the {@code successCount} int.
    */
   public int getSuccessCount() {
      return this.successCount;
   }

   public void setSuccessCount(int pSuccessCount) {
      this.successCount = pSuccessCount;
   }

   /**
    * Returns the lastOutput.
    */
   public Component getLastOutput() {
      return this.lastOutput == null ? CommonComponents.EMPTY : this.lastOutput;
   }

   public CompoundTag save(CompoundTag pCompound) {
      pCompound.putString("Command", this.command);
      pCompound.putInt("SuccessCount", this.successCount);
      pCompound.putString("CustomName", Component.Serializer.toJson(this.name));
      pCompound.putBoolean("TrackOutput", this.trackOutput);
      if (this.lastOutput != null && this.trackOutput) {
         pCompound.putString("LastOutput", Component.Serializer.toJson(this.lastOutput));
      }

      pCompound.putBoolean("UpdateLastExecution", this.updateLastExecution);
      if (this.updateLastExecution && this.lastExecution > 0L) {
         pCompound.putLong("LastExecution", this.lastExecution);
      }

      return pCompound;
   }

   /**
    * Reads NBT formatting and stored data into variables.
    */
   public void load(CompoundTag pNbt) {
      this.command = pNbt.getString("Command");
      this.successCount = pNbt.getInt("SuccessCount");
      if (pNbt.contains("CustomName", 8)) {
         this.setName(Component.Serializer.fromJson(pNbt.getString("CustomName")));
      }

      if (pNbt.contains("TrackOutput", 1)) {
         this.trackOutput = pNbt.getBoolean("TrackOutput");
      }

      if (pNbt.contains("LastOutput", 8) && this.trackOutput) {
         try {
            this.lastOutput = Component.Serializer.fromJson(pNbt.getString("LastOutput"));
         } catch (Throwable throwable) {
            this.lastOutput = Component.literal(throwable.getMessage());
         }
      } else {
         this.lastOutput = null;
      }

      if (pNbt.contains("UpdateLastExecution")) {
         this.updateLastExecution = pNbt.getBoolean("UpdateLastExecution");
      }

      if (this.updateLastExecution && pNbt.contains("LastExecution")) {
         this.lastExecution = pNbt.getLong("LastExecution");
      } else {
         this.lastExecution = -1L;
      }

   }

   /**
    * Sets the command.
    */
   public void setCommand(String pCommand) {
      this.command = pCommand;
      this.successCount = 0;
   }

   /**
    * Returns the command of the command block.
    */
   public String getCommand() {
      return this.command;
   }

   public boolean performCommand(Level pLevel) {
      if (!pLevel.isClientSide && pLevel.getGameTime() != this.lastExecution) {
         if ("Searge".equalsIgnoreCase(this.command)) {
            this.lastOutput = Component.literal("#itzlipofutzli");
            this.successCount = 1;
            return true;
         } else {
            this.successCount = 0;
            MinecraftServer minecraftserver = this.getLevel().getServer();
            if (minecraftserver.isCommandBlockEnabled() && !StringUtil.isNullOrEmpty(this.command)) {
               try {
                  this.lastOutput = null;
                  CommandSourceStack commandsourcestack = this.createCommandSourceStack().withCallback((p_45417_, p_45418_, p_45419_) -> {
                     if (p_45418_) {
                        ++this.successCount;
                     }

                  });
                  minecraftserver.getCommands().performPrefixedCommand(commandsourcestack, this.command);
               } catch (Throwable throwable) {
                  CrashReport crashreport = CrashReport.forThrowable(throwable, "Executing command block");
                  CrashReportCategory crashreportcategory = crashreport.addCategory("Command to be executed");
                  crashreportcategory.setDetail("Command", this::getCommand);
                  crashreportcategory.setDetail("Name", () -> {
                     return this.getName().getString();
                  });
                  throw new ReportedException(crashreport);
               }
            }

            if (this.updateLastExecution) {
               this.lastExecution = pLevel.getGameTime();
            } else {
               this.lastExecution = -1L;
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public Component getName() {
      return this.name;
   }

   public void setName(@Nullable Component pName) {
      if (pName != null) {
         this.name = pName;
      } else {
         this.name = DEFAULT_NAME;
      }

   }

   public void sendSystemMessage(Component pComponent) {
      if (this.trackOutput) {
         this.lastOutput = Component.literal("[" + TIME_FORMAT.format(new Date()) + "] ").append(pComponent);
         this.onUpdated();
      }

   }

   public abstract ServerLevel getLevel();

   public abstract void onUpdated();

   public void setLastOutput(@Nullable Component pLastOutputMessage) {
      this.lastOutput = pLastOutputMessage;
   }

   public void setTrackOutput(boolean pShouldTrackOutput) {
      this.trackOutput = pShouldTrackOutput;
   }

   public boolean isTrackOutput() {
      return this.trackOutput;
   }

   public InteractionResult usedBy(Player pPlayer) {
      if (!pPlayer.canUseGameMasterBlocks()) {
         return InteractionResult.PASS;
      } else {
         if (pPlayer.getCommandSenderWorld().isClientSide) {
            pPlayer.openMinecartCommandBlock(this);
         }

         return InteractionResult.sidedSuccess(pPlayer.level().isClientSide);
      }
   }

   public abstract Vec3 getPosition();

   public abstract CommandSourceStack createCommandSourceStack();

   public boolean acceptsSuccess() {
      return this.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK) && this.trackOutput;
   }

   public boolean acceptsFailure() {
      return this.trackOutput;
   }

   public boolean shouldInformAdmins() {
      return this.getLevel().getGameRules().getBoolean(GameRules.RULE_COMMANDBLOCKOUTPUT);
   }

   public abstract boolean isValid();
}