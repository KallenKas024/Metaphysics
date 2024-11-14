package com.example.cryptography;

import com.google.gson.Gson;
import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.client.render.ItemMapLikeRenderer;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.computer.mainthread.MainThread;
import dan200.computercraft.core.computer.mainthread.MainThreadScheduler;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ItemStackMap;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.primitives.AABBdc;
import org.joml.primitives.AABBic;
import org.valkyrienskies.core.api.ships.QueryableShipData;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.impl.shadow.B;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import static com.example.cryptography.Cryptography.ComputerPosMapper;

public class CoordinateAPI implements ILuaAPI {
    private BlockPos pos;
    private final int id;
    private final Level level;
    public Map<Map<String, Integer>, Map<String, Object>> tmpMap;
    private Computer computer;
    private Thread t = null;
    public CoordinateAPI(BlockPos pos, int id, Level level, Computer computer) {
        this.pos = pos;
        this.id = id;
        this.level = level;
        this.computer = computer;
    }

    @Override
    public String[] getNames() {
        return new String[]{"coord", "coordinate"};
    }

    @Override
    public void update() {
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
    public final boolean canSeeSky(int x, int y, int z){
        return level.canSeeSky(new BlockPos(x, y, z));
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

    public final @NotNull Map<String, Object> getEntitiesRaw(int scope, boolean flag){
        boolean isAll;
        if (scope == -1) {
            isAll = true;
        } else {
            isAll = false;
        }

        Map<String, Double> cmap = getCoordinate();
        BlockPos startBlockPos = new BlockPos((int) (Math.floor(cmap.get("x")) + scope), (int) (Math.floor(cmap.get("y")) + scope), (int) (Math.floor(cmap.get("z")) + scope));
        BlockPos endBlockPos = new BlockPos((int) (cmap.get("x") - scope), (int) (Math.floor(cmap.get("y")) - scope), (int) (Math.floor(cmap.get("z")) - scope));
        LevelEntityGetter<Entity> entities1 = level.getServer().getLevel(level.dimension()).getEntities();
        List<Entity> entities = new ArrayList<>();
        AABB aabb = new AABB(startBlockPos, endBlockPos);
        entities1.getAll().iterator().forEachRemaining(e -> {
            try {
                BlockPos p = new BlockPos((int) Math.floor(e.getX()), (int) Math.floor(e.getY() + 0.5), (int) Math.floor(e.getZ()));
                boolean filterFlag = true;
                if (flag){
                    filterFlag = level.canSeeSky(p);
                }
                if (filterFlag){
                    if (isAll) {
                        entities.add(e);
                    } else {
                        if (p.getX() <= aabb.maxX && p.getX() >= aabb.minX && p.getY() <= aabb.maxY && p.getY() >= aabb.minY && p.getZ() <= aabb.maxZ && p.getZ() >= aabb.minZ) {
                            entities.add(e);
                        }
                    }
                }
             } catch (RuntimeException ex) {
             }
        });

        Map<String, Object> result = new HashMap<>();
        entities.forEach(e -> {
            Map<Object, Object> map = new HashMap<>();
            map.put("x", e.getX());
            map.put("y", e.getY());
            map.put("z", e.getZ());
            Component preStr = e.getDisplayName();
            String name;
            name = preStr.getString();
            String uuid = e.getUUID().toString();
            map.put("uuid", uuid);
            map.put("name", name);
            if (e instanceof Player) {
                map.put("isPlayer", true);
                Map<String, Object> playerMapper = new HashMap<>();
                Player player = ((Player) e);
                boolean isCreative = player.isCreative();
                playerMapper.put("isCreative", isCreative);
                boolean isSpectator = player.isSpectator();
                playerMapper.put("isSpectator", isSpectator);
                map.put("playerInfo", playerMapper);
            } else {
                map.put("isPlayer", false);
                map.put("playerInfo", new HashMap<>());
            }
            String type = e.getType().getDescriptionId().replace("entity.", "").replace(".", ":");
            map.put("type", type);
            String direc = e.getMotionDirection().getSerializedName();
            map.put("direction", direc);
            Vec3 v3 = e.getDeltaMovement();
            Map<String, Double> vector = new HashMap<>();
            vector.put("x", v3.x());
            vector.put("y", v3.y());
            vector.put("z", v3.z());
            map.put("vector", vector);
            double yaw = Math.atan2(e.getLookAngle().x, e.getLookAngle().z) * 180 / Math.PI;
            if (yaw > 89.9 || yaw < -89.9) {
                yaw = Math.atan2(e.getLookAngle().y, e.getLookAngle().z) * 180 / Math.PI;
            }
            map.put("yaw", yaw);
            map.put("pitch", Math.asin(e.getLookAngle().y) * 180 / Math.PI);
            map.put("raw_euler_x", e.getLookAngle().x);
            map.put("raw_euler_y", e.getLookAngle().y);
            map.put("raw_euler_z", e.getLookAngle().z);
            if (e instanceof LivingEntity) {
                map.put("isLivingEntity", true);
                LivingEntity mob = (LivingEntity) e;
                map.put("maxHealth", mob.getMaxHealth());
                map.put("health", mob.getHealth());
                map.put("armor", mob.getArmorValue());
            } else {
                map.put("isLivingEntity", false);
            }
            result.put(uuid, map);
        });
        return result;
    }

    @LuaFunction
    public final @NotNull Map<String, Object> getEntities(int scope) {
        return getEntitiesRaw(scope, true);
    }

    @LuaFunction
    public final @NotNull Map<String, Object> getEntitiesAll(int scope) {
        return getEntitiesRaw(scope, false);
    }

    @LuaFunction
    public final int getBlockExplosionResistance(int x, int y, int z) {
        BlockPos bpos = new BlockPos(x, y, z);
        BlockState bs = level.getBlockState(bpos);
        return (int) Math.floor(bs.getBlock().getExplosionResistance());
    }

    public final Map<String, Object> getShipsRaw(int scope, boolean flag){
        if (scope > Cryptography.MAX_SCOPE) {
            return new HashMap<>();
        }
        Map<String, Double> cmap = getCoordinate();
        BlockPos startBlockPos = new BlockPos((int) (Math.floor(cmap.get("x")) + scope), (int) (Math.floor(cmap.get("y")) + scope), (int) (Math.floor(cmap.get("z")) + scope));
        BlockPos endBlockPos = new BlockPos((int) (cmap.get("x") - scope), (int) (Math.floor(cmap.get("y")) - scope), (int) (Math.floor(cmap.get("z")) - scope));
        QueryableShipData<Ship> qsd = VSGameUtilsKt.getAllShips(level);
        Map<String, Object> mapper = new HashMap<>();
        try {
            qsd.iterator().forEachRemaining(e -> {
                AABBdc p = e.getWorldAABB();
                double[] c = getAABBdcCenter(p);
                AABB aabb = new AABB(startBlockPos, endBlockPos);
                BlockPos blockPos = new BlockPos((int) Math.floor(c[0]), (int) Math.floor(c[1]), (int) Math.floor(c[2]));

                boolean filterFlag = true;
                if (flag){
                    filterFlag = level.canSeeSky(blockPos);
                }

                if (filterFlag && aabb.contains(c[0], c[1], c[2])){
                    Map<String, Object> attr = new HashMap<>();
                    attr.put("id", e.getId());
                    attr.put("slug", e.getSlug());
                    attr.put("dimension", e.getChunkClaimDimension());
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
        } catch (RuntimeException ex) {
        }
        return mapper;
    }

    @LuaFunction
    public final Map<String, Object> getShips(int scope) {
        return getShipsRaw(scope, true);
    }

    @LuaFunction
    public final Map<String, Object> getShipsAll(int scope) {
        return getShipsRaw(scope, false);
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

    private Map<String, Double> getCoordinate() {
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
        if (isOnShip()) {
            Ship ship = getShip();
            return ship.getSlug();
        }
        return "NoSlug";
    }

    @LuaFunction
    public final void setSlug(String name) {
        if (isOnShip()) {
            ServerShip ship = VSGameUtilsKt.getShipManagingPos(VSGameUtilsKt.getLevelFromDimensionId(level.getServer(), VSGameUtilsKt.getDimensionId(level)), pos);
            if (ship != null) {
                ship.setSlug(name);
            }
        }
    }

    @LuaFunction
    public final @NotNull Map<String, Object> getPlayers(){
        List<ServerPlayer> players = level.getServer().getLevel(level.dimension()).getPlayers(LivingEntity::isAlive);
        Map<String, Object> result = new HashMap<>();
        players.forEach((e) -> {
            Map<Object, Object> map = new HashMap<>();
            map.put("x", e.getX());
            map.put("y", e.getY());
            map.put("z", e.getZ());
            map.put("name", e.getDisplayName().getString());
            String uuid = e.getUUID().toString();
            map.put("uuid", uuid);
            map.put("eyeHeight", e.getEyeHeight());
            Map<String, Object> viewVector = new HashMap<>();
            viewVector.put("x", e.getLookAngle().x);
            viewVector.put("y", e.getLookAngle().y);
            viewVector.put("z", e.getLookAngle().z);
            map.put("viewVector", viewVector);
            map.put("pose", e.getPose().toString());
            map.put("isPassenger", e.isPassenger());
            result.put(uuid, map);
        });
        return result;
    }

    @LuaFunction
    public final Map<Integer[], Integer> getMapColor(int x, int z, int x2, int z2) {
        Map<Integer[], Integer> map = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = x; i <= x2; i++) {
            for (int j = z; j <= z2; j++) {
                int k = this.level.getHeight(Heightmap.Types.WORLD_SURFACE, i, j);
                int finalI = i;
                int finalJ = j;
                futures.add(CompletableFuture.runAsync(() -> processCoordinate(finalI, finalJ, k, map), executor));
                if (futures.size() >= 10) {
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                    futures.clear();
                }
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();
        return map;
    }
    private void processCoordinate(int x, int z, int k, Map<Integer[], Integer> map) {
        int color = level.getBlockState(new BlockPos(x, k, z)).getMapColor(level, new BlockPos(x, k, z)).col;
        map.put(new Integer[]{x, k, z}, color);
    }
    @LuaFunction
    public final Map<String, Map<String, Object>> getMonster(int scope) {
        Map<String, Double> cmap = getCoordinate();
        BlockPos startBlockPos = new BlockPos((int) (Math.floor(cmap.get("x")) + scope), (int) (Math.floor(cmap.get("y")) + scope), (int) (Math.floor(cmap.get("z")) + scope));
        BlockPos endBlockPos = new BlockPos((int) (cmap.get("x") - scope), (int) (Math.floor(cmap.get("y")) - scope), (int) (Math.floor(cmap.get("z")) - scope));
        AABB aabb = new AABB(startBlockPos, endBlockPos);
        Map<String, Object> result = new HashMap<>();
        Map<String,Map<String,Object>> map = new HashMap<>();
        this.level.getServer().getLevel(this.level.dimension()).getEntities().getAll().iterator().forEachRemaining(entity -> {
            if (entity instanceof Monster && entity.isAlive() && aabb.contains(entity.getX(), entity.getY(), entity.getZ())) {
                Monster monster = (Monster) entity;
                result.put("uuid", monster.getUUID().toString());
                result.put("name", monster.getName().getString());
                result.put("displayName", monster.getDisplayName().getString());
                result.put("x", monster.getX());
                result.put("y", monster.getY());
                result.put("z", monster.getZ());
                result.put("health", monster.getHealth());
                result.put("maxHealth", monster.getMaxHealth());
                result.put("armor", monster.getArmorValue());
                map.put(monster.getUUID().toString(), result);
            }
        });
        return map;
    }

    @LuaFunction
    public final void scanTopography(int x, int z, int x2, int z2) {
        int minX = Math.min(x, x2);
        int maxX = Math.max(x, x2);
        int minZ = Math.min(z, z2);
        int maxZ = Math.max(z, z2);

        if (computer != null) {
            ArrayList<Integer[]> resultList = new ArrayList<>();
            if (maxX - minX > 256 || maxZ - minZ > 256) {
                computer.queueEvent("ComputerScanTopographyDone", new Object[]{resultList});
            }
            for (int sx = minX; sx < maxX; sx += 16) {
                for (int sz = minZ; sz < maxZ; sz += 16) {
                    LevelChunk chunk = level.getChunkAt(new BlockPos(sx, 64, sz));
                    int adjust_x = sx - (sx % 16);
                    adjust_x = adjust_x < 0 ? adjust_x - 16 : adjust_x;
                    int adjust_z = sz - (sz % 16);
                    adjust_z = adjust_z < 0 ? adjust_z - 16 : adjust_z;

                    for (int ix = 0; ix < 16; ix++) {
                        for (int iz = 0; iz < 16; iz++) {
                            int final_x = adjust_x + ix;
                            int final_z = adjust_z + iz;

                            if (final_x < maxX && final_z < maxZ) {
                                int height = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, ix, iz);
                                Integer[] pos = new Integer[3];
                                pos[0] = final_x;
                                pos[1] = height;
                                pos[2] = final_z;
                                resultList.add(pos);
                            }
                        }
                    }
                }
            }
            computer.queueEvent("ComputerScanTopographyDone", new Object[]{resultList});
        }
    }
}

