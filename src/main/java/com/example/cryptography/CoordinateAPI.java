package com.example.cryptography;

import com.google.gson.Gson;
import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.core.computer.mainthread.MainThread;
import dan200.computercraft.core.computer.mainthread.MainThreadScheduler;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.primitives.AABBdc;
import org.joml.primitives.AABBic;
import org.valkyrienskies.core.api.ships.QueryableShipData;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import static com.example.cryptography.Cryptography.ComputerPosMapper;

public class CoordinateAPI implements ILuaAPI {

    private BlockPos pos;
    private final int id;
    private final Level level;
    private final ServerComputer serverComputerSelf;
    public Map<Map<String, Integer>, Map<String,Object>> tmpMap;
    public CoordinateAPI(BlockPos pos, int id, Level level, ServerComputer serverComputerSelf) {
        this.pos = pos;
        this.id = id;
        this.level = level;
        this.serverComputerSelf = serverComputerSelf;
    }

    @Override
    public String[] getNames() {
        return new String[] {"coord", "coordinate"};
    }

    @Override
    public void update()
    {
        this.pos = ComputerPosMapper.get(id);
    }
    @LuaFunction
    public final Map<String, Double> getCoord() {
        return getCoordinate();
    }

    @LuaFunction
    public final String getBlock(int x, int y, int z) {
        BlockPos bpos = new BlockPos(x, y, z);
        BlockState bs = level.getBlockState(bpos);
        return bs.getBlock().getDescriptionId().replace("block.", "").replace(".", ":");
    }

    @LuaFunction
    public final boolean getBlockIsReplaceable(int x, int y, int z) {
        BlockPos bpos = new BlockPos(x, y, z);
        BlockState bs = level.getBlockState(bpos);
        return bs.canBeReplaced();
    }

    @LuaFunction
    public final boolean getBlockIsSolid(int x, int y, int z) {
        BlockPos bpos = new BlockPos(x, y, z);
        BlockState bs = level.getBlockState(bpos);
        return bs.isSolid();
    }

    @LuaFunction
    public final boolean isBlockLoaded(int x, int y, int z) {
        BlockPos bpos = new BlockPos(x, y, z);
        return level.isLoaded(bpos);
    }

    @LuaFunction
    public final String getSelfDimensionType() {
        return level.dimension().location().getPath();
    }

    //@LuaFunction
    //public final String getBiome(int x, int y, int z) {
    //    BlockPos bpos = new BlockPos(x, y, z);
    //    return level.getBiome(bpos).kind().name();
    //}

    @LuaFunction
    public final boolean isBlockAir(int x, int y, int z) {
        BlockPos bpos = new BlockPos(x, y, z);
        return level.getBlockState(bpos).isAir();
    }

    @LuaFunction
    public final @NotNull Map<String, Object> getEntities(int scope) {
        //if (scope > Cryptography.MAX_SCOPE) {
        //    return new HashMap<>();
        //}
        boolean isAll;
        if (scope == -1) {
            isAll = true;
        } else {
            isAll = false;
        }
        Map<String, Double> cmap = getCoordinate();
        BlockPos startBlockPos = new BlockPos((int) (Math.floor(cmap.get("x"))+scope), (int) (Math.floor(cmap.get("y"))+scope), (int) (Math.floor(cmap.get("z"))+scope));
        BlockPos endBlockPos = new BlockPos((int) (cmap.get("x")-scope), (int) (Math.floor(cmap.get("y"))-scope), (int) (Math.floor(cmap.get("z"))-scope));
        LevelEntityGetter<Entity> entities1 = level.getServer().getLevel(level.dimension()).getEntities();
        List<Entity> entities = new ArrayList<>(2048);
        try {
            entities1.getAll().forEach(e -> {
                if (isAll) {
                    entities.add(e);
                } else {
                    BlockPos p = e.getOnPos();
                    AABB aabb = new AABB(startBlockPos, endBlockPos);
                    if (p.getX() <= aabb.maxX && p.getX() >= aabb.minX && p.getY() <= aabb.maxY && p.getY() >= aabb.minY && p.getZ() <= aabb.maxZ && p.getZ() >= aabb.minZ) {
                        entities.add(e);
                    }
                }
            });
        } catch (IndexOutOfBoundsException ex) {
        }
        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i < entities.size(); i++) {
            Map<Object, Object> map = new HashMap<>();
            Entity entity = entities.get(i);
            map.put("x", entity.getX());
            map.put("y", entity.getY());
            map.put("z", entity.getZ());
            Component preStr = entity.getDisplayName();
            String name;
            name = preStr.getString();
            String uuid = entity.getUUID().toString();
            map.put("uuid", uuid);
            map.put("name", name);
            if (entity instanceof Player) {
                map.put("isPlayer", true);
                Map<String, Object> playerMapper = new HashMap<>();
                Player player = ((Player) entity);
                boolean isCreative = player.isCreative();
                playerMapper.put("isCreative", isCreative);
                boolean isSpectator = player.isSpectator();
                playerMapper.put("isSpectator", isSpectator);
                map.put("playerInfo", playerMapper);
            } else {
                map.put("isPlayer", false);
                map.put("playerInfo", new HashMap<>());
            }
            String type = entity.getType().getDescriptionId().replace("entity.", "").replace(".", ":");
            map.put("type", type);
            String direc = entity.getMotionDirection().getSerializedName();
            map.put("direction", direc);
            Vec3 v3 = entity.getDeltaMovement();
            Map<String, Double> vector = new HashMap<>();
            vector.put("x", v3.x());
            vector.put("y", v3.y());
            vector.put("z", v3.z());
            map.put("vector", vector);
            double yaw = Math.atan2(entity.getLookAngle().x, entity.getLookAngle().z) * 180 / Math.PI;
            if (yaw > 89.9 || yaw < -89.9) {
                yaw = Math.atan2(entity.getLookAngle().y, entity.getLookAngle().z) * 180 / Math.PI;
            }
            map.put("yaw", yaw);
            map.put("pitch", Math.asin(entity.getLookAngle().y) * 180 / Math.PI);
            map.put("raw_euler_x", entity.getLookAngle().x);
            map.put("raw_euler_y", entity.getLookAngle().y);
            map.put("raw_euler_z", entity.getLookAngle().z);
            if (entity instanceof LivingEntity) {
                map.put("isLivingEntity", true);
                LivingEntity mob = (LivingEntity) entity;
                map.put("maxHealth", mob.getMaxHealth());
                map.put("health", mob.getHealth());
                map.put("armor",mob.getArmorValue());

            } else {
                map.put("isLivingEntity", false);
            }
            result.put(String.valueOf((i + 1)), map);
        }
        return result;
    }

    @LuaFunction
    public final int getBlockExplosionResistance(int x, int y, int z) {
        BlockPos bpos = new BlockPos(x, y, z);
        BlockState bs = level.getBlockState(bpos);
        return (int) Math.floor(bs.getBlock().getExplosionResistance());
    }

    @LuaFunction
    public final Map<String, Object> getShips(int scope) {
        if (scope > Cryptography.MAX_SCOPE) {
            return new HashMap<>();
        }
        Map<String, Double> cmap = getCoordinate();
        BlockPos startBlockPos = new BlockPos((int) (Math.floor(cmap.get("x")) + scope), (int) (Math.floor(cmap.get("y")) + scope), (int) (Math.floor(cmap.get("z")) + scope));
        BlockPos endBlockPos = new BlockPos((int) (cmap.get("x") - scope), (int) (Math.floor(cmap.get("y")) - scope), (int) (Math.floor(cmap.get("z")) - scope));
        QueryableShipData<Ship> qsd = VSGameUtilsKt.getAllShips(level);
        Map<String, Object> mapper = new HashMap<>();
        qsd.iterator().forEachRemaining(e -> {
            AABBdc p = e.getWorldAABB();
            double[] c = getAABBdcCenter(p);
            AABB aabb = new AABB(startBlockPos, endBlockPos);
            if (aabb.contains(c[0], c[1], c[2])) {
                Map<String, Object> attr = new HashMap<>();
                attr.put("id", e.getId());
                attr.put("slug", e.getSlug());
                attr.put("x", c[0]);
                attr.put("y", c[1]);
                attr.put("z", c[2]);
                AABBdc box = e.getWorldAABB();
                attr.put("max_x", box.maxX());
                attr.put("max_y", box.maxY());
                attr.put("max_z", box.maxZ());
                attr.put("min_x", box.minX());
                attr.put("min_y", box.minY());
                attr.put("min_z", box.minZ());
                mapper.put(String.valueOf(e.getId()), attr);
            }
        });
        return mapper;
    }

    @LuaFunction
    public final boolean isOnShipEntity() {
        return isOnShip();
    }

    @LuaFunction
    public final Map<String, Integer> getAbsoluteCoordinates() {
        Map<String, Integer> map = new HashMap<>();
        map.put("x", pos.getX());
        map.put("y", pos.getY());
        map.put("z", pos.getZ());
        return map;
    }

    private boolean isOnShip() {
        return VSGameUtilsKt.getShipManagingPos(level, pos) != null;
    }

    private Ship getShip() {
        return VSGameUtilsKt.getShipManagingPos(level, pos);
    }

    private double[] getAABBdcCenter(AABBdc aabb) {
        double width = aabb.maxX() - aabb.minX();
        double len = aabb.maxZ() - aabb.minZ();
        double hight = aabb.maxY() - aabb.minY();
        double centerX = aabb.minX() + width / 2;
        double centerY = aabb.minY() + hight / 2;
        double centerZ = aabb.minZ() + len / 2;
        return new double[]{centerX, centerY, centerZ};
    }
    private BlockPos getAABBicCenter(AABBic aabb) {
        int width = aabb.maxX() - aabb.minX();
        int len = aabb.maxZ() - aabb.minZ();
        int hight = aabb.maxY() - aabb.minY();
        int centerX = aabb.minX() + width / 2;
        int centerY = aabb.minY() + hight / 2;
        int centerZ = aabb.minZ() + len / 2;
        return new BlockPos(centerX, centerY, centerZ);
    }

    private Map<String,Double> getCoordinate() {
        Map<String, Double> map = new HashMap<>();
        if (isOnShip()) {
            Ship ship = getShip();
            Vector3d v3d = VSGameUtilsKt.toWorldCoordinates(ship, pos);
            map.put("x", v3d.x);
            map.put("y", v3d.y);
            map.put("z", v3d.z);
        } else {
            map.put("x", (double) pos.getX());
            map.put("y", (double) pos.getY());
            map.put("z", (double) pos.getZ());
        }
        return map;
    }

    @LuaFunction
    public final String getSlug() {
        if(isOnShip()) {
            Ship ship = getShip();
            return ship.getSlug();
        }
        return "NoSlug";
    }

    @LuaFunction
    public final void setSlug(String name) {
        if(isOnShip()) {
            ServerShip ship = VSGameUtilsKt.getShipManagingPos(VSGameUtilsKt.getLevelFromDimensionId(level.getServer(), VSGameUtilsKt.getDimensionId(level)), pos);
            if (ship != null) {
                ship.setSlug(name);
            }
        }
    }
    @LuaFunction
    public final Map<Integer, Map<String, Object>> getBlockMatrix3D(int start_x, int start_y, int start_z, int end_x, int end_y, int end_z) {
        Map<Integer, Map<String, Object>> map = new HashMap<>();
        int cost = 0;
        for(int i = start_x; i < end_x; i++) {
            cost += 1;
            for(int j = start_y; j < end_y; j++) {
                cost += 1;
                for(int k = start_z; k < end_z; k++) {
                    cost += 1;
                    BlockPos pos = new BlockPos(i, j, k);
                    BlockState state = level.getBlockState(pos);
                    Map<String, Object> re = new HashMap<>();
                    Map<String, Integer> imap = new HashMap<>();
                    imap.put("x", pos.getX());
                    imap.put("y", pos.getY());
                    imap.put("z", pos.getZ());
                    re.put("coordinate", imap);
                    re.put("namespace", state.getBlock().getDescriptionId());
                    map.put(cost, re);
                }
            }
        }
        return map;
    }
}

