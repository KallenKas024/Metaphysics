package com.example.cryptography;

import com.example.cryptography.Peripherals.Provider.BlockStateProvider.KeyboardBlockStateProvider;
import com.example.cryptography.Peripherals.Provider.RecipeProvider.KeyboardRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

import static com.example.cryptography.Cryptography.modid;

@Mod.EventBusSubscriber(modid = modid, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        net.minecraft.data.DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        generator.addProvider(event.includeServer(), new KeyboardRecipeProvider(packOutput));
        generator.addProvider(event.includeServer(), new KeyboardBlockStateProvider(packOutput, modid, existingFileHelper));
    }
}
