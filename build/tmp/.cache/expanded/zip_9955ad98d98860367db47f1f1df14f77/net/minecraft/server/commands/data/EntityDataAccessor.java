package net.minecraft.server.commands.data;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class EntityDataAccessor implements DataAccessor {
   private static final SimpleCommandExceptionType ERROR_NO_PLAYERS = new SimpleCommandExceptionType(Component.translatable("commands.data.entity.invalid"));
   public static final Function<String, DataCommands.DataProvider> PROVIDER = (p_139517_) -> {
      return new DataCommands.DataProvider() {
         /**
          * Creates an accessor based on the command context. This should only refer to arguments registered in {@link
          * createArgument}.
          */
         public DataAccessor access(CommandContext<CommandSourceStack> p_139530_) throws CommandSyntaxException {
            return new EntityDataAccessor(EntityArgument.getEntity(p_139530_, p_139517_));
         }

         /**
          * Creates an argument used for accessing data related to this type of thing, including a literal to
          * distinguish from other types.
          */
         public ArgumentBuilder<CommandSourceStack, ?> wrap(ArgumentBuilder<CommandSourceStack, ?> p_139527_, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> p_139528_) {
            return p_139527_.then(Commands.literal("entity").then(p_139528_.apply(Commands.argument(p_139517_, EntityArgument.entity()))));
         }
      };
   };
   private final Entity entity;

   public EntityDataAccessor(Entity pEntity) {
      this.entity = pEntity;
   }

   public void setData(CompoundTag pOther) throws CommandSyntaxException {
      if (this.entity instanceof Player) {
         throw ERROR_NO_PLAYERS.create();
      } else {
         UUID uuid = this.entity.getUUID();
         this.entity.load(pOther);
         this.entity.setUUID(uuid);
      }
   }

   public CompoundTag getData() {
      return NbtPredicate.getEntityTagToCompare(this.entity);
   }

   public Component getModifiedSuccess() {
      return Component.translatable("commands.data.entity.modified", this.entity.getDisplayName());
   }

   /**
    * Gets the message used as a result of querying the given NBT (both for /data get and /data get path)
    */
   public Component getPrintSuccess(Tag pNbt) {
      return Component.translatable("commands.data.entity.query", this.entity.getDisplayName(), NbtUtils.toPrettyComponent(pNbt));
   }

   /**
    * Gets the message used as a result of querying the given path with a scale.
    */
   public Component getPrintSuccess(NbtPathArgument.NbtPath pPath, double pScale, int pValue) {
      return Component.translatable("commands.data.entity.get", pPath, this.entity.getDisplayName(), String.format(Locale.ROOT, "%.2f", pScale), pValue);
   }
}