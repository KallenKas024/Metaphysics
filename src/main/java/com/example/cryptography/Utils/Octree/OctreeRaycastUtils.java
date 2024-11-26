package com.example.cryptography.Utils.Octree;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.joml.primitives.AABBdc;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.*;

public class OctreeRaycastUtils {

    private static Octree<Object> EntityMapper = new Octree<>();

    public static Map<String, Object> shut_raw(double selfPosX, double selfPosY, double selfPosZ, double directionX, double directionY, double directionZ, double distance, Player player, Level level) {
        if (directionX == 0 && directionY == 0 && directionZ == 0) {
            return Collections.emptyMap();
        }
        if (directionX > 1 && directionY > 1 && directionZ > 1) {
            return Collections.emptyMap();
        }
        if (player == null) {
            GameProfile gameProfile = new GameProfile(UUID.randomUUID(), UUID.randomUUID().toString());
            player = FakePlayerFactory.get(Objects.requireNonNull(level.getServer()).overworld(), gameProfile);
        }
        distance = distance > 2048 ? 2048 : distance < 1 ? 1 :distance;
        Vec3 selfPos = new Vec3(selfPosX, selfPosY, selfPosZ);
        Vec3 direction = new Vec3(directionX, directionY, directionZ);
        Map<String, Object> map = new HashMap<>();
        EntityHitResult entityResult = rayTraceWithEntity(selfPos, direction, distance, player);
        if (entityResult == null) {
            HitResult result = rayTrace(selfPos, direction, distance, player);
            if (result.getType() == HitResult.Type.MISS) {
                map.put("miss", "miss");
                return map;
            }else if (result.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blocksResult = (BlockHitResult) result;
                Ship ship = VSGameUtilsKt.getShipManagingPos(level, blocksResult.getBlockPos());
                if (ship != null) {
                    AABBdc aabb = null;
                    if (ship != null) {
                        aabb = ship.getWorldAABB();
                    }
                    Map<String, Object> m2 = new HashMap<>();
                    m2.put("maxX", aabb.maxX());
                    m2.put("maxY", aabb.maxY());
                    m2.put("maxZ", aabb.maxZ());
                    m2.put("minX", aabb.minX());
                    m2.put("minY", aabb.minY());
                    m2.put("minZ", aabb.minZ());
                    if (ship != null) {
                        m2.put("slug", ship.getSlug());
                    }
                    m2.put("id", ship.getId());
                    m2.put("blockX", blocksResult.getBlockPos().getX());
                    m2.put("blockY", blocksResult.getBlockPos().getY());
                    m2.put("blockZ", blocksResult.getBlockPos().getZ());
                    BlockState bs = level.getBlockState(blocksResult.getBlockPos());
                    m2.put("blockNamespace", bs.getBlock().getDescriptionId().replace("block.", "").replace(".", ":"));
                    map.put("ship_block", m2);
                    return map;
                } else {
                    Map<String, Object> m2 = new HashMap<>();
                    m2.put("x", blocksResult.getBlockPos().getX());
                    m2.put("y", blocksResult.getBlockPos().getY());
                    m2.put("z", blocksResult.getBlockPos().getZ());
                    BlockState bs = level.getBlockState(blocksResult.getBlockPos());
                    m2.put("namespace", bs.getBlock().getDescriptionId().replace("block.", "").replace(".", ":"));
                    map.put("block", m2);
                    return map;
                }
            }
        } else if (entityResult.getType() != HitResult.Type.MISS) {
            if (entityResult.getEntity() instanceof Player) {
                Map<String, Object> m2 = new HashMap<>();
                Player player2 = (Player) entityResult.getEntity();
                m2.put("x", entityResult.getEntity().getX());
                m2.put("y", entityResult.getEntity().getY());
                m2.put("z", entityResult.getEntity().getZ());
                m2.put("category", "player");
                m2.put("namespace", "minecraft:player");
                m2.put("name", player2.getName().getString());
                m2.put("health", player2.getHealth());
                m2.put("maxHealth", player2.getMaxHealth());
                m2.put("armor", player2.getArmorValue());
                map.put("player", m2);
            } else {
                Map<String, Object> m2 = new HashMap<>();
                LivingEntity entity = (LivingEntity) entityResult.getEntity();
                m2.put("x", entityResult.getEntity().getX());
                m2.put("y", entityResult.getEntity().getY());
                m2.put("z", entityResult.getEntity().getZ());
                m2.put("category", entityResult.getEntity().getType().getCategory().getSerializedName());
                m2.put("namespace", entityResult.getEntity().getType().getDescriptionId().replace("entity.", "").replace(".", ":"));
                m2.put("displayname", entityResult.getEntity().getDisplayName());
                m2.put("health", entity.getHealth());
                m2.put("maxHealth", entity.getMaxHealth());
                m2.put("armor", entity.getArmorValue());
                map.put("entity", m2);
                return map;
            }
        }
        return map;
    }
    public static Octree<Object> getEntityMapper() {
        return EntityMapper;
    }
}

