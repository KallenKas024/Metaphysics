package com.example.cryptography.Peripherals.Provider.BlockStateProvider;

import com.example.cryptography.Registrys.RegistryHelper;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class KeyboardBlockStateProvider extends BlockStateProvider {
    public KeyboardBlockStateProvider(PackOutput output, String modid, ExistingFileHelper exFileHelper) {
        super(output, modid, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(RegistryHelper.KEYBOARD_BLOCK_REGISTRY_OBJECT.get(), models().cubeColumn("keyboard", blockTexture(RegistryHelper.KEYBOARD_BLOCK_REGISTRY_OBJECT.get()),blockTexture(RegistryHelper.KEYBOARD_BLOCK_REGISTRY_OBJECT.get())));
    }
}