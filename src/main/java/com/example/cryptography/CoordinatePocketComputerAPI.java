package com.example.cryptography;

import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.world.entity.Entity;

public class CoordinatePocketComputerAPI implements ILuaAPI {

    private final Entity entity;
    private final boolean isOnPhone;

    @Override
    public String[] getNames() {
        return new String[] {"coordPCA", "coordinatePCA"};
    }

    public CoordinatePocketComputerAPI(Entity entity, boolean isOnPhone) {
        this.entity = entity;
        this.isOnPhone = isOnPhone;
    }

    @LuaFunction
    public final double getThisPocketComputerOwnerPosX() {
        if (isOnPhone) {
            return entity.getBlockX();
        } else return 0.0;
    }

    @LuaFunction
    public final double getThisPocketComputerOwnerPosY() {
        if (isOnPhone) {
            return entity.getBlockY();
        } else return 0.0;
    }

    @LuaFunction
    public final double getThisPocketComputerOwnerPosZ() {
        if (isOnPhone) {
            return entity.getBlockZ();
        } else return 0.0;
    }

    @LuaFunction
    public final double getThisPocketComputerOwnerXRot() {
        if (isOnPhone) {
            return entity.getXRot();
        } else return 0.0;
    }

    @LuaFunction
    public final double getThisPocketComputerOwnerYRot() {
        if (isOnPhone) {
            return entity.getYRot();
        } else return 0.0;
    }

    @LuaFunction
    public final double getThisPocketComputerOwnerLookAngleX() {
        if (isOnPhone) {
            return entity.getLookAngle().x();
        } else return 0.0;
    }

    @LuaFunction
    public final double getThisPocketComputerOwnerLookAngleY() {
        if (isOnPhone) {
            return entity.getLookAngle().y();
        } else return 0.0;
    }

    @LuaFunction
    public final double getThisPocketComputerOwnerLookAngleZ() {
        if (isOnPhone) {
            return entity.getLookAngle().z();
        } else return 0.0;
    }

    @LuaFunction
    public final double getThisPocketComputerOwnerForwardPosX() {
        if (isOnPhone) {
            return entity.getForward().x;
        } else return 0.0;
    }

    @LuaFunction
    public final double getThisPocketComputerOwnerForwardPosY() {
        if (isOnPhone) {
            return entity.getForward().y;
        } else return 0.0;
    }

    @LuaFunction
    public final double getThisPocketComputerOwnerForwardPosZ() {
        if (isOnPhone) {
            return entity.getForward().z;
        } else return 0.0;
    }
}
