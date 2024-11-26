package com.example.cryptography.mixin;

import com.example.cryptography.Utils.Octree.OctreeRaycastUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow private Vec3 position;

    @Inject(method = "move", at = @At("RETURN"), remap = false)
    public void move(MoverType pType, Vec3 pPos, CallbackInfo ci) {
        OctreeRaycastUtils.getEntityMapper().remove((int)Math.ceil(position.x), (int)Math.ceil(position.y), (int)Math.ceil(position.z));
        OctreeRaycastUtils.getEntityMapper().insert((int)Math.ceil(pPos.x()), (int)Math.ceil(pPos.y()), (int)Math.ceil(pPos.z()), this);
    }
    @Inject(method = "remove", at = @At("RETURN"), remap = false)
    public void remove(Entity.RemovalReason pReason, CallbackInfo ci) {
        OctreeRaycastUtils.getEntityMapper().remove((int)Math.ceil(position.x), (int)Math.ceil(position.y), (int)Math.ceil(position.z));
    }
    @Inject(method = "setPos(DDD)V", at = @At("RETURN"), remap = false)
    public final void setPos(double p_20210_, double p_20211_, double p_20212_, CallbackInfo ci) {
        OctreeRaycastUtils.getEntityMapper().remove((int)Math.ceil(position.x), (int)Math.ceil(position.y), (int)Math.ceil(position.z));
        OctreeRaycastUtils.getEntityMapper().insert((int)Math.ceil(p_20210_), (int)Math.ceil(p_20211_), (int)Math.ceil(p_20212_), this);
    }
    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    public void Entity(EntityType pEntityType, Level pLevel, CallbackInfo ci) {
        OctreeRaycastUtils.getEntityMapper().remove((int)Math.ceil(position.x), (int)Math.ceil(position.y), (int)Math.ceil(position.z));
    }
}
