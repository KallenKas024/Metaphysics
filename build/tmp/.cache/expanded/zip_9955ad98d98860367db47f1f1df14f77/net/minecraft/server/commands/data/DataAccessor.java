package net.minecraft.server.commands.data;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

public interface DataAccessor {
   void setData(CompoundTag pOther) throws CommandSyntaxException;

   CompoundTag getData() throws CommandSyntaxException;

   Component getModifiedSuccess();

   /**
    * Gets the message used as a result of querying the given NBT (both for /data get and /data get path)
    */
   Component getPrintSuccess(Tag pNbt);

   /**
    * Gets the message used as a result of querying the given path with a scale.
    */
   Component getPrintSuccess(NbtPathArgument.NbtPath pPath, double pScale, int pValue);
}