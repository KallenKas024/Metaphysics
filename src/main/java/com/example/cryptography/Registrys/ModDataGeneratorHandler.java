package com.example.cryptography.Registrys;

import com.example.cryptography.Peripherals.Provider.BlockStateProvider.KeyboardBlockStateProvider;
import com.example.cryptography.Peripherals.Provider.RecipeProvider.KeyboardRecipeProvider;
import net.minecraft.data.DataProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "cryptography", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModDataGeneratorHandler {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        ExistingFileHelper efh = event.getExistingFileHelper();
        event.getGenerator().addProvider(event.includeClient(), (DataProvider.Factory<?>) pOutput -> new KeyboardBlockStateProvider(pOutput, "cryptography", efh));
        event.getGenerator().addProvider(event.includeClient(), (DataProvider.Factory<?>) KeyboardRecipeProvider::new);
    }
}
