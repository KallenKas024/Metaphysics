package com.example.cryptography.Network;

import com.example.cryptography.Network.CP.CP_Keyboard;
import com.example.cryptography.Network.SP.SP_Keyboard;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

import static com.example.cryptography.Cryptography.modid;

public class PacketManager {
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(modid, modid+"_channel")).networkProtocolVersion(() -> {
                return PacketManager.VERSION;
            })
            .clientAcceptedVersions(PacketManager.VERSION::equals).serverAcceptedVersions(PacketManager.VERSION::equals).simpleChannel();

    static final String VERSION = "1.3";

    public PacketManager() {
    }

    public static <MSG> void sendToServer(MSG msg){
        CHANNEL.sendToServer(msg);
    }


    public static <MSG> void sendToClient(MSG message, PacketDistributor.PacketTarget packetTarget) {
        CHANNEL.send(packetTarget, message);
    }

    public static <MSG> void sendToAll(MSG message) {
        sendToClient(message, PacketDistributor.ALL.noArg());
    }

    public static <MSG> void sendToAllPlayerTrackingThisEntity(MSG message, Entity entity) {
        sendToClient(message, PacketDistributor.TRACKING_ENTITY.with(() -> entity));
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        sendToClient(message, PacketDistributor.PLAYER.with(() -> player));
    }

    public static <MSG> void sendToAllPlayerTrackingThisEntityWithSelf(MSG message, ServerPlayer entity) {
        sendToPlayer(message, entity);
        sendToClient(message, PacketDistributor.TRACKING_ENTITY.with(() -> entity));
    }

    public static <MSG> void sendToAllPlayerTrackingThisBlock(MSG message, BlockEntity te) {
        sendToClient(message, PacketDistributor.TRACKING_CHUNK.with(() -> te.getLevel().getChunkAt(te.getBlockPos())));
    }

    private static int index = 0;

    public static void Init(){
        CHANNEL.registerMessage(index++, SP_Keyboard.class,
                SP_Keyboard::encode, SP_Keyboard::decode,
                SP_Keyboard::handler, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(index++, CP_Keyboard.class,
                CP_Keyboard::encode, CP_Keyboard::decode,
                CP_Keyboard::handler, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }
}
