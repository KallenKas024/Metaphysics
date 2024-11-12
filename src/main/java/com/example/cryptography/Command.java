package com.example.cryptography;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Command {
    @SubscribeEvent
    public static void onServerStarting(ServerStartedEvent event) {
        event.getServer().getCommands().getDispatcher().register(
                Commands.literal("ban-series") // 指令名称
                        .then(Commands.argument("user", com.mojang.brigadier.arguments.StringArgumentType.string())
                                .then(Commands.argument("reason", StringArgumentType.string()))// 指令参数
                                .executes(context -> executeCommand(context.getSource(), context.getArgument("user", String.class), context.getArgument("reason", String.class)))
                        )
        );
    }
    private static int executeCommand(CommandSourceStack source, String user, String reason) {
        source.sendSuccess(() -> {
            ServerPlayer sp = source.getPlayer();
            if (!source.hasPermission(3)) {
                return Component.nullToEmpty("You don't have permission to execute this command");
            }
            
            return Component.nullToEmpty("baned " + user + " by reason -> " + reason);
        }, true);
        return 1;
    }
}
