package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FlowerPotBlock extends Block {
   private static final Map<Block, Block> POTTED_BY_CONTENT = Maps.newHashMap();
   public static final float AABB_SIZE = 3.0F;
   protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D);
   private final Block content;

   @Deprecated // Mods should use the constructor below
   public FlowerPotBlock(Block pContent, BlockBehaviour.Properties pProperties) {
       this(Blocks.FLOWER_POT == null ? null : () -> (FlowerPotBlock) net.minecraftforge.registries.ForgeRegistries.BLOCKS.getDelegateOrThrow(Blocks.FLOWER_POT).get(), () -> net.minecraftforge.registries.ForgeRegistries.BLOCKS.getDelegateOrThrow(pContent).get(), pProperties);
       if (Blocks.FLOWER_POT != null) {
           ((FlowerPotBlock)Blocks.FLOWER_POT).addPlant(net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(pContent), () -> this);
       }
   }

   /**
    * For mod use, eliminates the need to extend this class, and prevents modded
    * flower pots from altering vanilla behavior.
    *
    * @param emptyPot    The empty pot for this pot, or null for self.
    * @param pContent The flower block.
    * @param properties
    */
   public FlowerPotBlock(@org.jetbrains.annotations.Nullable java.util.function.Supplier<FlowerPotBlock> emptyPot, java.util.function.Supplier<? extends Block> pContent, BlockBehaviour.Properties properties) {
      super(properties);
      this.content = null; // Unused, redirected by coremod
      this.flowerDelegate = pContent;
      if (emptyPot == null) {
         this.fullPots = Maps.newHashMap();
         this.emptyPot = null;
      } else {
         this.fullPots = java.util.Collections.emptyMap();
         this.emptyPot = emptyPot;
      }
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE;
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getRenderShape}
    * whenever possible. Implementing/overriding is fine.
    */
   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.MODEL;
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      Item item = itemstack.getItem();
      BlockState blockstate = (item instanceof BlockItem ? getEmptyPot().fullPots.getOrDefault(net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(((BlockItem)item).getBlock()), net.minecraftforge.registries.ForgeRegistries.BLOCKS.getDelegateOrThrow(Blocks.AIR)).get() : Blocks.AIR).defaultBlockState();
      boolean flag = blockstate.is(Blocks.AIR);
      boolean flag1 = this.isEmpty();
      if (flag != flag1) {
         if (flag1) {
            pLevel.setBlock(pPos, blockstate, 3);
            pPlayer.awardStat(Stats.POT_FLOWER);
            if (!pPlayer.getAbilities().instabuild) {
               itemstack.shrink(1);
            }
         } else {
            ItemStack itemstack1 = new ItemStack(this.content);
            if (itemstack.isEmpty()) {
               pPlayer.setItemInHand(pHand, itemstack1);
            } else if (!pPlayer.addItem(itemstack1)) {
               pPlayer.drop(itemstack1, false);
            }

            pLevel.setBlock(pPos, getEmptyPot().defaultBlockState(), 3);
         }

         pLevel.gameEvent(pPlayer, GameEvent.BLOCK_CHANGE, pPos);
         return InteractionResult.sidedSuccess(pLevel.isClientSide);
      } else {
         return InteractionResult.CONSUME;
      }
   }

   public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
      return this.isEmpty() ? super.getCloneItemStack(pLevel, pPos, pState) : new ItemStack(this.content);
   }

   private boolean isEmpty() {
      return this.content == Blocks.AIR;
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return pFacing == Direction.DOWN && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public Block getContent() {
      return flowerDelegate.get();
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }

   //Forge Start
   private final Map<net.minecraft.resources.ResourceLocation, java.util.function.Supplier<? extends Block>> fullPots;
   private final java.util.function.Supplier<FlowerPotBlock> emptyPot;
   private final java.util.function.Supplier<? extends Block> flowerDelegate;

   public FlowerPotBlock getEmptyPot() {
       return emptyPot == null ? this : emptyPot.get();
   }

   public void addPlant(net.minecraft.resources.ResourceLocation flower, java.util.function.Supplier<? extends Block> fullPot) {
       if (getEmptyPot() != this) {
           throw new IllegalArgumentException("Cannot add plant to non-empty pot: " + this);
       }
       fullPots.put(flower, fullPot);
   }

   public Map<net.minecraft.resources.ResourceLocation, java.util.function.Supplier<? extends Block>> getFullPotsView() {
      return java.util.Collections.unmodifiableMap(fullPots);
   }
   //Forge End
}
