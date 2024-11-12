package net.minecraft.world.item;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;

public class BlockItem extends Item {
   public static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
   public static final String BLOCK_STATE_TAG = "BlockStateTag";
   /** @deprecated */
   @Deprecated
   private final Block block;

   public BlockItem(Block pBlock, Item.Properties pProperties) {
      super(pProperties);
      this.block = pBlock;
   }

   /**
    * Called when this item is used when targeting a Block
    */
   public InteractionResult useOn(UseOnContext pContext) {
      InteractionResult interactionresult = this.place(new BlockPlaceContext(pContext));
      if (!interactionresult.consumesAction() && this.isEdible()) {
         InteractionResult interactionresult1 = this.use(pContext.getLevel(), pContext.getPlayer(), pContext.getHand()).getResult();
         return interactionresult1 == InteractionResult.CONSUME ? InteractionResult.CONSUME_PARTIAL : interactionresult1;
      } else {
         return interactionresult;
      }
   }

   public InteractionResult place(BlockPlaceContext pContext) {
      if (!this.getBlock().isEnabled(pContext.getLevel().enabledFeatures())) {
         return InteractionResult.FAIL;
      } else if (!pContext.canPlace()) {
         return InteractionResult.FAIL;
      } else {
         BlockPlaceContext blockplacecontext = this.updatePlacementContext(pContext);
         if (blockplacecontext == null) {
            return InteractionResult.FAIL;
         } else {
            BlockState blockstate = this.getPlacementState(blockplacecontext);
            if (blockstate == null) {
               return InteractionResult.FAIL;
            } else if (!this.placeBlock(blockplacecontext, blockstate)) {
               return InteractionResult.FAIL;
            } else {
               BlockPos blockpos = blockplacecontext.getClickedPos();
               Level level = blockplacecontext.getLevel();
               Player player = blockplacecontext.getPlayer();
               ItemStack itemstack = blockplacecontext.getItemInHand();
               BlockState blockstate1 = level.getBlockState(blockpos);
               if (blockstate1.is(blockstate.getBlock())) {
                  blockstate1 = this.updateBlockStateFromTag(blockpos, level, itemstack, blockstate1);
                  this.updateCustomBlockEntityTag(blockpos, level, player, itemstack, blockstate1);
                  blockstate1.getBlock().setPlacedBy(level, blockpos, blockstate1, player, itemstack);
                  if (player instanceof ServerPlayer) {
                     CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, blockpos, itemstack);
                  }
               }

               SoundType soundtype = blockstate1.getSoundType(level, blockpos, pContext.getPlayer());
               level.playSound(player, blockpos, this.getPlaceSound(blockstate1, level, blockpos, pContext.getPlayer()), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
               level.gameEvent(GameEvent.BLOCK_PLACE, blockpos, GameEvent.Context.of(player, blockstate1));
               if (player == null || !player.getAbilities().instabuild) {
                  itemstack.shrink(1);
               }

               return InteractionResult.sidedSuccess(level.isClientSide);
            }
         }
      }
   }

   @Deprecated //Forge: Use more sensitive version {@link BlockItem#getPlaceSound(BlockState, IBlockReader, BlockPos, Entity) }
   protected SoundEvent getPlaceSound(BlockState pState) {
      return pState.getSoundType().getPlaceSound();
   }

   //Forge: Sensitive version of BlockItem#getPlaceSound
   protected SoundEvent getPlaceSound(BlockState state, Level world, BlockPos pos, Player entity) {
      return state.getSoundType(world, pos, entity).getPlaceSound();
   }

   @Nullable
   public BlockPlaceContext updatePlacementContext(BlockPlaceContext pContext) {
      return pContext;
   }

   protected boolean updateCustomBlockEntityTag(BlockPos pPos, Level pLevel, @Nullable Player pPlayer, ItemStack pStack, BlockState pState) {
      return updateCustomBlockEntityTag(pLevel, pPlayer, pPos, pStack);
   }

   @Nullable
   protected BlockState getPlacementState(BlockPlaceContext pContext) {
      BlockState blockstate = this.getBlock().getStateForPlacement(pContext);
      return blockstate != null && this.canPlace(pContext, blockstate) ? blockstate : null;
   }

   private BlockState updateBlockStateFromTag(BlockPos pPos, Level pLevel, ItemStack pStack, BlockState pState) {
      BlockState blockstate = pState;
      CompoundTag compoundtag = pStack.getTag();
      if (compoundtag != null) {
         CompoundTag compoundtag1 = compoundtag.getCompound("BlockStateTag");
         StateDefinition<Block, BlockState> statedefinition = pState.getBlock().getStateDefinition();

         for(String s : compoundtag1.getAllKeys()) {
            Property<?> property = statedefinition.getProperty(s);
            if (property != null) {
               String s1 = compoundtag1.get(s).getAsString();
               blockstate = updateState(blockstate, property, s1);
            }
         }
      }

      if (blockstate != pState) {
         pLevel.setBlock(pPos, blockstate, 2);
      }

      return blockstate;
   }

   private static <T extends Comparable<T>> BlockState updateState(BlockState pState, Property<T> pProperty, String pValueIdentifier) {
      return pProperty.getValue(pValueIdentifier).map((p_40592_) -> {
         return pState.setValue(pProperty, p_40592_);
      }).orElse(pState);
   }

   protected boolean canPlace(BlockPlaceContext pContext, BlockState pState) {
      Player player = pContext.getPlayer();
      CollisionContext collisioncontext = player == null ? CollisionContext.empty() : CollisionContext.of(player);
      return (!this.mustSurvive() || pState.canSurvive(pContext.getLevel(), pContext.getClickedPos())) && pContext.getLevel().isUnobstructed(pState, pContext.getClickedPos(), collisioncontext);
   }

   protected boolean mustSurvive() {
      return true;
   }

   protected boolean placeBlock(BlockPlaceContext pContext, BlockState pState) {
      return pContext.getLevel().setBlock(pContext.getClickedPos(), pState, 11);
   }

   public static boolean updateCustomBlockEntityTag(Level pLevel, @Nullable Player pPlayer, BlockPos pPos, ItemStack pStack) {
      MinecraftServer minecraftserver = pLevel.getServer();
      if (minecraftserver == null) {
         return false;
      } else {
         CompoundTag compoundtag = getBlockEntityData(pStack);
         if (compoundtag != null) {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);
            if (blockentity != null) {
               if (!pLevel.isClientSide && blockentity.onlyOpCanSetNbt() && (pPlayer == null || !pPlayer.canUseGameMasterBlocks())) {
                  return false;
               }

               CompoundTag compoundtag1 = blockentity.saveWithoutMetadata();
               CompoundTag compoundtag2 = compoundtag1.copy();
               compoundtag1.merge(compoundtag);
               if (!compoundtag1.equals(compoundtag2)) {
                  blockentity.load(compoundtag1);
                  blockentity.setChanged();
                  return true;
               }
            }
         }

         return false;
      }
   }

   /**
    * Returns the unlocalized name of this item.
    */
   public String getDescriptionId() {
      return this.getBlock().getDescriptionId();
   }

   /**
    * Allows items to add custom lines of information to the mouseover description.
    */
   public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
      super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
      this.getBlock().appendHoverText(pStack, pLevel, pTooltip, pFlag);
   }

   public Block getBlock() {
      return this.getBlockRaw() == null ? null : net.minecraftforge.registries.ForgeRegistries.BLOCKS.getDelegateOrThrow(this.getBlockRaw()).get();
   }

   private Block getBlockRaw() {
      return this.block;
   }

   public void registerBlocks(Map<Block, Item> pBlockToItemMap, Item pItem) {
      pBlockToItemMap.put(this.getBlock(), pItem);
   }

   public void removeFromBlockToItemMap(Map<Block, Item> blockToItemMap, Item itemIn) {
      blockToItemMap.remove(this.getBlock());
   }

   public boolean canFitInsideContainerItems() {
      return !(this.block instanceof ShulkerBoxBlock);
   }

   public void onDestroyed(ItemEntity pItemEntity) {
      if (this.block instanceof ShulkerBoxBlock) {
         ItemStack itemstack = pItemEntity.getItem();
         CompoundTag compoundtag = getBlockEntityData(itemstack);
         if (compoundtag != null && compoundtag.contains("Items", 9)) {
            ListTag listtag = compoundtag.getList("Items", 10);
            ItemUtils.onContainerDestroyed(pItemEntity, listtag.stream().map(CompoundTag.class::cast).map(ItemStack::of));
         }
      }

   }

   @Nullable
   public static CompoundTag getBlockEntityData(ItemStack pStack) {
      return pStack.getTagElement("BlockEntityTag");
   }

   public static void setBlockEntityData(ItemStack pStack, BlockEntityType<?> pBlockEntityType, CompoundTag pBlockEntityData) {
      if (pBlockEntityData.isEmpty()) {
         pStack.removeTagKey("BlockEntityTag");
      } else {
         BlockEntity.addEntityType(pBlockEntityData, pBlockEntityType);
         pStack.addTagElement("BlockEntityTag", pBlockEntityData);
      }

   }

   public FeatureFlagSet requiredFeatures() {
      return this.getBlock().requiredFeatures();
   }
}
