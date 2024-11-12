package net.minecraft.server.commands.data;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.CommandStorage;

public class StorageDataAccessor implements DataAccessor {
   static final SuggestionProvider<CommandSourceStack> SUGGEST_STORAGE = (p_139547_, p_139548_) -> {
      return SharedSuggestionProvider.suggestResource(getGlobalTags(p_139547_).keys(), p_139548_);
   };
   public static final Function<String, DataCommands.DataProvider> PROVIDER = (p_139554_) -> {
      return new DataCommands.DataProvider() {
         /**
          * Creates an accessor based on the command context. This should only refer to arguments registered in {@link
          * createArgument}.
          */
         public DataAccessor access(CommandContext<CommandSourceStack> p_139570_) {
            return new StorageDataAccessor(StorageDataAccessor.getGlobalTags(p_139570_), ResourceLocationArgument.getId(p_139570_, p_139554_));
         }

         /**
          * Creates an argument used for accessing data related to this type of thing, including a literal to
          * distinguish from other types.
          */
         public ArgumentBuilder<CommandSourceStack, ?> wrap(ArgumentBuilder<CommandSourceStack, ?> p_139567_, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> p_139568_) {
            return p_139567_.then(Commands.literal("storage").then(p_139568_.apply(Commands.argument(p_139554_, ResourceLocationArgument.id()).suggests(StorageDataAccessor.SUGGEST_STORAGE))));
         }
      };
   };
   private final CommandStorage storage;
   private final ResourceLocation id;

   static CommandStorage getGlobalTags(CommandContext<CommandSourceStack> pContext) {
      return pContext.getSource().getServer().getCommandStorage();
   }

   StorageDataAccessor(CommandStorage pStorage, ResourceLocation pId) {
      this.storage = pStorage;
      this.id = pId;
   }

   public void setData(CompoundTag pOther) {
      this.storage.set(this.id, pOther);
   }

   public CompoundTag getData() {
      return this.storage.get(this.id);
   }

   public Component getModifiedSuccess() {
      return Component.translatable("commands.data.storage.modified", this.id);
   }

   /**
    * Gets the message used as a result of querying the given NBT (both for /data get and /data get path)
    */
   public Component getPrintSuccess(Tag pNbt) {
      return Component.translatable("commands.data.storage.query", this.id, NbtUtils.toPrettyComponent(pNbt));
   }

   /**
    * Gets the message used as a result of querying the given path with a scale.
    */
   public Component getPrintSuccess(NbtPathArgument.NbtPath pPath, double pScale, int pValue) {
      return Component.translatable("commands.data.storage.get", pPath, this.id, String.format(Locale.ROOT, "%.2f", pScale), pValue);
   }
}