package com.example.cryptography.Peripherals.Peripheral;

import com.example.cryptography.Peripherals.BlockEntity.KeyboardEntity;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class KeyboardPeripheral implements IPeripheral{

    private KeyboardEntity keyboardEntity;

    public KeyboardPeripheral(KeyboardEntity keyboardEntity) {
        this.keyboardEntity = keyboardEntity;
    }

    @Override
    public String getType() {
        return "keyboard";
    }

    @Override
    public Set<String> getAdditionalTypes() {
        return IPeripheral.super.getAdditionalTypes();
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if (iPeripheral != null && iPeripheral.getType().equalsIgnoreCase(getType())) {
            return true;
        } else return false;
    }

    @Override
    public void attach(IComputerAccess computer) {
        // 当计算机连接到外设时调用
        this.keyboardEntity.addComputer(computer);
    }

    @Override
    public void detach(IComputerAccess computer) {
        // 当计算机断开连接时调用
        this.keyboardEntity.removeComputer(computer);
    }
}
