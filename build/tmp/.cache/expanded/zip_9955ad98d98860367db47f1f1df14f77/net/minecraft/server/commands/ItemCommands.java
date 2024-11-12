package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ItemCommands {
   static final Dynamic3CommandExceptionType ERROR_TARGET_NOT_A_CONTAINER = new Dynamic3CommandExceptionType((p_180355_, p_180356_, p_180357_) -> {
      return Component.translatable("commands.item.target.not_a_container", p_180355_, p_180356_, p_180357_);
   });
   private static final Dynamic3CommandExceptionType ERROR_SOURCE_NOT_A_CONTAINER = new Dynamic3CommandExceptionType((p_180347_, p_180348_, p_180349_) -> {
      return Component.translatable("commands.item.source.not_a_container", p_180347_, p_180348_, p_180349_);
   });
   static final DynamicCommandExceptionType ERROR_TARGET_INAPPLICABLE_SLOT = new DynamicCommandExceptionType((p_180361_) -> {
      return Component.translatable("commands.item.target.no_such_slot", p_180361_);
   });
   private static final DynamicCommandExceptionType ERROR_SOURCE_INAPPLICABLE_SLOT = new DynamicCommandExceptionType((p_180353_) -> {
      return Component.translatable("commands.item.source.no_such_slot", p_180353_);
   });
   private static final DynamicCommandExceptionType ERROR_TARGET_NO_CHANGES = new DynamicCommandExceptionType((p_180342_) -> {
      return Component.translatable("commands.item.target.no_changes", p_180342_);
   });
   private static final Dynamic2CommandExceptionType ERROR_TARGET_NO_CHANGES_KNOWN_ITEM = new Dynamic2CommandExceptionType((p_180344_, p_180345_) -> {
      return Component.translatable("commands.item.target.no_changed.known_item", p_180344_, p_180345_);
   });
   private static final SuggestionProvider<CommandSourceStack> SUGGEST_MODIFIER = (p_278910_, p_278911_) -> {
      LootDataManager lootdatamanager = p_278910_.getSource().getServer().getLootData();
      return SharedSuggestionProvider.suggestResource(lootdatamanager.getKeys(LootDataType.MODIFIER), p_278911_);
   };

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher, CommandBuildContext pContext) {
      pDispatcher.register(Commands.literal("item").requires((p_180256_) -> {
         return p_180256_.hasPermission(2);
      }).then(Commands.literal("replace").then(Commands.literal("block").then(Commands.argument("pos", BlockPosArgument.blockPos()).then(Commands.argument("slot", SlotArgument.slot()).then(Commands.literal("with").then(Commands.argument("item", ItemArgument.item(pContext)).executes((p_180383_) -> {
         return setBlockItem(p_180383_.getSource(), BlockPosArgument.getLoadedBlockPos(p_180383_, "pos"), SlotArgument.getSlot(p_180383_, "slot"), ItemArgument.getItem(p_180383_, "item").createItemStack(1, false));
      }).then(Commands.argument("count", IntegerArgumentType.integer(1, 64)).executes((p_180381_) -> {
         return setBlockItem(p_180381_.getSource(), BlockPosArgument.getLoadedBlockPos(p_180381_, "pos"), SlotArgument.getSlot(p_180381_, "slot"), ItemArgument.getItem(p_180381_, "item").createItemStack(IntegerArgumentType.getInteger(p_180381_, "count"), true));
      })))).then(Commands.literal("from").then(Commands.literal("block").then(Commands.argument("source", BlockPosArgument.blockPos()).then(Commands.argument("sourceSlot", SlotArgument.slot()).executes((p_180379_) -> {
         return blockToBlock(p_180379_.getSource(), BlockPosArgument.getLoadedBlockPos(p_180379_, "source"), SlotArgument.getSlot(p_180379_, "sourceSlot"), BlockPosArgument.getLoadedBlockPos(p_180379_, "pos"), SlotArgument.getSlot(p_180379_, "slot"));
      }).then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes((p_180377_) -> {
         return blockToBlock(p_180377_.getSource(), BlockPosArgument.getLoadedBlockPos(p_180377_, "source"), SlotArgument.getSlot(p_180377_, "sourceSlot"), BlockPosArgument.getLoadedBlockPos(p_180377_, "pos"), SlotArgument.getSlot(p_180377_, "slot"), ResourceLocationArgument.getItemModifier(p_180377_, "modifier"));
      }))))).then(Commands.literal("entity").then(Commands.argument("source", EntityArgument.entity()).then(Commands.argument("sourceSlot", SlotArgument.slot()).executes((p_180375_) -> {
         return entityToBlock(p_180375_.getSource(), EntityArgument.getEntity(p_180375_, "source"), SlotArgument.getSlot(p_180375_, "sourceSlot"), BlockPosArgument.getLoadedBlockPos(p_180375_, "pos"), SlotArgument.getSlot(p_180375_, "slot"));
      }).then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes((p_180373_) -> {
         return entityToBlock(p_180373_.getSource(), EntityArgument.getEntity(p_180373_, "source"), SlotArgument.getSlot(p_180373_, "sourceSlot"), BlockPosArgument.getLoadedBlockPos(p_180373_, "pos"), SlotArgument.getSlot(p_180373_, "slot"), ResourceLocationArgument.getItemModifier(p_180373_, "modifier"));
      }))))))))).then(Commands.literal("entity").then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("slot", SlotArgument.slot()).then(Commands.literal("with").then(Commands.argument("item", ItemArgument.item(pContext)).executes((p_180371_) -> {
         return setEntityItem(p_180371_.getSource(), EntityArgument.getEntities(p_180371_, "targets"), SlotArgument.getSlot(p_180371_, "slot"), ItemArgument.getItem(p_180371_, "item").createItemStack(1, false));
      }).then(Commands.argument("count", IntegerArgumentType.integer(1, 64)).executes((p_180369_) -> {
         return setEntityItem(p_180369_.getSource(), EntityArgument.getEntities(p_180369_, "targets"), SlotArgument.getSlot(p_180369_, "slot"), ItemArgument.getItem(p_180369_, "item").createItemStack(IntegerArgumentType.getInteger(p_180369_, "count"), true));
      })))).then(Commands.literal("from").then(Commands.literal("block").then(Commands.argument("source", BlockPosArgument.blockPos()).then(Commands.argument("sourceSlot", SlotArgument.slot()).executes((p_180367_) -> {
         return blockToEntities(p_180367_.getSource(), BlockPosArgument.getLoadedBlockPos(p_180367_, "source"), SlotArgument.getSlot(p_180367_, "sourceSlot"), EntityArgument.getEntities(p_180367_, "targets"), SlotArgument.getSlot(p_180367_, "slot"));
      }).then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes((p_180365_) -> {
         return blockToEntities(p_180365_.getSource(), BlockPosArgument.getLoadedBlockPos(p_180365_, "source"), SlotArgument.getSlot(p_180365_, "sourceSlot"), EntityArgument.getEntities(p_180365_, "targets"), SlotArgument.getSlot(p_180365_, "slot"), ResourceLocationArgument.getItemModifier(p_180365_, "modifier"));
      }))))).then(Commands.literal("entity").then(Commands.argument("source", EntityArgument.entity()).then(Commands.argument("sourceSlot", SlotArgument.slot()).executes((p_180363_) -> {
         return entityToEntities(p_180363_.getSource(), EntityArgument.getEntity(p_180363_, "source"), SlotArgument.getSlot(p_180363_, "sourceSlot"), EntityArgument.getEntities(p_180363_, "targets"), SlotArgument.getSlot(p_180363_, "slot"));
      }).then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes((p_180359_) -> {
         return entityToEntities(p_180359_.getSource(), EntityArgument.getEntity(p_180359_, "source"), SlotArgument.getSlot(p_180359_, "sourceSlot"), EntityArgument.getEntities(p_180359_, "targets"), SlotArgument.getSlot(p_180359_, "slot"), ResourceLocationArgument.getItemModifier(p_180359_, "modifier"));
      })))))))))).then(Commands.literal("modify").then(Commands.literal("block").then(Commands.argument("pos", BlockPosArgument.blockPos()).then(Commands.argument("slot", SlotArgument.slot()).then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes((p_180351_) -> {
         return modifyBlockItem(p_180351_.getSource(), BlockPosArgument.getLoadedBlockPos(p_180351_, "pos"), SlotArgument.getSlot(p_180351_, "slot"), ResourceLocationArgument.getItemModifier(p_180351_, "modifier"));
      }))))).then(Commands.literal("entity").then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("slot", SlotArgument.slot()).then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes((p_180251_) -> {
         return modifyEntityItem(p_180251_.getSource(), EntityArgument.getEntities(p_180251_, "targets"), SlotArgument.getSlot(p_180251_, "slot"), ResourceLocationArgument.getItemModifier(p_180251_, "modifier"));
      })))))));
   }

   private static int modifyBlockItem(CommandSourceStack pSource, BlockPos pPos, int pSlot, LootItemFunction pModifier) throws CommandSyntaxException {
      Container container = getContainer(pSource, pPos, ERROR_TARGET_NOT_A_CONTAINER);
      if (pSlot >= 0 && pSlot < container.getContainerSize()) {
         ItemStack itemstack = applyModifier(pSource, pModifier, container.getItem(pSlot));
         container.setItem(pSlot, itemstack);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.item.block.set.success", pPos.getX(), pPos.getY(), pPos.getZ(), itemstack.getDisplayName());
         }, true);
         return 1;
      } else {
         throw ERROR_TARGET_INAPPLICABLE_SLOT.create(pSlot);
      }
   }

   private static int modifyEntityItem(CommandSourceStack pSource, Collection<? extends Entity> pTargets, int pSlot, LootItemFunction pModifier) throws CommandSyntaxException {
      Map<Entity, ItemStack> map = Maps.newHashMapWithExpectedSize(pTargets.size());

      for(Entity entity : pTargets) {
         SlotAccess slotaccess = entity.getSlot(pSlot);
         if (slotaccess != SlotAccess.NULL) {
            ItemStack itemstack = applyModifier(pSource, pModifier, slotaccess.get().copy());
            if (slotaccess.set(itemstack)) {
               map.put(entity, itemstack);
               if (entity instanceof ServerPlayer) {
                  ((ServerPlayer)entity).containerMenu.broadcastChanges();
               }
            }
         }
      }

      if (map.isEmpty()) {
         throw ERROR_TARGET_NO_CHANGES.create(pSlot);
      } else {
         if (map.size() == 1) {
            Map.Entry<Entity, ItemStack> entry = map.entrySet().iterator().next();
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.item.entity.set.success.single", entry.getKey().getDisplayName(), entry.getValue().getDisplayName());
            }, true);
         } else {
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.item.entity.set.success.multiple", map.size());
            }, true);
         }

         return map.size();
      }
   }

   private static int setBlockItem(CommandSourceStack pSource, BlockPos pPos, int pSlot, ItemStack pItem) throws CommandSyntaxException {
      Container container = getContainer(pSource, pPos, ERROR_TARGET_NOT_A_CONTAINER);
      if (pSlot >= 0 && pSlot < container.getContainerSize()) {
         container.setItem(pSlot, pItem);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.item.block.set.success", pPos.getX(), pPos.getY(), pPos.getZ(), pItem.getDisplayName());
         }, true);
         return 1;
      } else {
         throw ERROR_TARGET_INAPPLICABLE_SLOT.create(pSlot);
      }
   }

   private static Container getContainer(CommandSourceStack pSource, BlockPos pPos, Dynamic3CommandExceptionType pException) throws CommandSyntaxException {
      BlockEntity blockentity = pSource.getLevel().getBlockEntity(pPos);
      if (!(blockentity instanceof Container)) {
         throw pException.create(pPos.getX(), pPos.getY(), pPos.getZ());
      } else {
         return (Container)blockentity;
      }
   }

   private static int setEntityItem(CommandSourceStack pSource, Collection<? extends Entity> pTargets, int pSlot, ItemStack pItem) throws CommandSyntaxException {
      List<Entity> list = Lists.newArrayListWithCapacity(pTargets.size());

      for(Entity entity : pTargets) {
         SlotAccess slotaccess = entity.getSlot(pSlot);
         if (slotaccess != SlotAccess.NULL && slotaccess.set(pItem.copy())) {
            list.add(entity);
            if (entity instanceof ServerPlayer) {
               ((ServerPlayer)entity).containerMenu.broadcastChanges();
            }
         }
      }

      if (list.isEmpty()) {
         throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(pItem.getDisplayName(), pSlot);
      } else {
         if (list.size() == 1) {
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.item.entity.set.success.single", list.iterator().next().getDisplayName(), pItem.getDisplayName());
            }, true);
         } else {
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.item.entity.set.success.multiple", list.size(), pItem.getDisplayName());
            }, true);
         }

         return list.size();
      }
   }

   private static int blockToEntities(CommandSourceStack pSource, BlockPos pPos, int pSourceSlot, Collection<? extends Entity> pTargets, int pSlot) throws CommandSyntaxException {
      return setEntityItem(pSource, pTargets, pSlot, getBlockItem(pSource, pPos, pSourceSlot));
   }

   private static int blockToEntities(CommandSourceStack pSource, BlockPos pPos, int pSourceSlot, Collection<? extends Entity> pTargets, int pSlot, LootItemFunction pModifier) throws CommandSyntaxException {
      return setEntityItem(pSource, pTargets, pSlot, applyModifier(pSource, pModifier, getBlockItem(pSource, pPos, pSourceSlot)));
   }

   private static int blockToBlock(CommandSourceStack pSource, BlockPos pSourcePos, int pSourceSlot, BlockPos pPos, int pSlot) throws CommandSyntaxException {
      return setBlockItem(pSource, pPos, pSlot, getBlockItem(pSource, pSourcePos, pSourceSlot));
   }

   private static int blockToBlock(CommandSourceStack pSource, BlockPos pSourcePos, int pSourceSlot, BlockPos pPos, int pSlot, LootItemFunction pModifier) throws CommandSyntaxException {
      return setBlockItem(pSource, pPos, pSlot, applyModifier(pSource, pModifier, getBlockItem(pSource, pSourcePos, pSourceSlot)));
   }

   private static int entityToBlock(CommandSourceStack pSource, Entity pSourceEntity, int pSourceSlot, BlockPos pPos, int pSlot) throws CommandSyntaxException {
      return setBlockItem(pSource, pPos, pSlot, getEntityItem(pSourceEntity, pSourceSlot));
   }

   private static int entityToBlock(CommandSourceStack pSource, Entity pSourceEntity, int pSourceSlot, BlockPos pPos, int pSlot, LootItemFunction pModifier) throws CommandSyntaxException {
      return setBlockItem(pSource, pPos, pSlot, applyModifier(pSource, pModifier, getEntityItem(pSourceEntity, pSourceSlot)));
   }

   private static int entityToEntities(CommandSourceStack pSource, Entity pSourceEntity, int pSourceSlot, Collection<? extends Entity> pTargets, int pSlot) throws CommandSyntaxException {
      return setEntityItem(pSource, pTargets, pSlot, getEntityItem(pSourceEntity, pSourceSlot));
   }

   private static int entityToEntities(CommandSourceStack pSource, Entity pSourceEntity, int pSourceSlot, Collection<? extends Entity> pTargets, int pSlot, LootItemFunction pModifier) throws CommandSyntaxException {
      return setEntityItem(pSource, pTargets, pSlot, applyModifier(pSource, pModifier, getEntityItem(pSourceEntity, pSourceSlot)));
   }

   private static ItemStack applyModifier(CommandSourceStack pSource, LootItemFunction pModifier, ItemStack pItem) {
      ServerLevel serverlevel = pSource.getLevel();
      LootParams lootparams = (new LootParams.Builder(serverlevel)).withParameter(LootContextParams.ORIGIN, pSource.getPosition()).withOptionalParameter(LootContextParams.THIS_ENTITY, pSource.getEntity()).create(LootContextParamSets.COMMAND);
      LootContext lootcontext = (new LootContext.Builder(lootparams)).create((ResourceLocation)null);
      lootcontext.pushVisitedElement(LootContext.createVisitedEntry(pModifier));
      return pModifier.apply(pItem, lootcontext);
   }

   private static ItemStack getEntityItem(Entity pEntity, int pSlot) throws CommandSyntaxException {
      SlotAccess slotaccess = pEntity.getSlot(pSlot);
      if (slotaccess == SlotAccess.NULL) {
         throw ERROR_SOURCE_INAPPLICABLE_SLOT.create(pSlot);
      } else {
         return slotaccess.get().copy();
      }
   }

   private static ItemStack getBlockItem(CommandSourceStack pSource, BlockPos pPos, int pSlot) throws CommandSyntaxException {
      Container container = getContainer(pSource, pPos, ERROR_SOURCE_NOT_A_CONTAINER);
      if (pSlot >= 0 && pSlot < container.getContainerSize()) {
         return container.getItem(pSlot).copy();
      } else {
         throw ERROR_SOURCE_INAPPLICABLE_SLOT.create(pSlot);
      }
   }
}