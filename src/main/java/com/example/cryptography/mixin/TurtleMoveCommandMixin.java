package com.example.cryptography.mixin;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.shared.turtle.blocks.TurtleBlockEntity;
import dan200.computercraft.shared.turtle.core.TurtleBrain;
import dan200.computercraft.shared.turtle.core.TurtleMoveCommand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.example.cryptography.Cryptography.ComputerPosMapper;

@Mixin(TurtleMoveCommand.class)
public abstract class TurtleMoveCommandMixin {

    @Inject(method = "execute", at = @At("RETURN"), remap = false)
    public void execute(ITurtleAccess turtle, CallbackInfoReturnable<TurtleCommandResult> cir) {
        Direction direc = turtle.getDirection();
        BlockPos newPos = turtle.getPosition().relative(direc);
        TurtleBlockEntity tt = ((TurtleBrain) turtle).getOwner();
        ComputerPosMapper.put(tt.getComputerID(), newPos);
    }
}
