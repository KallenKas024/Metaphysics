package net.minecraft.stats;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ResourceLocationException;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.slf4j.Logger;

public class ServerRecipeBook extends RecipeBook {
   public static final String RECIPE_BOOK_TAG = "recipeBook";
   private static final Logger LOGGER = LogUtils.getLogger();

   public int addRecipes(Collection<Recipe<?>> pRecipes, ServerPlayer pPlayer) {
      List<ResourceLocation> list = Lists.newArrayList();
      int i = 0;

      for(Recipe<?> recipe : pRecipes) {
         ResourceLocation resourcelocation = recipe.getId();
         if (!this.known.contains(resourcelocation) && !recipe.isSpecial()) {
            this.add(resourcelocation);
            this.addHighlight(resourcelocation);
            list.add(resourcelocation);
            CriteriaTriggers.RECIPE_UNLOCKED.trigger(pPlayer, recipe);
            ++i;
         }
      }

      if (list.size() > 0) {
         this.sendRecipes(ClientboundRecipePacket.State.ADD, pPlayer, list);
      }

      return i;
   }

   public int removeRecipes(Collection<Recipe<?>> pRecipes, ServerPlayer pPlayer) {
      List<ResourceLocation> list = Lists.newArrayList();
      int i = 0;

      for(Recipe<?> recipe : pRecipes) {
         ResourceLocation resourcelocation = recipe.getId();
         if (this.known.contains(resourcelocation)) {
            this.remove(resourcelocation);
            list.add(resourcelocation);
            ++i;
         }
      }

      this.sendRecipes(ClientboundRecipePacket.State.REMOVE, pPlayer, list);
      return i;
   }

   private void sendRecipes(ClientboundRecipePacket.State pState, ServerPlayer pPlayer, List<ResourceLocation> pRecipes) {
      pPlayer.connection.send(new ClientboundRecipePacket(pState, pRecipes, Collections.emptyList(), this.getBookSettings()));
   }

   public CompoundTag toNbt() {
      CompoundTag compoundtag = new CompoundTag();
      this.getBookSettings().write(compoundtag);
      ListTag listtag = new ListTag();

      for(ResourceLocation resourcelocation : this.known) {
         listtag.add(StringTag.valueOf(resourcelocation.toString()));
      }

      compoundtag.put("recipes", listtag);
      ListTag listtag1 = new ListTag();

      for(ResourceLocation resourcelocation1 : this.highlight) {
         listtag1.add(StringTag.valueOf(resourcelocation1.toString()));
      }

      compoundtag.put("toBeDisplayed", listtag1);
      return compoundtag;
   }

   public void fromNbt(CompoundTag pTag, RecipeManager pRecipeManager) {
      this.setBookSettings(RecipeBookSettings.read(pTag));
      ListTag listtag = pTag.getList("recipes", 8);
      this.loadRecipes(listtag, this::add, pRecipeManager);
      ListTag listtag1 = pTag.getList("toBeDisplayed", 8);
      this.loadRecipes(listtag1, this::addHighlight, pRecipeManager);
   }

   private void loadRecipes(ListTag pTags, Consumer<Recipe<?>> pRecipeConsumer, RecipeManager pRecipeManager) {
      for(int i = 0; i < pTags.size(); ++i) {
         String s = pTags.getString(i);

         try {
            ResourceLocation resourcelocation = new ResourceLocation(s);
            Optional<? extends Recipe<?>> optional = pRecipeManager.byKey(resourcelocation);
            if (!optional.isPresent()) {
               LOGGER.error("Tried to load unrecognized recipe: {} removed now.", (Object)resourcelocation);
            } else {
               pRecipeConsumer.accept(optional.get());
            }
         } catch (ResourceLocationException resourcelocationexception) {
            LOGGER.error("Tried to load improperly formatted recipe: {} removed now.", (Object)s);
         }
      }

   }

   public void sendInitialRecipeBook(ServerPlayer pPlayer) {
      pPlayer.connection.send(new ClientboundRecipePacket(ClientboundRecipePacket.State.INIT, this.known, this.highlight, this.getBookSettings()));
   }
}