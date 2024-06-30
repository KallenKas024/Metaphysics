package com.example.cryptography.mixin;

import com.example.cryptography.CoordinatePocketComputerAPI;
import com.example.cryptography.CryptographyAPI;
import dan200.computercraft.shared.pocket.core.PocketServerComputer;
import dan200.computercraft.shared.pocket.items.PocketComputerItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.example.cryptography.Cryptography.isUsePhoneAPI;

@Mixin(PocketComputerItem.class)
public abstract class ItemPocketComputerMixin {

    @Inject(method = "createServerComputer", at = @At("RETURN"), remap = false)
    public void createServerComputer(ServerLevel world, Entity entity, Container inventory, ItemStack stack, CallbackInfoReturnable<PocketServerComputer> cir) {
        cir.getReturnValue().addAPI(new CryptographyAPI());
        if (isUsePhoneAPI) {
            cir.getReturnValue().addAPI(new CoordinatePocketComputerAPI(entity, true));
        }
    }
}
