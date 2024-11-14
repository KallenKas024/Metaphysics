package com.example.cryptography.mixin;

import com.example.cryptography.Apis.CoordinateAPI;
import com.example.cryptography.Apis.CryptographyAPI;
import com.example.cryptography.Apis.DatabaseAPI;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.util.ComponentMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.example.cryptography.Cryptography.ComputerPosMapper;

@Mixin(ServerComputer.class)
public abstract class TileComputerMixin {

    public BlockPos pos;

    @Final
    @Shadow(remap = false)
    private Computer computer;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void ServerComputer(ServerLevel level, BlockPos position, int computerID, String label, ComputerFamily family, int terminalWidth, int terminalHeight, ComponentMap baseComponents, CallbackInfo ci) {
        this.pos = position;
        ComputerPosMapper.put(computerID, pos);
        computer.addApi(new CryptographyAPI());
        computer.addApi(new CoordinateAPI(ComputerPosMapper.get(computerID), computerID, level, computer));
        computer.addApi(new DatabaseAPI(computerID, level));
    }
}