package com.example.cryptography.Peripherals.BlockEntity;

import com.example.cryptography.Peripherals.Peripheral.KeyboardPeripheral;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class KeyboardEntity extends BlockEntity {

    private LazyOptional<IPeripheral> peripheral;
    public final Set<IComputerAccess> computers = Collections.newSetFromMap(new ConcurrentHashMap());

    public KeyboardEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public IPeripheral peripheral() {
        return new KeyboardPeripheral(this);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == Capabilities.CAPABILITY_PERIPHERAL) {
            if (peripheral == null || peripheral.isPresent()) {
                peripheral = LazyOptional.of(this::peripheral);
            }
            return peripheral.cast();
        }
        return super.getCapability(cap, side);
    }

    public void addComputer(IComputerAccess computer) {
        this.computers.add(computer);
    }

    public void removeComputer(IComputerAccess computer) {
        this.computers.remove(computer);
    }


}
