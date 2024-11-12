package com.example.cryptography;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Mod("cryptography")
public class Cryptography {

    public static Map<Integer, BlockPos> ComputerPosMapper = new HashMap<>();
    public static Map<String, Boolean> ComputerStatusMapper = new HashMap<>();
    public static final boolean isUsePhoneAPI = false;
    public static final int MAX_SCOPE = 2500;
    public Cryptography() {
        MinecraftForge.EVENT_BUS.register(this);
    }
}
