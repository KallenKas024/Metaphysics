package com.example.cryptography.Apis;
import com.example.cryptography.Utils.Octree.OctreeRaycastUtils;
import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.LuaFunction;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import dan200.computercraft.core.computer.Computer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.valkyrienskies.mod.common.world.RaycastUtilsKt;
public class RaycastApi implements ILuaAPI {
    private final Level level;
    private ThreadPoolExecutor threadPool;
    private final Computer computer;
    public RaycastApi(Level level, Computer computer) {
        this.level = level;
        this.computer = computer;
    }
    private HitResult rayTrace(Vec3 selfPos, Vec3 direction, double range, Player player) {
        Vec3 end = selfPos.add(direction.x * range, direction.y * range, direction.z * range);
        ClipContext.Fluid fluidMode = ClipContext.Fluid.NONE;
        ClipContext.Block blockMode = ClipContext.Block.VISUAL;
        ClipContext context = new ClipContext(selfPos, end, blockMode, fluidMode, player);
        return RaycastUtilsKt.clipIncludeShips(this.level, context);
    }
    private EntityHitResult rayTraceWithEntity(Vec3 selfPos, Vec3 direction, double range, Player player) {
        Vec3 end = selfPos.add(direction.x * range, direction.y * range, direction.z * range);
        return RaycastUtilsKt.raytraceEntities(this.level, player, selfPos, end, new AABB(selfPos, end), entity -> {
            if (entity instanceof LivingEntity) {
                if (entity instanceof Player p) {
                    if (p.getStringUUID().equalsIgnoreCase(player.getStringUUID())) {
                        return false;
                    } else {
                        return true;
                    }
                }
                return true;
            } else {
                return false;
            }
        }, range);
    }

    @LuaFunction
    public final String perform(int selfPosX, int selfPosY, int selfPosZ, double directionX, double directionY, double directionZ, double distance, String uuid2) {
        String uuid = UUID.randomUUID().toString();
        if (uuid2.isEmpty() || uuid2.isBlank()) {
            uuid2 = UUID.randomUUID().toString();
        }
        String finalUuid = uuid2;
        threadPool.execute(() -> {
            Map < String, Object > result = OctreeRaycastUtils.shut_raw(selfPosX, selfPosY, selfPosZ, directionX, directionY, directionZ, distance, level.getPlayerByUUID(UUID.fromString(finalUuid)), level);
            if (computer != null) {
                computer.queueEvent(uuid, new Map[] {result});
            }
        });
        return uuid;
    }
    //dont use it as a LuaFun

    @Override
    public String[] getNames() {
        return new String[]{"raytrace", "raycast", "scanner"};
    }
    @Override
    public void startup() {
        this.threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        this.threadPool.setMaximumPoolSize(10);
    }
    @Override
    public void shutdown() {
        this.threadPool.shutdown();
    }
}
