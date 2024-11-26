package com.example.cryptography.Network.CP;

import com.example.cryptography.Peripherals.BlockEntity.KeyboardEntity;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.shared.computer.blocks.ComputerBlock;
import dan200.computercraft.shared.computer.blocks.ComputerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.example.cryptography.Utils.ByteUtils.decodeString;
import static com.example.cryptography.Utils.ByteUtils.encodeString;

public class CP_Keyboard {
        public BlockPos te;
        public String name;
        public int param;

        public CP_Keyboard(){

        }

        public CP_Keyboard(ComputerBlockEntity te, String event, int param){
            this.te = te.getBlockPos();
            this.name = event;
            this.param = param;
        }

        public static CP_Keyboard decode(FriendlyByteBuf buf) {
            //System.out.println("Received_DEC");
            CP_Keyboard data = new CP_Keyboard();
            data.te = buf.readBlockPos();
            data.name = decodeString(buf);
            data.param = buf.readInt();
            return data;
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeBlockPos(te);
            encodeString(buf, name);
            buf.writeInt(param);
        }

        public static void handler(CP_Keyboard msg, Supplier<NetworkEvent.Context> context){
            NetworkEvent.Context ctx = context.get();
            ctx.setPacketHandled(true);
            ctx.enqueueWork(() -> {
                BlockEntity be = context.get().getSender().level().getExistingBlockEntity(msg.te);
                if (be instanceof ComputerBlockEntity te) {
                    String key = GLFW.glfwGetKeyName(msg.param, GLFW.glfwGetKeyScancode(msg.param));
                    Map<String, Object> data = new HashMap<>();
                    data.put("key", key);
                    data.put("keyCode", msg.param);
                    te.getServerComputer().queueEvent(msg.name, new Map[]{data});
                    //System.out.println("queueEvent "+msg.name+" to computer " + e.getID() + " and got param: " + key);
                }
            });
        }
}
