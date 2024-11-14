package com.example.cryptography;
import com.example.cryptography.Peripherals.Block.KeyboardBlock;
import com.example.cryptography.Peripherals.BlockEntity.BlockEntityTypes.KeyboardEntityType;
import com.example.cryptography.Peripherals.BlockEntity.KeyboardEntity;
import com.example.cryptography.Registrys.RegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mod("KSI-KallenSpaceIndustry")
public class Cryptography {

    public static Map<Integer, BlockPos> ComputerPosMapper = new HashMap<>();
    public static Map<String, Boolean> ComputerStatusMapper = new HashMap<>();
    public static final boolean isUsePhoneAPI = false;
    public static final int MAX_SCOPE = 2500;
    public static final String modid = "kallenspaceindustry";

    public Cryptography() {
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        RegistryHelper.register(eventBus);
        eventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(RegistryHelper.KEYBOARD_BLOCK_REGISTRY_OBJECT.get());
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getPlacedBlock().getBlock() == RegistryHelper.KEYBOARD_BLOCK_REGISTRY_OBJECT.get()) {
            event.(event.getPos(), new KeyboardEntity(KeyboardEntityType.KEYBOARD_ENTITY_BLOCK_ENTITY_TYPE, event.getPos(), event.getPlacedBlock()));
        }
    }
}
