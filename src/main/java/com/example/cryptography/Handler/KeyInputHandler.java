package com.example.cryptography.Handler;
import com.example.cryptography.Network.CP.CP_Keyboard;
import com.example.cryptography.Network.PacketManager;
import com.example.cryptography.Peripherals.BlockEntity.KeyboardEntity;
import dan200.computercraft.shared.computer.blocks.ComputerBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.lwjgl.glfw.GLFW;

import static com.example.cryptography.Cryptography.modid;

@Mod.EventBusSubscriber(modid = modid, bus = Bus.FORGE, value = Dist.CLIENT)
public class KeyInputHandler {
    public static boolean isUpload = false;
    public static ComputerBlockEntity ke = null;
    @SubscribeEvent
    public static void onKeyUpdate(InputEvent.Key event) {
        if (event.getAction() == GLFW.GLFW_PRESS) {
            String key = GLFW.glfwGetKeyName(event.getKey(), event.getScanCode());
            System.out.println("press "+key+" attached.");
            if (isUpload) {
                System.out.println("Uploaded");
                if (ke != null) {
                    PacketManager.sendToServer(new CP_Keyboard(ke, "onKeyPress", event.getKey()));
                }
            }
        } else {
            String key = GLFW.glfwGetKeyName(event.getKey(), event.getScanCode());
            System.out.println("press "+key+" attached.");
            if (isUpload) {
                System.out.println("Uploaded");
                if (ke != null) {
                    PacketManager.sendToServer(new CP_Keyboard(ke, "onKeyRelease", event.getKey()));
                }
            }
        }
    }
}
