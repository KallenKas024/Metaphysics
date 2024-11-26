package com.example.cryptography.Registrys;

import com.example.cryptography.Peripherals.Block.KeyboardBlock;
import com.example.cryptography.Peripherals.BlockEntity.KeyboardEntity;
import com.example.cryptography.Peripherals.Provider.RecipeProvider.KeyboardRecipeProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static com.example.cryptography.Cryptography.modid;

public class RegistryHelper {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, modid);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, modid);
    public static final RegistryObject<KeyboardBlock> KEYBOARD_BLOCK_REGISTRY_OBJECT = registerBlock("keyboard", () -> new KeyboardBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, modid);
    private static final RegistryObject<CreativeModeTab> FIRST_TAB = CREATIVE_MODE_TABS.register("first_tab", () -> CreativeModeTab.builder().icon(() -> new ItemStack(Items.NETHERITE_INGOT)).title(Component.nullToEmpty("KSI - Kallen Space Industry")).displayItems((pParameters, pOutput) -> {pOutput.accept(KEYBOARD_BLOCK_REGISTRY_OBJECT.get());}).build());
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, modid);
    public static final RegistryObject<BlockEntityType<KeyboardEntity>> KEYBOARD_BLOCK_ENTITY_TYPE = BLOCK_ENTITY_TYPES.register("keyboard_block_entity", () -> BlockEntityType.Builder.of(
            KeyboardEntity::new,
            KEYBOARD_BLOCK_REGISTRY_OBJECT.get()
    ).build(null));

    public static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    public static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static DeferredRegister<Block> getBlocks() {
        return BLOCKS;
    }

    public static DeferredRegister<Item> getItems() {
        return ITEMS;
    }

    public static DeferredRegister<CreativeModeTab> getCreativeModeTabs() {
        return CREATIVE_MODE_TABS;
    }

    public static DeferredRegister<BlockEntityType<?>> getBlockEntityTypes() {
        return BLOCK_ENTITY_TYPES;
    }

    public static void register(IEventBus eventBus){
        //CREATIVE_MODE_TABS.register(eventBus);
        //BLOCKS.register(eventBus);
        //ITEMS.register(eventBus);
        //BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
