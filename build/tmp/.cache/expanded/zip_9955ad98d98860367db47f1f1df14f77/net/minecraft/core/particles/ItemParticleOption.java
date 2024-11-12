package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class ItemParticleOption implements ParticleOptions {
   public static final ParticleOptions.Deserializer<ItemParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<ItemParticleOption>() {
      public ItemParticleOption fromCommand(ParticleType<ItemParticleOption> p_123721_, StringReader p_123722_) throws CommandSyntaxException {
         p_123722_.expect(' ');
         ItemParser.ItemResult itemparser$itemresult = ItemParser.parseForItem(BuiltInRegistries.ITEM.asLookup(), p_123722_);
         ItemStack itemstack = (new ItemInput(itemparser$itemresult.item(), itemparser$itemresult.nbt())).createItemStack(1, false);
         return new ItemParticleOption(p_123721_, itemstack);
      }

      public ItemParticleOption fromNetwork(ParticleType<ItemParticleOption> p_123724_, FriendlyByteBuf p_123725_) {
         return new ItemParticleOption(p_123724_, p_123725_.readItem());
      }
   };
   private final ParticleType<ItemParticleOption> type;
   private final ItemStack itemStack;

   public static Codec<ItemParticleOption> codec(ParticleType<ItemParticleOption> pType) {
      return ItemStack.CODEC.xmap((p_123714_) -> {
         return new ItemParticleOption(pType, p_123714_);
      }, (p_123709_) -> {
         return p_123709_.itemStack;
      });
   }

   public ItemParticleOption(ParticleType<ItemParticleOption> pType, ItemStack pItemStack) {
      this.type = pType;
      this.itemStack = pItemStack.copy(); //Forge: Fix stack updating after the fact causing particle changes.
   }

   public void writeToNetwork(FriendlyByteBuf pBuffer) {
      pBuffer.writeItem(this.itemStack);
   }

   public String writeToString() {
      return BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()) + " " + (new ItemInput(this.itemStack.getItemHolder(), this.itemStack.getTag())).serialize();
   }

   public ParticleType<ItemParticleOption> getType() {
      return this.type;
   }

   public ItemStack getItem() {
      return this.itemStack;
   }
}
