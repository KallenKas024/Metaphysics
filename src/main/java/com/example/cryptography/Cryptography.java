package com.example.cryptography;
import com.example.cryptography.Handler.KeyInputHandler;
import com.example.cryptography.Network.PacketManager;
import com.example.cryptography.Peripherals.Block.KeyboardBlock;
import com.example.cryptography.Registrys.RegistryHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.HashMap;
import java.util.Map;

@Mod("cryptography")
public class Cryptography {

    public static Map<Integer, BlockPos> ComputerPosMapper = new HashMap<>();
    public static Map<String, Boolean> ComputerStatusMapper = new HashMap<>();
    public static final boolean isUsePhoneAPI = false;
    public static final int MAX_SCOPE = 2500;
    public static final String modid = "cryptography";
    public static IEventBus eventBus;

    public Cryptography() {
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        //RegistryHelper.register(eventBus);
        //eventBus.addListener(this::addCreative);
        this.eventBus = eventBus;
        PacketManager.Init();
    }

    @SubscribeEvent
    public void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(RegistryHelper.KEYBOARD_BLOCK_REGISTRY_OBJECT.get().asItem());
        }
    }
}
