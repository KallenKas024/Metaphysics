package com.example.cryptography;

import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.shared.pocket.core.PocketHolder;

public class CoordinatePocketComputerAPI implements ILuaAPI {

    private final PocketHolder holder;
    private final boolean isOnPhone;

    @Override
    public String[] getNames() {
        return new String[] {"coordPCA", "coordinatePCA"};
    }

    public CoordinatePocketComputerAPI(PocketHolder holder, boolean isOnPhone) {
        this.holder = holder;
        this.isOnPhone = isOnPhone;
    }

    @LuaFunction
    public final double getThisPocketComputerOwnerPosX() {
        if (isOnPhone) {
            return holder.blockPos().getX();
        } else return 0.0;
    }

    @LuaFunction
    public final double getThisPocketComputerOwnerPosY() {
        if (isOnPhone) {
            return holder.blockPos().getY();
        } else return 0.0;
    }

    @LuaFunction
    public final double getThisPocketComputerOwnerPosZ() {
        if (isOnPhone) {
            return holder.blockPos().getZ();
        } else return 0.0;
    }

    @LuaFunction
    public final double getThisPocketComputerOwnerXRot() {
        if (isOnPhone && holder instanceof PocketHolder.EntityHolder) {
            return ((PocketHolder.EntityHolder) holder).entity().getXRot();
        } else return 0.0;
    }

    @LuaFunction
    public final double getThisPocketComputerOwnerYRot() {
        if (isOnPhone && holder instanceof PocketHolder.EntityHolder) {
            return ((PocketHolder.EntityHolder) holder).entity().getYRot();
        } else return 0.0;
    }

    @LuaFunction
    public final double getThisPocketComputerOwnerLookAngleX() {
        if (isOnPhone && holder instanceof PocketHolder.EntityHolder) {
            return ((PocketHolder.EntityHolder) holder).entity().getLookAngle().x();
        } else return 0.0;
    }

    @LuaFunction
    public final double getThisPocketComputerOwnerLookAngleY() {
        if (isOnPhone && holder instanceof PocketHolder.EntityHolder) {
            return ((PocketHolder.EntityHolder) holder).entity().getLookAngle().y();
        } else return 0.0;
    }

    @LuaFunction
    public final double getThisPocketComputerOwnerLookAngleZ() {
        if (isOnPhone && holder instanceof PocketHolder.EntityHolder) {
            return ((PocketHolder.EntityHolder) holder).entity().getLookAngle().z();
        } else return 0.0;
    }

    @LuaFunction
    public final double getThisPocketComputerOwnerForwardPosX() {
        if (isOnPhone && holder instanceof PocketHolder.EntityHolder) {
            return ((PocketHolder.EntityHolder) holder).entity().getForward().x();
        } else return 0.0;
    }

    @LuaFunction
    public final double getThisPocketComputerOwnerForwardPosY() {
        if (isOnPhone && holder instanceof PocketHolder.EntityHolder) {
            return ((PocketHolder.EntityHolder) holder).entity().getForward().y();
        } else return 0.0;
    }

    @LuaFunction
    public final double getThisPocketComputerOwnerForwardPosZ() {
        if (isOnPhone && holder instanceof PocketHolder.EntityHolder) {
            return ((PocketHolder.EntityHolder) holder).entity().getForward().z();
        } else return 0.0;
    }
}
