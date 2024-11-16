package com.example.cryptography.Peripherals.Provider.BlockStateProvider;

import com.example.cryptography.Registrys.RegistryHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import static net.minecraftforge.client.ForgeRenderTypes.getText;

public class KeyboardBlockStateProvider extends BlockStateProvider {


    public KeyboardBlockStateProvider(PackOutput output, String modid, ExistingFileHelper exFileHelper) {
        super(output, modid, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        horizontalBlock(RegistryHelper.KEYBOARD_BLOCK_REGISTRY_OBJECT.get(), new ModelFile.UncheckedModelFile(new ResourceLocation("cryptography", "textures/block/keyboard_model.json")));
    }


}