package com.example.cryptography.mixin;

import com.example.cryptography.bridge.ServerComputerBridge;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerComputer.class)
public class Mixin_ServerComputer implements ServerComputerBridge {
    @Shadow(remap = false)
    @Final
    private Computer computer;

    @Override
    public Computer bridge$cryptography$computer() {
        return this.computer;
    }
}
