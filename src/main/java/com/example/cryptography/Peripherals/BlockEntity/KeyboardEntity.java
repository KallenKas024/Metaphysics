package com.example.cryptography.Peripherals.BlockEntity;

import com.example.cryptography.Network.PacketManager;
import com.example.cryptography.Network.SP.SP_Keyboard;
import com.example.cryptography.Peripherals.Peripheral.KeyboardPeripheral;
import com.example.cryptography.Registrys.RegistryHelper;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class KeyboardEntity extends BlockEntity {

    public LazyOptional<IPeripheral> peripheral;
    public final Set<IComputerAccess> computers = Collections.newSetFromMap(new ConcurrentHashMap());
    private Player player;
    private boolean isListening = false;

    public KeyboardEntity(BlockPos pPos, BlockState pBlockState) {
        super(RegistryHelper.KEYBOARD_BLOCK_ENTITY_TYPE.get(), pPos, pBlockState);
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

    public void onUse(Player player) {
        if (player.isAlive()) {
            if (!isListening) {
                isListening = true;
                this.player = player;
                computers.forEach(c -> c.queueEvent("playerAttach", player.getName().getString()));
                if (player.level().isClientSide()) {
                    //PacketManager.sendToClient(new SP_Keyboard(this, true), PacketDistributor.ALL.noArg());
                } else {
                    //PacketManager.sendToPlayer(new SP_Keyboard(this, true), player.getServer().getPlayerList().getPlayer(player.getUUID()));
                }
            } else {
                if (this.player.getStringUUID().equalsIgnoreCase(player.getStringUUID())) {
                    isListening = false;
                    computers.forEach(c -> c.queueEvent("playerDetach", player.getName().getString()));
                    if (player.level().isClientSide()) {
                        //PacketManager.sendToClient(new SP_Keyboard(this, false), PacketDistributor.ALL.noArg());
                    } else {
                        //PacketManager.sendToPlayer(new SP_Keyboard(this, false), player.getServer().getPlayerList().getPlayer(player.getUUID()));
                    }
                } else if (!Objects.requireNonNull(level.getServer()).getPlayerList().getPlayers().contains(this.player)) {
                    //如果操作中的玩家下线了就重新开启
                    isListening = true;
                    this.player = player;
                    computers.forEach(c -> c.queueEvent("playerChange", player.getName().getString()));
                    computers.forEach(c -> c.queueEvent("playerAttach", player.getName().getString()));
                    if (player.level().isClientSide()) {
                        //PacketManager.sendToClient(new SP_Keyboard(this, true), PacketDistributor.ALL.noArg());
                    } else {
                        //PacketManager.sendToPlayer(new SP_Keyboard(this, true), player.getServer().getPlayerList().getPlayer(player.getUUID()));
                    }
                }
            }
        }
    }
}
