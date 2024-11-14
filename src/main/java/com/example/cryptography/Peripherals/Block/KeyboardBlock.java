package com.example.cryptography.Peripherals.Block;

import com.example.cryptography.Peripherals.BlockEntity.KeyboardEntity;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class KeyboardBlock extends Block implements EntityBlock{
    private boolean isListening = false;
    private Thread thread;
    private KeyboardEntity entity;
    private Player player;
    public KeyboardBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (player.isAlive()) {
            if (!isListening) {
                isListening = true;
                this.player = player;
                Thread thread = new Thread(() -> {
                    while (isListening && !Thread.currentThread().isInterrupted()) {
                        String key = CUSTOM_KEY.getKey().getName();
                        entity.computers.forEach(
                                i -> {
                                    i.queueEvent("playerClick", (Object) new String[]{key});
                                }
                        );
                    }
                });
                this.thread = thread;
                thread.start();
            } else {
                if (this.player == player) {
                    isListening = false;
                    thread.interrupt();
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        this.entity = new KeyboardEntity(pPos, pState);
        return this.entity;
    }
}
