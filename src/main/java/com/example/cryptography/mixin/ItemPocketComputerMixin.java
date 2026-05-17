package com.example.cryptography.mixin;

import com.example.cryptography.CoordinatePocketComputerAPI;
import com.example.cryptography.CryptographyAPI;
import com.example.cryptography.bridge.ServerComputerBridge;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.shared.pocket.core.PocketBrain;
import dan200.computercraft.shared.pocket.core.PocketHolder;
import dan200.computercraft.shared.pocket.items.PocketComputerItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.example.cryptography.Cryptography.isUsePhoneAPI;

@Mixin(PocketComputerItem.class)
public abstract class ItemPocketComputerMixin {
    private boolean isCreateBrainCall = false;

    @Inject(method = "getOrCreateBrain", at = @At(
            value = "INVOKE",
            target = "Ldan200/computercraft/shared/pocket/core/PocketBrain;<init>(Ldan200/computercraft/shared/pocket/core/PocketHolder;Ldan200/computercraft/api/upgrades/UpgradeData;Ldan200/computercraft/shared/computer/core/ServerComputer$Properties;)V",
            remap = false
    ), remap = false)
    public void getOrCreateBrain$ahead(ServerLevel level, PocketHolder holder, ItemStack stack, CallbackInfoReturnable<PocketBrain> cir) {
        isCreateBrainCall = true;
    }

    @Inject(method = "getOrCreateBrain", at = @At(value = "RETURN"), remap = false)
    public void getOrCreateBrain(ServerLevel level, PocketHolder holder, ItemStack stack, CallbackInfoReturnable<PocketBrain> cir) {
        if (isCreateBrainCall) {
            isCreateBrainCall = false;
            Computer computer = ((ServerComputerBridge) (Object) cir.getReturnValue().computer()).bridge$cryptography$computer();
            computer.addApi(new CryptographyAPI());
            if (isUsePhoneAPI) {
                computer.addApi(new CoordinatePocketComputerAPI(holder, true));
            }
        }
    }
}
