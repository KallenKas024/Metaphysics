package com.example.cryptography.Apis;

import com.example.cryptography.Network.PacketManager;
import com.example.cryptography.Network.SP.SP_Keyboard;
import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.shared.computer.blocks.ComputerBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

import java.util.Objects;
import java.util.UUID;

public class Keyboard implements ILuaAPI {
    private Level level;
    private Player player;
    private Computer computer;
    private ComputerBlockEntity blockEntity;
    @Override
    public String[] getNames() {
        return new String[]{
                "key"
        };
    }
    public Keyboard(Level level, Computer computer, ComputerBlockEntity cbe) {
        this.level = level;
        this.computer = computer;
        this.blockEntity = cbe;
    }
    @LuaFunction
    public final void start(String uuid) {
        Player player = this.level.getPlayerByUUID(UUID.fromString(uuid));
        if (player.isAlive()) {
            this.player = player;
            this.computer.queueEvent("playerAttach", new String[]{player.getName().getString()});
            if (player.level().isClientSide()) {
                PacketManager.sendToClient(new SP_Keyboard(this.blockEntity, true), PacketDistributor.ALL.noArg());
            } else {
                PacketManager.sendToPlayer(new SP_Keyboard(this.blockEntity, true), player.getServer().getPlayerList().getPlayer(player.getUUID()));
            }
        }
    }
    @LuaFunction
    public final void end() {
        if (player == null) {
            return;
        }
        computer.queueEvent("playerDetach", new String[]{player.getName().getString()});
        if (player.level().isClientSide()) {
            PacketManager.sendToClient(new SP_Keyboard(this.blockEntity, false), PacketDistributor.ALL.noArg());
        } else {
            PacketManager.sendToPlayer(new SP_Keyboard(this.blockEntity, false), player.getServer().getPlayerList().getPlayer(player.getUUID()));
        }
    }
}
