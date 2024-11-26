package com.example.cryptography.Network.SP;


import com.example.cryptography.Handler.KeyInputHandler;
import com.example.cryptography.Network.PacketManager;
import com.example.cryptography.Peripherals.BlockEntity.KeyboardEntity;
import dan200.computercraft.shared.computer.blocks.ComputerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.example.cryptography.Utils.ByteUtils.compress;

@SuppressWarnings("rawtypes")
public class SP_Keyboard {
    public final BlockPos te;
    public final boolean isUpload;

    public SP_Keyboard(ComputerBlockEntity te, boolean isUpload){
        this.te = te.getBlockPos();
        this.isUpload = isUpload;
    }

    public SP_Keyboard(FriendlyByteBuf buf){
        this.te = buf.readBlockPos();
        this.isUpload = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(te);
        buf.writeBoolean(isUpload);
    }

    public static SP_Keyboard decode(FriendlyByteBuf buf){
        return new SP_Keyboard(buf);
    }

    public static void handler(SP_Keyboard msg, Supplier<NetworkEvent.Context> context){
        NetworkEvent.Context ctx = context.get();
        ctx.setPacketHandled(true);
        ctx.enqueueWork(() -> {
            BlockEntity be = Minecraft.getInstance().level.getBlockEntity(msg.te);
            if(be instanceof ComputerBlockEntity te) {
                KeyInputHandler.isUpload = msg.isUpload;
                System.out.println("KeyInputHandler.isUpload: " + msg.isUpload);
                if (msg.isUpload) {
                    KeyInputHandler.ke = te;
                } else {
                    KeyInputHandler.ke = null;
                }
            }
        });
    }
}