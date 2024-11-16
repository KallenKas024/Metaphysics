package com.example.cryptography.Peripherals.Provider.RecipeProvider;

import com.example.cryptography.Registrys.RegistryHelper;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class KeyboardRecipeProvider extends RecipeProvider {
    public KeyboardRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, RegistryHelper.KEYBOARD_BLOCK_REGISTRY_OBJECT.get())
                .pattern("PSP")
                .pattern("IRI")
                .pattern("PPP")
                .define('S', Items.GLASS)
                .define('I', Items.NETHERITE_INGOT)
                .define('R', Items.COMPARATOR)
                .define('P', Items.IRON_BLOCK)
                .unlockedBy(getHasName(RegistryHelper.KEYBOARD_BLOCK_REGISTRY_OBJECT.get()), has(RegistryHelper.KEYBOARD_BLOCK_REGISTRY_OBJECT.get()))
                .save(pWriter);
    }
}
